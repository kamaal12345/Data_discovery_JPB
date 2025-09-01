package com.jio.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.jio.convertor.VulnerabilitiesSummaryConvertor;
import com.jio.dto.VulnerabilitiesSummaryDto;
import com.jio.entity.VulnerabilitiesSummary;
import com.jio.repository.ModificationHistoryRepository;
import com.jio.repository.VulnerabilitiesSummaryRepository;

import io.micrometer.common.util.StringUtils;

@Service
public class VulnerabilitiesService {

	private final VulnerabilitiesSummaryRepository vulnerabilitiesSummaryRepository;

	public VulnerabilitiesService(VulnerabilitiesSummaryRepository vulnerabilitiesSummaryRepository,
			ModificationHistoryRepository modificationHistoryRepository) {
		this.vulnerabilitiesSummaryRepository = vulnerabilitiesSummaryRepository;
	}

	public VulnerabilitiesSummaryDto createVulnerability(VulnerabilitiesSummaryDto dto) {
		VulnerabilitiesSummary entity = VulnerabilitiesSummaryConvertor.convertToVulnerabilityEntity(dto);
		return VulnerabilitiesSummaryConvertor.convertToVulnerabilityDTO(vulnerabilitiesSummaryRepository.save(entity));
	}

	public VulnerabilitiesSummaryDto updateVulnerability(Integer id, VulnerabilitiesSummaryDto dto) {
		Optional<VulnerabilitiesSummary> optional = vulnerabilitiesSummaryRepository.findById(id);
		if (optional.isPresent()) {
			VulnerabilitiesSummary entity = optional.get();
			entity.setSeverity(dto.getSeverity());
			entity.setVulId(dto.getVulId());
			entity.setVulnerability(dto.getVulnerability());
			entity.setAffectedScope(dto.getAffectedScope());
			entity.setDescription(dto.getDescription());
			entity.setObservation(dto.getObservation());
			entity.setTestDetails(dto.getTestDetails());
			entity.setRemediation(dto.getRemediation());
			entity.setReferences(dto.getReferences());
//			entity.setPicCount(dto.getPicCount());
			entity.setReportId(dto.getReportId());
			return VulnerabilitiesSummaryConvertor
					.convertToVulnerabilityDTO(vulnerabilitiesSummaryRepository.save(entity));
		} else {
			throw new RuntimeException("Vulnerability with ID " + id + " not found");
		}
	}

	public VulnerabilitiesSummaryDto getVulnerability(Integer id) {
		return vulnerabilitiesSummaryRepository.findById(id)
				.map(VulnerabilitiesSummaryConvertor::convertToVulnerabilityDTO)
				.orElseThrow(() -> new RuntimeException("Vulnerability with ID " + id + " not found"));
	}

	public List<VulnerabilitiesSummaryDto> getAllVulnerabilities() {
		List<VulnerabilitiesSummary> list = vulnerabilitiesSummaryRepository.findAll();
		return list.stream().map(VulnerabilitiesSummaryConvertor::convertToVulnerabilityDTO)
				.collect(Collectors.toList());
	}

	public Page<VulnerabilitiesSummary> getAllVulnerabilities(Integer offset, Integer pageSize, String field,
			Integer sort, String searchText, Date startDate, Date endDate) {
		Pageable pageable = null;
		Direction direction = sort != null && sort == 0 ? Direction.DESC : Direction.ASC;

		if (!StringUtils.isEmpty(field)) {
			pageable = PageRequest.of(offset, pageSize, Sort.by(direction, field));
		} else {
			pageable = PageRequest.of(offset, pageSize, Sort.by(direction, "templateId"));
		}

		if (searchText != "" && (startDate != null) && (endDate != null)) {
			Page<VulnerabilitiesSummary> foundVulnerabilities = vulnerabilitiesSummaryRepository
					.findAllVulnerabilitiesBetweenDateSearchAndReportId(startDate, endDate, searchText, pageable);
			return foundVulnerabilities;
		}
		if (searchText != "" && (startDate != null) && (endDate != null)) {
			Page<VulnerabilitiesSummary> foundVulnerabilities = vulnerabilitiesSummaryRepository
					.findAllVulnerabilitiesBetweenDateAndSearch(startDate, endDate, searchText, pageable);
			return foundVulnerabilities;
		}
		if ((startDate != null) && (endDate != null)) {
			Page<VulnerabilitiesSummary> findByCreatedDateBetween = vulnerabilitiesSummaryRepository
					.findAllVulnerabilitiesBetweenDateWithReportId(startDate, endDate, pageable);
			return findByCreatedDateBetween;
		}

		if ((startDate != null) && (endDate != null)) {
			Page<VulnerabilitiesSummary> findByCreatedDateBetween = vulnerabilitiesSummaryRepository
					.findAllVulnerabilitiesBetweenDate(startDate, endDate, pageable);
			return findByCreatedDateBetween;
		}

		return vulnerabilitiesSummaryRepository.findAll(pageable);
	}

	public List<VulnerabilitiesSummaryDto> getAllVulnerabilitiesByReportId(Integer reportId) {
		List<VulnerabilitiesSummary> list = vulnerabilitiesSummaryRepository.findAllByReportId(reportId);
		return list.stream().map(VulnerabilitiesSummaryConvertor::convertToVulnerabilityDTO)
				.collect(Collectors.toList());
	}

	public void deleteVulnerability(Integer id) {
		vulnerabilitiesSummaryRepository.deleteById(id);
	}

}
