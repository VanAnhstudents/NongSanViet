package com.nsv.backend.user.repository;

import com.nsv.backend.user.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<Province, Long> {

    List<Province> findAllByOrderByNameAsc();
}
