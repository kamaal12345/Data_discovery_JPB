package com.jio.convertor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jio.dto.ImageDTO;
import com.jio.entity.Images;

@Service
public class ImageConverter {

	public static Images toEntity(ImageDTO dto) {
		Images entity = new Images();
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setDataUrl(dto.getDataUrl());
		dto.setCreatedById(entity.getCreatedById());
		dto.setUpdatedById(entity.getUpdatedById());
		entity.setCreatedDate(dto.getCreatedDate());
		entity.setUpdatedDate(dto.getUpdatedDate());
		return entity;
	}

	public static ImageDTO toDTO(Images entity) {
		return new ImageDTO(entity.getId(), entity.getName(), entity.getDataUrl(), entity.getCreatedById(), entity.getUpdatedById(), entity.getCreatedDate(),
				entity.getUpdatedDate());
	}

	public static List<Images> toEntityList(List<ImageDTO> dtoList) {
		return dtoList.stream().map(ImageConverter::toEntity).collect(Collectors.toList());
	}

	public static List<ImageDTO> toDTOList(List<Images> entityList) {
		return entityList.stream().map(ImageConverter::toDTO).collect(Collectors.toList());
	}

}
