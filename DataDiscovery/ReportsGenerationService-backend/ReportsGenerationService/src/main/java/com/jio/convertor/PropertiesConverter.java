package com.jio.convertor;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jio.dto.PropertiesDto;
import com.jio.entity.Properties;
import com.jio.entity.PropertyValues;

@Service
public class PropertiesConverter {

	private final PropertyValuesConverter propertyValuesConverter;

	public PropertiesConverter(PropertyValuesConverter propertyValuesConverter) {
		this.propertyValuesConverter = propertyValuesConverter;
	}

	public PropertiesDto convertToPropertiesDto(final Properties propertiesEntity) {
		return convertToPropertiesDto(propertiesEntity, null);
	}

	// Overloaded method to convert Properties + related PropertyValues list to DTO
	public PropertiesDto convertToPropertiesDto(final Properties propertiesEntity,
			List<PropertyValues> propertyValuesList) {
		PropertiesDto propertiesDto = new PropertiesDto();
		propertiesDto.setId(propertiesEntity.getId());
		propertiesDto.setName(propertiesEntity.getName());
		propertiesDto.setLookupCodeSetValue(propertiesEntity.getLookupCodeSetValue());
		propertiesDto.setCreatedById(propertiesEntity.getCreatedById());
		propertiesDto.setUpdatedById(propertiesEntity.getUpdatedById());
		propertiesDto.setStatus(propertiesEntity.getStatus());

		if (propertyValuesList != null) {
			propertiesDto.setPropertyValues(propertyValuesConverter.convertToPropertyValDtos(propertyValuesList));
		}

		return propertiesDto;
	}

	public Properties convertToPropertiesEntity(final PropertiesDto propertiesDto) {
		Properties propertiesEntity = new Properties();
		propertiesEntity.setId(propertiesDto.getId());
		propertiesEntity.setName(propertiesDto.getName());
		propertiesEntity.setLookupCodeSetValue(propertiesDto.getLookupCodeSetValue());
		propertiesEntity.setCreatedById(propertiesDto.getCreatedById());
		propertiesEntity.setUpdatedById(propertiesDto.getUpdatedById());
		propertiesEntity.setStatus(propertiesDto.getStatus());
		return propertiesEntity;
	}

	// Optional helper method to convert list without property values
	public List<PropertiesDto> convertToPropertiesDtos(final List<Properties> propertiesEntityList) {
		return propertiesEntityList.stream().map(this::convertToPropertiesDto)
				.collect(java.util.stream.Collectors.toList());
	}
}
