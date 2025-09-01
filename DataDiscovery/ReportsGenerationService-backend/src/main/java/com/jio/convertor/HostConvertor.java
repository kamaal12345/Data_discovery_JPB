package com.jio.convertor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jio.dto.HostDto;
import com.jio.entity.Host;

@Service
public class HostConvertor {

	public HostDto convertToDto(Host entity) {
		HostDto dto = new HostDto();

		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setIp(entity.getIp());
		dto.setStatus(entity.getStatus());
		dto.setCategory(entity.getCategory());
		dto.setDescription(entity.getDescription());
		dto.setCreatedById(entity.getCreatedById());
		dto.setUpdatedById(entity.getUpdatedById());
		dto.setCreatedDate(entity.getCreatedDate());
		dto.setUpdatedDate(entity.getUpdatedDate());

		return dto;
	}

	public Host convertToEntity(HostDto dto) {
		Host entity = new Host();

		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setIp(dto.getIp());
		entity.setStatus(dto.getStatus());
		entity.setCategory(dto.getCategory());
		entity.setDescription(dto.getDescription());
		entity.setCreatedById(dto.getCreatedById());
		entity.setUpdatedById(dto.getUpdatedById());
		entity.setCreatedDate(dto.getCreatedDate());
		entity.setUpdatedDate(dto.getUpdatedDate());

		return entity;
	}

	public List<HostDto> convertToDtoList(List<Host> entities) {
		List<HostDto> dtoList = new ArrayList<>();
		for (Host entity : entities) {
			dtoList.add(convertToDto(entity));
		}
		return dtoList;
	}

	public List<Host> convertToEntityList(List<HostDto> dtoList) {
		List<Host> entityList = new ArrayList<>();
		for (HostDto dto : dtoList) {
			entityList.add(convertToEntity(dto));
		}
		return entityList;
	}

	public Page<HostDto> convertToPageDto(Page<Host> entityPage, Pageable pageable) {
		List<HostDto> dtoList = convertToDtoList(entityPage.getContent());
		return new PageImpl<>(dtoList, pageable, entityPage.getTotalElements());
	}
}
