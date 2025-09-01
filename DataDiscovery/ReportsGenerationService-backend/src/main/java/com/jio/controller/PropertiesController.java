package com.jio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jio.dto.APIResponse;
import com.jio.dto.PropertiesDto;
import com.jio.services.PropertiesService;

@RestController
@RequestMapping("/api/properties")
public class PropertiesController {

	@Autowired
	private PropertiesService propertiesService;

	@PostMapping("/add")
	public ResponseEntity<PropertiesDto> createProperties(@RequestBody PropertiesDto propertiesdto) {
		return ResponseEntity.ok(propertiesService.createOrUpdateProperties(propertiesdto));
	}

	@PutMapping("/edit")
	public ResponseEntity<PropertiesDto> updatePropertiesById(@RequestBody PropertiesDto propertiesdto) {
		PropertiesDto updatedProperties = propertiesService.createOrUpdateProperties(propertiesdto);
		return new ResponseEntity<PropertiesDto>(updatedProperties, HttpStatus.CREATED);
	}

	@GetMapping("/properties-list")
	private APIResponse<Page<PropertiesDto>> getPropertiesList(@RequestParam(defaultValue = "0") Integer offset,
			@RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "") String field,
			@RequestParam Integer sort) {
		Page<PropertiesDto> propertiesList = propertiesService.getAllProperties(offset, pageSize, field, sort);
		return new APIResponse<>(propertiesList.getSize(), propertiesList);
	}

	@GetMapping("/{propertiesid}")
	public ResponseEntity<PropertiesDto> getPropertiesById(@PathVariable("propertiesid") int propertiesidL) {
		PropertiesDto propertiesRetrived = propertiesService.getPropertiesById(propertiesidL);
		return new ResponseEntity<PropertiesDto>(propertiesRetrived, HttpStatus.OK);
	}

	@GetMapping("/propertyValues/{lookupCodeSetValue}")
	public ResponseEntity<PropertiesDto> getByLookupCodeSetValue(
			@PathVariable("lookupCodeSetValue") int lookupCodeSetValue) {
		PropertiesDto propertyValuesRetrived = propertiesService.getByLookupCodeSetValue(lookupCodeSetValue);
		return new ResponseEntity<PropertiesDto>(propertyValuesRetrived, HttpStatus.OK);
	}

}