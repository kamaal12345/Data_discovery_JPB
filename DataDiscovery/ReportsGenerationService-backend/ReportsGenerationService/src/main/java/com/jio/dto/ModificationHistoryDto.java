package com.jio.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModificationHistoryDto {
	private Integer id;
	private String version;
	private Date date;
	private String author;
	private String description;
	private Integer reportId;
	private Integer createdById;
	private Integer updatedById;
	private Date createdDate;
	private Date updatedDate;
}