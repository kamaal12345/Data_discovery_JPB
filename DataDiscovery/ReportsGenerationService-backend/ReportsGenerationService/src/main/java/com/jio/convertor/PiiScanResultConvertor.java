package com.jio.convertor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jio.dto.PiiScanResultDto;
import com.jio.entity.PiiScanResult;

@Service
public class PiiScanResultConvertor {

	// Convert PiiScanResult entity to DTO
	public static PiiScanResultDto convertToPiiScanResultDTO(PiiScanResult entity) {
		PiiScanResultDto dto = new PiiScanResultDto();

		dto.setResultId(entity.getResultId());
		dto.setPiiType(entity.getPiiType());
		dto.setFilePath(entity.getFilePath());
		dto.setMatchedData(entity.getMatchedData());
		dto.setIp(entity.getIp());
		dto.setCreatedById(entity.getCreatedById());
		dto.setUpdatedById(entity.getUpdatedById());
		dto.setCreatedDate(entity.getCreatedDate());
		dto.setUpdatedDate(entity.getUpdatedDate());

		return dto;
	}

	// Convert PiiScanResult DTO to entity
	public static PiiScanResult convertToPiiScanResultEntity(PiiScanResultDto dto) {
		PiiScanResult entity = new PiiScanResult();

		entity.setResultId(dto.getResultId());
		entity.setPiiType(dto.getPiiType());
		entity.setFilePath(dto.getFilePath());
		entity.setMatchedData(dto.getMatchedData());
		entity.setIp(dto.getIp());
		entity.setCreatedById(dto.getCreatedById());
		entity.setUpdatedById(dto.getUpdatedById());
		entity.setCreatedDate(dto.getCreatedDate());
		entity.setUpdatedDate(dto.getUpdatedDate());

		return entity;
	}

	// Convert List of PiiScanResult entities to List of DTOs
	public static List<PiiScanResultDto> convertToPiiScanResultDTOList(List<PiiScanResult> entities) {
		List<PiiScanResultDto> dtoList = new ArrayList<>();

		for (PiiScanResult entity : entities) {
			dtoList.add(convertToPiiScanResultDTO(entity));
		}

		return dtoList;
	}

	// Convert List of PiiScanResult DTOs to List of entities
	public static List<PiiScanResult> convertToPiiScanResultList(List<PiiScanResultDto> dtoList) {
	    List<PiiScanResult> entityList = new ArrayList<>();
	    if (dtoList != null) {
	        for (PiiScanResultDto dto : dtoList) {
	            entityList.add(convertToPiiScanResultEntity(dto));
	        }
	    }
	    return entityList;
	}

}
