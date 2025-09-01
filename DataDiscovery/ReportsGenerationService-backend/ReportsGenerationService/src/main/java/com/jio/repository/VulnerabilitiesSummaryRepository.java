package com.jio.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jio.entity.VulnerabilitiesSummary;

@Repository
public interface VulnerabilitiesSummaryRepository extends JpaRepository<VulnerabilitiesSummary, Integer> {

	@Query("select vs from VulnerabilitiesSummary vs where (vs.createdDate between :startDate AND :endDate) and (vs.vulnerability LIKE %:searchText% OR vs.affectedScope LIKE %:searchText% )")
	Page<VulnerabilitiesSummary> findAllVulnerabilitiesBetweenDateSearchAndReportId(@Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param("searchText") String searchText, Pageable pageable);

	@Query("select vs from VulnerabilitiesSummary vs where (vs.createdDate between :startDate AND :endDate) and (vs.vulnerability LIKE %:searchText% OR vs.affectedScope LIKE %:searchText% )")
	Page<VulnerabilitiesSummary> findAllVulnerabilitiesBetweenDateAndSearch(@Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param("searchText") String searchText, Pageable pageable);

	@Query("select vs from VulnerabilitiesSummary vs where (vs.createdDate between :startDate AND :endDate)")
	Page<VulnerabilitiesSummary> findAllVulnerabilitiesBetweenDateWithReportId(@Param("startDate") Date startDate,
			@Param("endDate") Date endDate, Pageable pageable);

	@Query("select vs from VulnerabilitiesSummary vs where (vs.createdDate between :startDate AND :endDate)")
	Page<VulnerabilitiesSummary> findAllVulnerabilitiesBetweenDate(@Param("startDate") Date startDate,
			@Param("endDate") Date endDate, Pageable pageable);

	List<VulnerabilitiesSummary> findByReportId(Integer reportId);

	List<VulnerabilitiesSummary> findAllByReportId(Integer reportId);

	List<VulnerabilitiesSummary> getVulnerabilitiesListByReportId(Integer reportId);

	List<VulnerabilitiesSummary> findByReportIdIn(List<Integer> reportIds);

}
