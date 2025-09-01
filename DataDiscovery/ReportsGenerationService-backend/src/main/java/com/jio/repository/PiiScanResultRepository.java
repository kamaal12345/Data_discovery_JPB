package com.jio.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jio.entity.PiiScanResult;

@Repository
public interface PiiScanResultRepository extends JpaRepository<PiiScanResult, Long> {

	Page<PiiScanResult> findByMatchedDataContainingIgnoreCase(String searchText, Pageable pageable);

	Page<PiiScanResult> findByPiiScanRequest_RequestId(Long requestId, Pageable pageable);
	
	Page<PiiScanResult> findByPiiScanRequest_RequestIdAndPiiTypeContainingIgnoreCase(
	        Long requestId,
	        String piiType,
	        Pageable pageable);
	
    @Query("SELECT r FROM PiiScanResult r " +
            "WHERE r.piiScanRequest.requestId = :requestId " +
            "AND (" +
            "LOWER(r.piiType) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(r.filePath) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(r.matchedData) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            ")")
     Page<PiiScanResult> searchByRequestIdAndText(
             @Param("requestId") Long requestId,
             @Param("searchText") String searchText,
             Pageable pageable);
    
    
    List<PiiScanResult> findByPiiScanRequest_RequestId(Long requestId);


}
