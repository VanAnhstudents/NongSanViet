package com.nsv.backend.user.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.user.dto.*;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.entity.FarmCertificate;
import com.nsv.backend.user.repository.FarmCertificateRepository;
import com.nsv.backend.user.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmCertificateServiceImpl implements FarmCertificateService {

    private static final Set<String> VALID_CERT_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED");

    private final FarmRepository farmRepository;
    private final FarmCertificateRepository certificateRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public FarmCertificateResponse submitCertificate(Long farmerId, SubmitCertificateRequest request) {
        Farm farm = farmRepository.findByUserId(farmerId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "FARM_NOT_FOUND",
                "Bạn cần tạo hồ sơ trang trại trước khi nộp chứng nhận"
            ));

        if (request.issueDate() != null && request.expiryDate() != null
                && request.expiryDate().isBefore(request.issueDate())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_CERTIFICATE_DATES",
                "Ngày hết hạn chứng nhận không được trước ngày cấp"
            );
        }

        FarmCertificate certificate = FarmCertificate.builder()
            .farmId(farm.getId())
            .certificateType(request.certificateType().trim().toUpperCase())
            .certificateNumber(StringUtils.hasText(request.certificateNumber()) ? request.certificateNumber().trim() : null)
            .issuedBy(StringUtils.hasText(request.issuedBy()) ? request.issuedBy().trim() : null)
            .issueDate(request.issueDate())
            .expiryDate(request.expiryDate())
            .fileUrl(request.fileUrl().trim())
            .status("PENDING")
            .build();

        FarmCertificate saved = certificateRepository.save(certificate);

        if ("NONE".equals(farm.getCertificateStatus()) || "REJECTED".equals(farm.getCertificateStatus())) {
            farm.setCertificateStatus("PENDING");
            farmRepository.save(farm);
        }

        recordAuditLog(farmerId, "SUBMIT_FARM_CERTIFICATE", "farm_certificates", saved.getId());
        String farmerName = querySingleString("SELECT full_name FROM users WHERE id = ?", farmerId);

        return FarmCertificateResponse.from(saved, farm, farmerName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmCertificateResponse> getFarmerCertificates(Long farmerId) {
        Farm farm = farmRepository.findByUserId(farmerId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "FARM_NOT_FOUND",
                "Không tìm thấy hồ sơ trang trại"
            ));

        String farmerName = querySingleString("SELECT full_name FROM users WHERE id = ?", farmerId);
        return certificateRepository.findAllByFarmIdOrderByCreatedAtDesc(farm.getId()).stream()
            .map(cert -> FarmCertificateResponse.from(cert, farm, farmerName))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmCertificateResponse> getCertificatesForAdmin(String statusFilter) {
        List<FarmCertificate> certificates;
        if (StringUtils.hasText(statusFilter)) {
            String normalized = statusFilter.trim().toUpperCase();
            if (!VALID_CERT_STATUSES.contains(normalized)) {
                throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_CERTIFICATE_STATUS",
                    "Trạng thái lọc chứng nhận không hợp lệ"
                );
            }
            certificates = certificateRepository.findAllByStatusOrderByCreatedAtDesc(normalized);
        } else {
            certificates = certificateRepository.findAllByOrderByCreatedAtDesc();
        }

        if (certificates.isEmpty()) {
            return List.of();
        }

        Set<Long> farmIds = certificates.stream()
            .map(FarmCertificate::getFarmId)
            .collect(Collectors.toSet());

        Map<Long, Farm> farmMap = farmRepository.findAllById(farmIds).stream()
            .collect(Collectors.toMap(Farm::getId, Function.identity()));

        Map<Long, String> farmerNameCache = new HashMap<>();
        for (Farm farm : farmMap.values()) {
            farmerNameCache.computeIfAbsent(
                farm.getUserId(),
                uid -> querySingleString("SELECT full_name FROM users WHERE id = ?", uid)
            );
        }

        return certificates.stream()
            .map(cert -> {
                Farm farm = farmMap.get(cert.getFarmId());
                String farmerName = farm != null ? farmerNameCache.get(farm.getUserId()) : null;
                return FarmCertificateResponse.from(cert, farm, farmerName);
            })
            .toList();
    }

    @Override
    @Transactional
    public FarmCertificateResponse reviewCertificate(
            Long adminId,
            Long certificateId,
            ReviewCertificateRequest request
    ) {
        FarmCertificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "CERTIFICATE_NOT_FOUND",
                "Không tìm thấy hồ sơ chứng nhận"
            ));

        if (!"PENDING".equals(certificate.getStatus())) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "CERTIFICATE_ALREADY_REVIEWED",
                "Chứng nhận này đã được xét duyệt trước đó với trạng thái " + certificate.getStatus()
            );
        }

        String decision = request.decision().trim().toUpperCase();
        if ("REJECTED".equals(decision) && !StringUtils.hasText(request.rejectionReason())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "REJECTION_REASON_REQUIRED",
                "Vui lòng nhập lý do từ chối chứng nhận"
            );
        }

        certificate.setStatus(decision);
        certificate.setRejectionReason("REJECTED".equals(decision) ? request.rejectionReason().trim() : null);
        certificate.setReviewedBy(adminId);
        certificate.setReviewedAt(OffsetDateTime.now());

        FarmCertificate savedCert = certificateRepository.save(certificate);

        Farm farm = farmRepository.findById(savedCert.getFarmId())
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "FARM_NOT_FOUND",
                "Không tìm thấy trang trại sở hữu chứng nhận"
            ));

        syncFarmVerificationStatus(farm);

        String notificationContent = "APPROVED".equals(decision)
            ? String.format(
                "Chứng nhận %s của trang trại \"%s\" đã được Admin phê duyệt và gắn huy hiệu xác minh.",
                savedCert.getCertificateType(), farm.getFarmName()
            )
            : String.format(
                "Chứng nhận %s của trang trại \"%s\" bị từ chối. Lý do: %s.",
                savedCert.getCertificateType(), farm.getFarmName(), savedCert.getRejectionReason()
            );

        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, content, is_read) VALUES (?, ?, FALSE)",
            farm.getUserId(),
            notificationContent
        );
        recordAuditLog(adminId, "REVIEW_FARM_CERTIFICATE_" + decision, "farm_certificates", savedCert.getId());

        String farmerName = querySingleString("SELECT full_name FROM users WHERE id = ?", farm.getUserId());
        return FarmCertificateResponse.from(savedCert, farm, farmerName);
    }

    @Override
    @Transactional(readOnly = true)
    public FarmProfileResponse getFarmProfileWithBadges(Long farmId) {
        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "FARM_NOT_FOUND",
                "Không tìm thấy hồ sơ trang trại"
            ));

        List<FarmCertificate> approvedCerts = certificateRepository
            .findAllByFarmIdAndStatusOrderByReviewedAtDesc(farm.getId(), "APPROVED");

        List<VerifiedBadgeResponse> badges = approvedCerts.stream()
            .map(VerifiedBadgeResponse::from)
            .toList();

        boolean isVerified = !badges.isEmpty() || "VERIFIED".equals(farm.getCertificateStatus());

        String farmerName = querySingleString("SELECT full_name FROM users WHERE id = ?", farm.getUserId());
        String provinceName = querySingleString("SELECT name FROM provinces WHERE id = ?", farm.getProvinceId());
        String categoryName = querySingleString("SELECT name FROM categories WHERE id = ?", farm.getMainCategoryId());

        return new FarmProfileResponse(
            farm.getId(),
            farm.getUserId(),
            farmerName,
            farm.getFarmName(),
            farm.getDescription(),
            provinceName,
            farm.getAddressLine(),
            categoryName,
            isVerified ? "VERIFIED" : farm.getCertificateStatus(),
            isVerified,
            badges.size(),
            badges,
            farm.getCreatedAt()
        );
    }

    private void syncFarmVerificationStatus(Farm farm) {
        List<FarmCertificate> allFarmCerts = certificateRepository.findAllByFarmIdOrderByCreatedAtDesc(farm.getId());

        Optional<FarmCertificate> latestApproved = allFarmCerts.stream()
            .filter(c -> "APPROVED".equals(c.getStatus()))
            .findFirst();

        if (latestApproved.isPresent()) {
            farm.setCertificateStatus("VERIFIED");
            farm.setCertificateUrl(latestApproved.get().getFileUrl());
        } else if (allFarmCerts.stream().anyMatch(c -> "PENDING".equals(c.getStatus()))) {
            farm.setCertificateStatus("PENDING");
        } else {
            farm.setCertificateStatus("REJECTED");
        }
        farmRepository.save(farm);
    }

    private void recordAuditLog(Long actorId, String action, String entityType, Long entityId) {
        jdbcTemplate.update(
            "INSERT INTO audit_logs (actor_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)",
            actorId,
            action,
            entityType,
            entityId
        );
    }

    private String querySingleString(String sql, Object param) {
        return jdbcTemplate.query(
            sql,
            ps -> ps.setObject(1, param),
            rs -> rs.next() ? rs.getString(1) : null
        );
    }
}
