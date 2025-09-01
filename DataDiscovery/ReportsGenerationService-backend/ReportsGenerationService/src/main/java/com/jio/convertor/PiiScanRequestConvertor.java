package com.jio.convertor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jio.dto.PiiScanRequestDto;
import com.jio.dto.PiiScanResultDto;
import com.jio.entity.PiiScanRequest;
import com.jio.entity.PiiScanResult;

@Service
public class PiiScanRequestConvertor {

	// Convert PiiScanRequest entity to PiiScanRequestDto
	public static PiiScanRequestDto convertToPiiScanRequestDTO(PiiScanRequest entity) {
		PiiScanRequestDto dto = new PiiScanRequestDto();

		dto.setRequestId(entity.getRequestId());
		dto.setServerType(entity.getServerType());
		dto.setTargetName(entity.getTargetName());
		dto.setMaxFileSize(entity.getMaxFileSize());
		dto.setStopScannAfter(entity.getStopScannAfter());
		dto.setFilePath(entity.getFilePath());
		dto.setPiiTypes(entity.getPiiTypes());
		dto.setExcludePatterns(entity.getExcludePatterns());
		dto.setCreatedById(entity.getCreatedById());
		dto.setUpdatedById(entity.getUpdatedById());
		dto.setCreatedDate(entity.getCreatedDate());
		dto.setUpdatedDate(entity.getUpdatedDate());
//		dto.setPiiScanResults(
//			    PiiScanResultConvertor.convertToPiiScanResultDTOList(entity.getPiiScanResults()));
		dto.setPiiScanResults(null);

		return dto;
	}

	// Convert PiiScanRequestDto to PiiScanRequest entity
	public static PiiScanRequest convertToPiiScanRequestEntity(PiiScanRequestDto dto) {
		PiiScanRequest entity = new PiiScanRequest();

		entity.setRequestId(dto.getRequestId());
		entity.setServerType(dto.getServerType());
		entity.setTargetName(dto.getTargetName());
		entity.setMaxFileSize(dto.getMaxFileSize());
		entity.setStopScannAfter(dto.getStopScannAfter());
		entity.setFilePath(dto.getFilePath());
		entity.setPiiTypes(dto.getPiiTypes());
		entity.setExcludePatterns(dto.getExcludePatterns());
		entity.setCreatedById(dto.getCreatedById());
		entity.setUpdatedById(dto.getUpdatedById());
		entity.setCreatedDate(dto.getCreatedDate());
		entity.setUpdatedDate(dto.getUpdatedDate());

		List<PiiScanResultDto> resultDtos = dto.getPiiScanResults();
		List<PiiScanResult> resultEntities = PiiScanResultConvertor.convertToPiiScanResultList(
		    resultDtos != null ? resultDtos : new ArrayList<>()
		);

		for (PiiScanResult result : resultEntities) {
		    result.setPiiScanRequest(entity);
		}
		entity.setPiiScanResults(resultEntities);

		return entity;
	}

	// Convert a list of PiiScanRequest entities to a list of PiiScanRequestDto
	public static List<PiiScanRequestDto> convertToPiiScanRequestDTOList(List<PiiScanRequest> entities) {
		List<PiiScanRequestDto> dtoList = new ArrayList<>();
		for (PiiScanRequest entity : entities) {
			dtoList.add(convertToPiiScanRequestDTO(entity));
		}
		return dtoList;
	}

	// Convert a list of PiiScanRequestDto to a list of PiiScanRequest entities
	public static List<PiiScanRequest> convertToPiiScanRequestList(List<PiiScanRequestDto> dtoList) {
		List<PiiScanRequest> entityList = new ArrayList<>();
		for (PiiScanRequestDto dto : dtoList) {
			entityList.add(convertToPiiScanRequestEntity(dto));
		}
		return entityList;
	}

	// Convert Page<PiiScanRequest> to Page<PiiScanRequestDto>
	public static Page<PiiScanRequestDto> convertToPiiScanRequestDTOPage(Page<PiiScanRequest> entities,
			Pageable pageable) {
		List<PiiScanRequestDto> dtoList = convertToPiiScanRequestDTOList(entities.getContent());
		return new PageImpl<>(dtoList, pageable, entities.getTotalElements());
	}

	// Convert Page<PiiScanRequestDto> to Page<PiiScanRequest> (if needed)
	public static Page<PiiScanRequest> convertToPiiScanRequestPage(Page<PiiScanRequestDto> dtoPage, Pageable pageable) {
		List<PiiScanRequest> entityList = convertToPiiScanRequestList(dtoPage.getContent());
		return new PageImpl<>(entityList, pageable, dtoPage.getTotalElements());
	}
}
