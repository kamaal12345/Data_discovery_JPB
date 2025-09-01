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
public class VulnerabilitiesSummaryDto {
	private Integer id;
	private String vulId;
	private String severity;
	private String vulnerability;
	private String affectedScope;
	private String description;
	private String observation;
	private String testDetails;
	private String remediation;
	private String references;
	private Integer reportId;
	private Integer createdById;
	private Integer updatedById;
	private Date createdDate;
	private Date updatedDate;	
	
}