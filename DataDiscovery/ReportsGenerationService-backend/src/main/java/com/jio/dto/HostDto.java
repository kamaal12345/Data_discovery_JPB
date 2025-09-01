package com.jio.dto;

import java.util.Date;

import lombok.Data;

@Data
public class HostDto {
	private Integer id;
	private String name;
	private String ip;
	private Integer status;
	private Integer category;
	private String description;
	private Integer createdById;
	private Integer updatedById;
	private Date createdDate;
	private Date updatedDate;
}
