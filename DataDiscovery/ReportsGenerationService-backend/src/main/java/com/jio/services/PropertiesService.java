package com.jio.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jio.convertor.PropertiesConverter;
import com.jio.convertor.PropertyValuesConverter;
import com.jio.dto.PropertiesDto;
import com.jio.dto.PropertyValuesDto;
import com.jio.entity.Properties;
import com.jio.entity.PropertyValues;
import com.jio.repository.PropertiesRepository;
import com.jio.repository.PropertyValuesRepository;

@Service
public class PropertiesService {

    @Autowired
    private PropertiesRepository propertiesRepository;

    @Autowired
    private PropertyValuesRepository propertyValuesRepository;

    @Autowired
    private PropertiesConverter propertiesConverter;

    @Autowired
    private PropertyValuesConverter propertyValuesConverter;

    // Create or update Properties and their PropertyValues
    public PropertiesDto createOrUpdateProperties(PropertiesDto propertiesDto) {
        Properties properties = propertiesDto.getId() != null
            ? propertiesRepository.findById(propertiesDto.getId())
                .orElse(new Properties())
            : new Properties();

        properties.setName(propertiesDto.getName());
        properties.setLookupCodeSetValue(propertiesDto.getLookupCodeSetValue());
        properties.setCreatedById(propertiesDto.getCreatedById());
        properties.setUpdatedById(propertiesDto.getUpdatedById());
        properties.setStatus(propertiesDto.getStatus());

        Properties savedProperties = propertiesRepository.save(properties);

        List<PropertyValues> updatedPropertyValues = new ArrayList<>();

        if (propertiesDto.getPropertyValues() != null) {
            for (PropertyValuesDto propertyValuesDto : propertiesDto.getPropertyValues()) {
                PropertyValues propertyValue;

                if (propertyValuesDto.getId() != null) {
                    propertyValue = propertyValuesRepository.findById(propertyValuesDto.getId())
                        .orElse(new PropertyValues());
                } else {
                    propertyValue = new PropertyValues();
                }

                propertyValue.setDescription(propertyValuesDto.getDescription());
                propertyValue.setValue(propertyValuesDto.getValue());
                propertyValue.setLookupCodeSetId(savedProperties.getId());
                propertyValue.setStatus(propertyValuesDto.getStatus());
                propertyValue.setCreatedById(propertyValuesDto.getCreatedById() != null
                        ? propertyValuesDto.getCreatedById() : propertiesDto.getCreatedById());
                propertyValue.setUpdatedById(propertiesDto.getUpdatedById());

                updatedPropertyValues.add(propertyValuesRepository.save(propertyValue));
            }
        }

        // If parent is inactive, set all child values to inactive
        if (!savedProperties.getStatus()) {
            List<PropertyValues> allValues = propertyValuesRepository.findByLookupCodeSetId(savedProperties.getId());
            for (PropertyValues val : allValues) {
                val.setStatus(false);
                propertyValuesRepository.save(val);
            }
        }

        PropertiesDto responseDto = propertiesConverter.convertToPropertiesDto(savedProperties);
        responseDto.setPropertyValues(propertyValuesConverter.convertToPropertyValDtos(updatedPropertyValues));

        return responseDto;
    }

    // Get paged list of Properties with their PropertyValues
    public Page<PropertiesDto> getAllProperties(Integer offset, Integer pageSize, String field, Integer sort) {
        Direction direction = (sort != null && sort == 0) ? Direction.DESC : Direction.ASC;
        String sortBy = StringUtils.hasText(field) ? field : "name";
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));

        Page<Properties> propertiesPage = propertiesRepository.findAll(pageable);
        List<PropertiesDto> dtoList = new ArrayList<>();

        for (Properties prop : propertiesPage.getContent()) {
            PropertiesDto dto = propertiesConverter.convertToPropertiesDto(prop);

            List<PropertyValues> values = propertyValuesRepository.findByLookupCodeSetId(prop.getId());
            List<PropertyValuesDto> valuesDto = propertyValuesConverter.convertToPropertyValDtos(values);

            dto.setPropertyValues(valuesDto);

            dtoList.add(dto);
        }

        return new PageImpl<>(dtoList, pageable, propertiesPage.getTotalElements());
    }

    // Get a single PropertiesDto by id, including propertyValues
    public PropertiesDto getPropertiesById(int id) {
        Properties properties = propertiesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Properties not found with ID: " + id));

        PropertiesDto dto = propertiesConverter.convertToPropertiesDto(properties);

        List<PropertyValues> values = propertyValuesRepository.findByLookupCodeSetId(id);
        List<PropertyValuesDto> valuesDto = propertyValuesConverter.convertToPropertyValDtos(values);

        dto.setPropertyValues(valuesDto);

        return dto;
    }

    // Get PropertiesDto by lookupCodeSetValue, including propertyValues
    public PropertiesDto getByLookupCodeSetValue(int lookupCodeSetValue) {
        Properties properties = propertiesRepository.getIdBylookupCodeSetValue(lookupCodeSetValue);

        if (properties == null) {
            throw new RuntimeException("No properties found for lookupCodeSetValue: " + lookupCodeSetValue);
        }

        PropertiesDto dto = propertiesConverter.convertToPropertiesDto(properties);

        List<PropertyValues> values = propertyValuesRepository.findByLookupCodeSetId(properties.getId());
        List<PropertyValuesDto> valuesDto = propertyValuesConverter.convertToPropertyValDtos(values);

        dto.setPropertyValues(valuesDto);

        return dto;
    }
}
