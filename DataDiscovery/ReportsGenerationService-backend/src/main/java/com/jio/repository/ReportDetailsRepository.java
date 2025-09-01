package com.jio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jio.entity.ReportDetails;

@Repository
public interface ReportDetailsRepository extends JpaRepository<ReportDetails, Integer> {

	Optional<ReportDetails> findByReportId(Integer reportId);

	boolean existsByScopeIgnoreCase(String trim);

	Optional<ReportDetails> findByScopeIgnoreCase(String trim);

}
