package com.jio.dto;

import lombok.Data;

@Data
public class PropertyValuesDto {

	private Integer id;
	private String description;
	private Integer lookupCodeSetId;
	private Integer value;
	private Integer createdById;
	private Integer updatedById;
	private Boolean status;

}
