package com.jio.convertor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jio.dto.ReportsContentDto;
import com.jio.entity.ReportsContent;

@Service
public class ReportsContentConvertor {

	public static ReportsContentDto toDto(ReportsContent entity) {
		return ReportsContentDto.builder().id(entity.getId()).disclaimer(entity.getDisclaimer())
				.confiAndPropText(entity.getConfiAndPropText()).reportAnalysis(entity.getReportAnalysis())
				.executiveSummary(entity.getExecutiveSummary()).progressReport(entity.getProgressReport())
				.methodologyText(entity.getMethodologyText()).methodologyImage(entity.getMethodologyImage()).build();
	}

	public static ReportsContent toEntity(ReportsContentDto dto) {
		return ReportsContent.builder().id(dto.getId()).disclaimer(dto.getDisclaimer())
				.confiAndPropText(dto.getConfiAndPropText()).reportAnalysis(dto.getReportAnalysis())
				.executiveSummary(dto.getExecutiveSummary()).progressReport(dto.getProgressReport())
				.methodologyText(dto.getMethodologyText()).methodologyImage(dto.getMethodologyImage()).build();
	}

	public static List<ReportsContentDto> toDtoList(List<ReportsContent> entities) {
		return entities.stream().map(ReportsContentConvertor::toDto).collect(Collectors.toList());
	}

	public static List<ReportsContent> toEntityList(List<ReportsContentDto> dtos) {
		return dtos.stream().map(ReportsContentConvertor::toEntity).collect(Collectors.toList());
	}
}
