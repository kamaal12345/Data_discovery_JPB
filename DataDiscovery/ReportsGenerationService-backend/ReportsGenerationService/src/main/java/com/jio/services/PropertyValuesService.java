package com.jio.services;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jio.convertor.PropertyValuesConverter;
import com.jio.dto.PropertyValuesDto;
import com.jio.entity.PropertyValues;
import com.jio.repository.PropertyValuesRepository;

@Service
public class PropertyValuesService {

	@Autowired
	private PropertyValuesRepository propertyValuesRepository;

	@Autowired
	private PropertyValuesConverter propertyValuesConverter;

	// Create PropertyValue
	public PropertyValuesDto createPropertyValues(PropertyValues propertyValues) {
		PropertyValues saved = propertyValuesRepository.save(propertyValues);
		return propertyValuesConverter.convertToPropertyValDto(saved);
	}

	// Update PropertyValue by ID
	public PropertyValuesDto updatePropertyValuesById(Integer id, PropertyValues propertyValues) {
		PropertyValues existing = propertyValuesRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("PropertyValue not found with id: " + id));

		// Update fields
		existing.setDescription(propertyValues.getDescription());
		existing.setValue(propertyValues.getValue());
		existing.setCreatedById(propertyValues.getCreatedById());
		existing.setUpdatedById(propertyValues.getUpdatedById());
		existing.setCreatedDate(propertyValues.getCreatedDate());
		existing.setUpdatedDate(propertyValues.getUpdatedDate());
		existing.setStatus(propertyValues.getStatus());

		PropertyValues updated = propertyValuesRepository.save(existing);
		return propertyValuesConverter.convertToPropertyValDto(updated);
	}

	// Get PropertyValue by ID
	public PropertyValues getPropertyById(int propertyId) {
		return propertyValuesRepository.findById(propertyId)
				.orElseThrow(() -> new RuntimeException("PropertyValue not found with id: " + propertyId));
	}

	// Get all PropertyValues
	public List<PropertyValuesDto> getAllProperties() {
		List<PropertyValues> propertyValuesList = propertyValuesRepository.findAll();
		if (CollectionUtils.isEmpty(propertyValuesList)) {
			return Collections.emptyList();
		}
		return propertyValuesConverter.convertToPropertyValDtos(propertyValuesList);
	}
}
