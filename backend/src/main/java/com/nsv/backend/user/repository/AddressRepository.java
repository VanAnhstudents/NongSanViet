package com.nsv.backend.user.repository;

import com.nsv.backend.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("select a from Address a join fetch a.province where a.userId = :userId order by a.id desc")
    List<Address> findAllByUser(@Param("userId") Long userId);

    @Query("select a from Address a join fetch a.province where a.id = :id and a.userId = :userId")
    Optional<Address> findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
