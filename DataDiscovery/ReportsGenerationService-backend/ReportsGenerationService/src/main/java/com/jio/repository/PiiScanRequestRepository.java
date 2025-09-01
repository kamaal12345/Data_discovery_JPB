package com.jio.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jio.entity.PiiScanRequest;

@Repository
public interface PiiScanRequestRepository extends JpaRepository<PiiScanRequest, Long> {

	Page<PiiScanRequest> findByFilePathContainingIgnoreCase(String searchText, Pageable pageable);

}
