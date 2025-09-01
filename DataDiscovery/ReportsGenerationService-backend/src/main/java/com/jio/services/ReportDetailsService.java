package com.jio.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jio.convertor.ReportDetailsConvertor;
import com.jio.convertor.VulnerabilitiesSummaryConvertor;
import com.jio.dto.ReportDetailsDto;
import com.jio.dto.VulnerabilitiesSummaryDto;
import com.jio.entity.ModificationHistory;
import com.jio.entity.ReportDetails;
import com.jio.entity.TotalReport;
import com.jio.entity.VulnerabilitiesSummary;
import com.jio.repository.ModificationHistoryRepository;
import com.jio.repository.ReportDetailsRepository;
import com.jio.repository.VulnerabilitiesSummaryRepository;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ReportDetailsService {

	@Autowired
	private ReportDetailsRepository reportDetailsRepository;

	@Autowired
	private ModificationHistoryRepository modificationHistoryRepository;

	@Autowired
	private VulnerabilitiesSummaryRepository vulnerabilitiesSummaryRepository;

	@Transactional
	public ReportDetailsDto createOrUpdateReportDetailsFUI(ReportDetailsDto dto) {

		ReportDetails reportDetails = dto.getReportId() != null ? reportDetailsRepository
				.findByReportId(dto.getReportId()).orElseThrow(() -> new RuntimeException("Report not found"))
				: new ReportDetails();

		reportDetails = ReportDetailsConvertor.convertToReportDetailsEntity(dto);
		ReportDetails savedReportDetails = reportDetailsRepository.save(reportDetails);

		List<VulnerabilitiesSummary> existingVulns = vulnerabilitiesSummaryRepository
				.findAllByReportId(savedReportDetails.getReportId());
		int nextIndex = existingVulns.size() + 1;

		List<VulnerabilitiesSummary> updatedVulnerabilities = new ArrayList<>();

		for (VulnerabilitiesSummaryDto vulnerabilitiesSummaryDto : dto.getVulnerabilities()) {
			VulnerabilitiesSummary vulnerabilitiesSummary;

			if (vulnerabilitiesSummaryDto.getId() != null) {
				vulnerabilitiesSummary = vulnerabilitiesSummaryRepository.findById(vulnerabilitiesSummaryDto.getId())
						.orElseThrow(() -> new RuntimeException("Vulnerability not found"));
				vulnerabilitiesSummary = VulnerabilitiesSummaryConvertor
						.convertToVulnerabilityEntity(vulnerabilitiesSummaryDto);
			} else {
				vulnerabilitiesSummary = VulnerabilitiesSummaryConvertor
						.convertToVulnerabilityEntity(vulnerabilitiesSummaryDto);

				if (vulnerabilitiesSummary.getVulId() == null) {
					String vulId = String.format("IDX%03d", nextIndex++);
					vulnerabilitiesSummary.setVulId(vulId);
				}
			}

			vulnerabilitiesSummary.setReportId(savedReportDetails.getReportId());
			updatedVulnerabilities.add(vulnerabilitiesSummaryRepository.save(vulnerabilitiesSummary));
		}

		ReportDetailsDto responseDto = ReportDetailsConvertor.convertToReportDetailsDto(savedReportDetails);
		responseDto.setVulnerabilities(VulnerabilitiesSummaryConvertor.convertToVulnerabilityDTOList(
				vulnerabilitiesSummaryRepository.findAllByReportId(savedReportDetails.getReportId())));

		return responseDto;
	}

	@Transactional
	public ReportDetailsDto createOrUpdateReportDetailsFExcel(ReportDetailsDto dto) {

		// 1. Fetch existing or create new ReportDetails
		ReportDetails reportDetails = dto.getReportId() != null ? reportDetailsRepository
				.findByReportId(dto.getReportId()).orElseThrow(() -> new RuntimeException("Report not found"))
				: new ReportDetails();

		// 2. Convert and save ReportDetails
		reportDetails = ReportDetailsConvertor.convertToReportDetailsEntity(dto);
		ReportDetails savedReportDetails = reportDetailsRepository.save(reportDetails);

		// 3. Fetch existing vulnerabilities for this report
		List<VulnerabilitiesSummary> existingVulns = vulnerabilitiesSummaryRepository
				.findAllByReportId(savedReportDetails.getReportId());
		int nextIndex = existingVulns.size() + 1;

		List<VulnerabilitiesSummary> updatedVulnerabilities = new ArrayList<>();
		List<VulnerabilitiesSummaryDto> duplicates = new ArrayList<>();

		// 4. Loop through vulnerabilities and handle duplicates
		for (VulnerabilitiesSummaryDto vulnDto : dto.getVulnerabilities()) {

			// Check if vulnerability already exists in the database (by reportId and
			// vulnerability name)
			boolean isDuplicate = existingVulns.stream()
					.anyMatch(v -> v.getReportId().equals(savedReportDetails.getReportId())
							&& v.getVulnerability().equalsIgnoreCase(vulnDto.getVulnerability()));

			if (isDuplicate) {
				duplicates.add(vulnDto); // Collect duplicates (optional: you can log or handle differently)
				continue; // Skip adding this vulnerability as it’s a duplicate
			}

			// 5. Handle non-duplicate vulnerabilities
			VulnerabilitiesSummary vulnerability;
			if (vulnDto.getId() != null) {
				vulnerability = vulnerabilitiesSummaryRepository.findById(vulnDto.getId())
						.orElseThrow(() -> new RuntimeException("Vulnerability not found"));
				vulnerability = VulnerabilitiesSummaryConvertor.convertToVulnerabilityEntity(vulnDto);
			} else {
				vulnerability = VulnerabilitiesSummaryConvertor.convertToVulnerabilityEntity(vulnDto);

				// Set a unique ID if not provided (for new vulnerabilities)
				if (vulnerability.getVulId() == null) {
					String vulId = String.format("IDX%03d", nextIndex++);
					vulnerability.setVulId(vulId);
				}
			}

			// Set the reportId for the vulnerability
			vulnerability.setReportId(savedReportDetails.getReportId());

			// Save the vulnerability to the database
			updatedVulnerabilities.add(vulnerabilitiesSummaryRepository.save(vulnerability));
		}

		// 6. Build response DTO
		ReportDetailsDto responseDto = ReportDetailsConvertor.convertToReportDetailsDto(savedReportDetails);

		// Fetch the updated list of vulnerabilities for this report
		responseDto.setVulnerabilities(VulnerabilitiesSummaryConvertor.convertToVulnerabilityDTOList(
				vulnerabilitiesSummaryRepository.findAllByReportId(savedReportDetails.getReportId())));

		// 7. Set the optional duplicates list (if any)
		responseDto.setDuplicateVulnerabilities(duplicates); // If no duplicates, will be empty list
		return responseDto;
	}

	public Map<String, Object> checkReportScope(String scope) {
		Map<String, Object> response = new HashMap<>();

		if (scope == null || scope.trim().isEmpty()) {
			throw new IllegalArgumentException("Scope cannot be null or empty");
		}

		Optional<ReportDetails> existingReport = reportDetailsRepository.findByScopeIgnoreCase(scope.trim());

		if (existingReport.isPresent()) {
			response.put("isDuplicate", true);
			response.put("reportId", existingReport.get().getReportId());
		} else {
			response.put("isDuplicate", false);
			response.put("reportId", null);
		}

		return response;
	}

	public ReportDetailsDto getReportDetailseById(int reportId) {
		ReportDetails reportDetails = reportDetailsRepository.findById(reportId)
				.orElseThrow(() -> new EntityNotFoundException("Report Details not found with ID: " + reportId));
		ReportDetailsDto reportDetailsDto = ReportDetailsConvertor.convertToReportDetailsDto(reportDetails);
		List<VulnerabilitiesSummary> vulnerabilitiesSummariesList = vulnerabilitiesSummaryRepository
				.getVulnerabilitiesListByReportId(reportDetails.getReportId());
		List<VulnerabilitiesSummaryDto> vulnerabilitiesSummariesDtoList = VulnerabilitiesSummaryConvertor
				.convertToVulnerabilityDTOList(vulnerabilitiesSummariesList);
		reportDetailsDto.setVulnerabilities(vulnerabilitiesSummariesDtoList);
		return reportDetailsDto;
	}

	public Page<ReportDetailsDto> getAllReportDetails(Integer offset, Integer pageSize, String field, Integer sort) {
		Pageable pageable = null;
		Direction direction = sort != null && sort == 0 ? Direction.DESC : Direction.ASC;

		if (!StringUtils.isEmpty(field)) {
			pageable = PageRequest.of(offset, pageSize, Sort.by(direction, field));
		} else {
			pageable = PageRequest.of(offset, pageSize, Sort.by(direction, "reportId"));
		}

		Page<ReportDetails> reportDetailsPage = reportDetailsRepository.findAll(pageable);

		List<Integer> reportIds = reportDetailsPage.getContent().stream().map(ReportDetails::getReportId)
				.collect(Collectors.toList());

		List<VulnerabilitiesSummary> vulnerabilities = vulnerabilitiesSummaryRepository.findByReportIdIn(reportIds);

		Map<Integer, List<VulnerabilitiesSummary>> vulnerabilitiesGrouped = vulnerabilities.stream()
				.sorted(Comparator.comparingInt(v -> {
					switch (v.getSeverity().toUpperCase()) {
					case "HIGH":
						return 0;
					case "MEDIUM":
						return 1;
					case "LOW":
						return 2;
					default:
						return 3;
					}
				})).collect(Collectors.groupingBy(VulnerabilitiesSummary::getReportId, LinkedHashMap::new,
						Collectors.toList()));
		return ReportDetailsConvertor.toReportDetailsDTOPageList(reportDetailsPage, pageable, vulnerabilitiesGrouped);
	}

	public ReportDetailsDto updateReport(Integer reportId, ReportDetailsDto dto) {
		Optional<ReportDetails> optional = reportDetailsRepository.findById(reportId);
		if (optional.isPresent()) {
			ReportDetails entity = ReportDetailsConvertor.convertToReportDetailsEntity(dto);
			entity.setReportId(reportId);
			ReportDetails updated = reportDetailsRepository.save(entity);
			return ReportDetailsConvertor.convertToReportDetailsDto(updated);
		} else {
			throw new RuntimeException("Report not found with ID: " + reportId);
		}
	}

	public ReportDetailsDto getById(Integer reportId) {
		return reportDetailsRepository.findByReportId(reportId).map(ReportDetailsConvertor::convertToReportDetailsDto)
				.orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
	}

	public ReportDetailsDto getReportById(Integer reportId) {
		return reportDetailsRepository.findByReportId(reportId).map(ReportDetailsConvertor::convertToReportDetailsDto)
				.orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
	}

	public List<ReportDetailsDto> getAllReports() {
		return ReportDetailsConvertor.convertToReportDetailsDtos(reportDetailsRepository.findAll());
	}

	public void deleteReport(Integer id) {
		if (!reportDetailsRepository.existsById(id)) {
			throw new RuntimeException("Report not found with ID: " + id);
		}
		reportDetailsRepository.deleteById(id);
	}

	public TotalReport getTotalReportByReportId(Integer reportId) {
		TotalReport totalReport = new TotalReport();

		ReportDetails reportDetails = reportDetailsRepository.findByReportId(reportId)
				.orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + reportId));

		List<ModificationHistory> modificationHistories = modificationHistoryRepository.findByReportId(reportId);

		List<VulnerabilitiesSummary> vulnerabilitiesSummaries = vulnerabilitiesSummaryRepository
				.findByReportId(reportId).stream().sorted(Comparator.comparingInt(v -> {
					String severity = v.getSeverity() != null ? v.getSeverity().toUpperCase() : "";
					switch (severity) {
					case "HIGH":
						return 0;
					case "MEDIUM":
						return 1;
					case "LOW":
						return 2;
					default:
						return 3;
					}
				})).collect(Collectors.toList());

		totalReport.setReportDetails(reportDetails);
		totalReport.setModificationHistories(modificationHistories);
		totalReport.setVulnerabilitiesSummaries(vulnerabilitiesSummaries);

		return totalReport;
	}

}
