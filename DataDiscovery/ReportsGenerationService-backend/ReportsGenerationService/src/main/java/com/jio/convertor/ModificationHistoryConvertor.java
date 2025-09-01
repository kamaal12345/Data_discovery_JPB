package com.jio.convertor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jio.dto.ModificationHistoryDto;
import com.jio.entity.ModificationHistory;

@Service
public class ModificationHistoryConvertor {
	public static ModificationHistoryDto toDTO(ModificationHistory entity) {
	    return ModificationHistoryDto.builder()
	            .id(entity.getId())
	            .version(entity.getVersion())
	            .date(entity.getDate())
	            .author(entity.getAuthor())
	            .description(entity.getDescription())
	            .reportId(entity.getReportId())
	            .createdById(entity.getCreatedById())
	            .updatedById(entity.getUpdatedById())
	            .createdDate(entity.getCreatedDate())
	            .updatedDate(entity.getUpdatedDate())
	            .build();
	}

	public static ModificationHistory toEntity(ModificationHistoryDto dto) {
	    return ModificationHistory.builder()
	            .id(dto.getId())
	            .version(dto.getVersion())
	            .date(dto.getDate())
	            .author(dto.getAuthor())
	            .description(dto.getDescription())
	            .reportId(dto.getReportId())
	            .createdById(dto.getCreatedById())
	            .updatedById(dto.getUpdatedById())
	            .createdDate(dto.getCreatedDate())
	            .updatedDate(dto.getUpdatedDate())
	            .build();
	}

	public static List<ModificationHistoryDto> toModificationDTOList(List<ModificationHistory> entities) {
	    return entities.stream().map(ModificationHistoryConvertor::toDTO).collect(Collectors.toList());
	}

	public static List<ModificationHistory> toModificationList(List<ModificationHistoryDto> Dto) {
	    return Dto.stream().map(ModificationHistoryConvertor::toEntity).collect(Collectors.toList());
	}


}
