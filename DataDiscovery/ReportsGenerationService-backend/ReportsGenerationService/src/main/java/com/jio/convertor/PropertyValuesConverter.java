package com.jio.convertor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jio.dto.PropertyValuesDto;
import com.jio.entity.PropertyValues;

@Service
public class PropertyValuesConverter {

	public PropertyValuesDto convertToPropertyValDto(final PropertyValues propertyValEntity) {
		PropertyValuesDto propertyValuesDto = new PropertyValuesDto();
		propertyValuesDto.setId(propertyValEntity.getId());
		propertyValuesDto.setDescription(propertyValEntity.getDescription());
		propertyValuesDto.setValue(propertyValEntity.getValue());
		propertyValuesDto.setLookupCodeSetId(propertyValEntity.getLookupCodeSetId());
		propertyValuesDto.setCreatedById(propertyValEntity.getCreatedById());
		propertyValuesDto.setUpdatedById(propertyValEntity.getUpdatedById());
		propertyValuesDto.setStatus(propertyValEntity.getStatus());
		return propertyValuesDto;
	}

	public PropertyValues convertToPropertyValEntity(final PropertyValuesDto propertyValDto) {
		PropertyValues propertyValuesEntity = new PropertyValues();
		propertyValuesEntity.setId(propertyValDto.getId());
		propertyValuesEntity.setDescription(propertyValDto.getDescription());
		propertyValuesEntity.setValue(propertyValDto.getValue());
		propertyValuesEntity.setCreatedById(propertyValDto.getCreatedById());
		propertyValuesEntity.setUpdatedById(propertyValDto.getUpdatedById());
		propertyValuesEntity.setLookupCodeSetId(propertyValDto.getLookupCodeSetId());
		propertyValuesEntity.setStatus(propertyValDto.getStatus());
		return propertyValuesEntity;
	}

	public List<PropertyValuesDto> convertToPropertyValDtos(final List<PropertyValues> propertyValEntityList) {
		return propertyValEntityList.stream().map(this::convertToPropertyValDto).collect(Collectors.toList());
	}
}
