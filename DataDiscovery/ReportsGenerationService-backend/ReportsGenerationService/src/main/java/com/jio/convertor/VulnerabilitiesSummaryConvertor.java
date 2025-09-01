package com.jio.convertor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jio.dto.VulnerabilitiesSummaryDto;
import com.jio.entity.VulnerabilitiesSummary;

@Service
public class VulnerabilitiesSummaryConvertor {

	public static VulnerabilitiesSummaryDto convertToVulnerabilityDTO(VulnerabilitiesSummary entity) {
		VulnerabilitiesSummaryDto dto = new VulnerabilitiesSummaryDto();

		dto.setId(entity.getId());
		dto.setVulId(entity.getVulId());
		dto.setSeverity(entity.getSeverity());
		dto.setVulnerability(entity.getVulnerability());
		dto.setAffectedScope(entity.getAffectedScope());
		dto.setDescription(entity.getDescription());
		dto.setObservation(entity.getObservation());
		dto.setTestDetails(entity.getTestDetails());
		dto.setRemediation(entity.getRemediation());
		dto.setReferences(entity.getReferences());
		dto.setReportId(entity.getReportId());
		dto.setCreatedById(entity.getCreatedById());
		dto.setUpdatedById(entity.getUpdatedById());
		dto.setCreatedDate(entity.getCreatedDate());
		dto.setUpdatedDate(entity.getUpdatedDate());
		return dto;
	}

	public static VulnerabilitiesSummary convertToVulnerabilityEntity(VulnerabilitiesSummaryDto dto) {
		VulnerabilitiesSummary entity = new VulnerabilitiesSummary();

		entity.setId(dto.getId());
		entity.setVulId(dto.getVulId());
		entity.setSeverity(dto.getSeverity());
		entity.setVulnerability(dto.getVulnerability());
		entity.setAffectedScope(dto.getAffectedScope());
		entity.setDescription(dto.getDescription());
		entity.setObservation(dto.getObservation());
		entity.setTestDetails(dto.getTestDetails());
		entity.setRemediation(dto.getRemediation());
		entity.setReferences(dto.getReferences());
		entity.setReportId(dto.getReportId());
		entity.setCreatedById(dto.getCreatedById());
		entity.setUpdatedById(dto.getUpdatedById());
		entity.setCreatedDate(dto.getCreatedDate());
		entity.setUpdatedDate(dto.getUpdatedDate());
		return entity;
	}

	public static List<VulnerabilitiesSummaryDto> convertToVulnerabilityDTOList(List<VulnerabilitiesSummary> entities) {
		List<VulnerabilitiesSummaryDto> dtoList = new ArrayList<>();

		for (VulnerabilitiesSummary entity : entities) {
			dtoList.add(convertToVulnerabilityDTO(entity));
		}

		return dtoList;
	}

	public static List<VulnerabilitiesSummary> convertToVulnerabilityList(List<VulnerabilitiesSummaryDto> dtoList) {
		List<VulnerabilitiesSummary> entityList = new ArrayList<>();
		for (VulnerabilitiesSummaryDto dto : dtoList) {
			entityList.add(convertToVulnerabilityEntity(dto));
		}
		return entityList;
	}

	public static Page<VulnerabilitiesSummaryDto> convertToVulnerabilityDTOPage(Page<VulnerabilitiesSummary> entities,
			Pageable pageable) {
		List<VulnerabilitiesSummaryDto> dtoList = convertToVulnerabilityDTOList(entities.getContent());
		return new PageImpl<>(dtoList, pageable, entities.getTotalElements());
	}
}
