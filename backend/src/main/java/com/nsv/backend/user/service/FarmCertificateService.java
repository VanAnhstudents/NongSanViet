package com.nsv.backend.user.service;

import com.nsv.backend.user.dto.*;

import java.util.List;

public interface FarmCertificateService {
    FarmCertificateResponse submitCertificate(Long farmerId, SubmitCertificateRequest request);
    List<FarmCertificateResponse> getFarmerCertificates(Long farmerId);
    List<FarmCertificateResponse> getCertificatesForAdmin(String status);
    FarmCertificateResponse reviewCertificate(Long adminId, Long certificateId, ReviewCertificateRequest request);
    FarmProfileResponse getFarmProfileWithBadges(Long farmId);
}
