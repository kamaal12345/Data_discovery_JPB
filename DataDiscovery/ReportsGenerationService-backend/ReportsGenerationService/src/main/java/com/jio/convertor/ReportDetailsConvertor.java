package com.jio.convertor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jio.dto.ReportDetailsDto;
import com.jio.entity.ReportDetails;
import com.jio.entity.VulnerabilitiesSummary;

@Service
public class ReportDetailsConvertor {

	public static ReportDetailsDto convertToReportDetailsDto(final ReportDetails reportDetailsEntity) {
		ReportDetailsDto reportDetailsDto = new ReportDetailsDto();
		reportDetailsDto.setReportId(reportDetailsEntity.getReportId());
//	    reportDetailsDto.setReportTitle(reportDetailsEntity.getReportTitle());
//	    reportDetailsDto.setReportSubTitle(reportDetailsEntity.getReportSubTitle());
		reportDetailsDto.setScope(reportDetailsEntity.getScope());
		reportDetailsDto.setTypeOfTesting(reportDetailsEntity.getTypeOfTesting());
		reportDetailsDto.setHeaderImage(reportDetailsEntity.getHeaderImage());
		reportDetailsDto.setHeaderText(reportDetailsEntity.getHeaderText());
		reportDetailsDto.setFooterText(reportDetailsEntity.getFooterText());
		reportDetailsDto.setApplicationName(reportDetailsEntity.getApplicationName());
		reportDetailsDto.setAssessmentType(reportDetailsEntity.getAssessmentType());
		reportDetailsDto.setCompanyName(reportDetailsEntity.getCompanyName());
		reportDetailsDto.setCompanyLogo(reportDetailsEntity.getCompanyLogo());
		reportDetailsDto.setCreatedById(reportDetailsEntity.getCreatedById());
		reportDetailsDto.setUpdatedById(reportDetailsEntity.getUpdatedById());
		reportDetailsDto.setCreatedDate(reportDetailsEntity.getCreatedDate());
		reportDetailsDto.setUpdatedDate(reportDetailsEntity.getUpdatedDate());
		return reportDetailsDto;
	}

	public static ReportDetails convertToReportDetailsEntity(final ReportDetailsDto reportDetailsDto) {
		ReportDetails reportDetailsEntity = new ReportDetails();
		reportDetailsEntity.setReportId(reportDetailsDto.getReportId());
//	    reportDetailsEntity.setReportTitle(reportDetailsDto.getReportTitle());
//	    reportDetailsEntity.setReportSubTitle(reportDetailsDto.getReportSubTitle());
		reportDetailsEntity.setScope(reportDetailsDto.getScope());
		reportDetailsEntity.setTypeOfTesting(reportDetailsDto.getTypeOfTesting());
		reportDetailsEntity.setHeaderImage(reportDetailsDto.getHeaderImage());
		reportDetailsEntity.setHeaderText(reportDetailsDto.getHeaderText());
		reportDetailsEntity.setFooterText(reportDetailsDto.getFooterText());
		reportDetailsEntity.setApplicationName(reportDetailsDto.getApplicationName());
		reportDetailsEntity.setAssessmentType(reportDetailsDto.getAssessmentType());
		reportDetailsEntity.setCompanyName(reportDetailsDto.getCompanyName());
		reportDetailsEntity.setCompanyLogo(reportDetailsDto.getCompanyLogo());
		reportDetailsEntity.setCreatedById(reportDetailsDto.getCreatedById());
		reportDetailsEntity.setUpdatedById(reportDetailsDto.getUpdatedById());
		reportDetailsEntity.setCreatedDate(reportDetailsDto.getCreatedDate());
		reportDetailsEntity.setUpdatedDate(reportDetailsDto.getUpdatedDate());
		return reportDetailsEntity;
	}

	public static List<ReportDetailsDto> convertToReportDetailsDtos(final List<ReportDetails> reportDetailsEntities) {
		List<ReportDetailsDto> dtoList = new ArrayList<>();
		reportDetailsEntities.parallelStream().forEach(entity -> dtoList.add(convertToReportDetailsDto(entity)));
		return dtoList;
	}

	public static Page<ReportDetailsDto> toReportDetailsDTOPageList(Page<ReportDetails> reportDetailsPage,
			Pageable pageable, Map<Integer, List<VulnerabilitiesSummary>> vulnerabilitiesGrouped) {
		List<ReportDetailsDto> dtoList = reportDetailsPage.getContent().stream()
				.map(entity -> toDto(entity,
						vulnerabilitiesGrouped.getOrDefault(entity.getReportId(), Collections.emptyList())))
				.collect(Collectors.toList());

		return new PageImpl<>(dtoList, pageable, reportDetailsPage.getTotalElements());
	}

	public static ReportDetailsDto toDto(ReportDetails entity, List<VulnerabilitiesSummary> vulnerabilities) {
		ReportDetailsDto dto = new ReportDetailsDto();
		dto.setReportId(entity.getReportId());
//	    dto.setReportTitle(entity.getReportTitle());
//	    dto.setReportSubTitle(entity.getReportSubTitle());
		dto.setScope(entity.getScope());
		dto.setTypeOfTesting(entity.getTypeOfTesting());
		dto.setHeaderImage(entity.getHeaderImage());
		dto.setHeaderText(entity.getHeaderText());
		dto.setFooterText(entity.getFooterText());
		dto.setApplicationName(entity.getApplicationName());
		dto.setAssessmentType(entity.getAssessmentType());
		dto.setCompanyName(entity.getCompanyName());
		dto.setCompanyLogo(entity.getCompanyLogo());
		dto.setCreatedById(entity.getCreatedById());
		dto.setUpdatedById(entity.getUpdatedById());
		dto.setCreatedDate(entity.getCreatedDate());
		dto.setUpdatedDate(entity.getUpdatedDate());
		dto.setVulnerabilities(VulnerabilitiesSummaryConvertor.convertToVulnerabilityDTOList(vulnerabilities));

		return dto;
	}

}
