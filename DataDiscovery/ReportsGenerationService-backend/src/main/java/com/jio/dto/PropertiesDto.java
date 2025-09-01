package com.jio.dto;

import java.util.List;

import lombok.Data;

@Data
public class PropertiesDto {

	private Integer id;
	private String name;
	private Integer lookupCodeSetValue;
	private Integer createdById;
	private Integer updatedById;
	private Boolean status;
	private List<PropertyValuesDto> propertyValues;

}
