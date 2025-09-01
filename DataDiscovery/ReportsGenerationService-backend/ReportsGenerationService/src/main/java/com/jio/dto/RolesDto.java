package com.jio.dto;

import lombok.Data;

@Data
public class RolesDto {
	private Integer id;
	private String roleName;
	private Integer roleValue;
	private String description;
	private Boolean status;

}
