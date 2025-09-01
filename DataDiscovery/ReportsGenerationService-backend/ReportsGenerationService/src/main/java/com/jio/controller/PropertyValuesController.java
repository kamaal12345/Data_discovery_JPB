package com.jio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jio.dto.PropertyValuesDto;
import com.jio.entity.PropertyValues;
import com.jio.services.PropertyValuesService;

@RestController
@RequestMapping("/api/property-values")
public class PropertyValuesController {

	@Autowired
	private PropertyValuesService propertyValuesService;

	// Create property values
	@PostMapping("/add")
	public ResponseEntity<PropertyValuesDto> createPropertyValues(@RequestBody PropertyValues propertyValues) {
		return ResponseEntity.ok(propertyValuesService.createPropertyValues(propertyValues));
	}

	// View property value by ID
	@GetMapping("/{propertyId}")
	public ResponseEntity<PropertyValues> getPropertyById(@PathVariable int propertyId) {
		PropertyValues result = propertyValuesService.getPropertyById(propertyId);
		return ResponseEntity.ok(result);
	}

	// List all property values
	@GetMapping("/list")
	public ResponseEntity<List<PropertyValuesDto>> getAllProperties() {
		return ResponseEntity.ok(propertyValuesService.getAllProperties());
	}

	// Edit property values
	@PutMapping("/edit/{id}")
	public ResponseEntity<PropertyValuesDto> updatePropertyValuesById(@PathVariable Integer id,
			@RequestBody PropertyValues propertyValues) {
		PropertyValuesDto updated = propertyValuesService.updatePropertyValuesById(id, propertyValues);
		return ResponseEntity.ok(updated);
	}
}
