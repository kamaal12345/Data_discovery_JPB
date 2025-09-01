package com.jio.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDetailsDto {
	
	private Integer reportId;
//	private String reportTitle;
//	private String reportSubTitle;
	private String scope;
	private String typeOfTesting;
	private String headerImage;
	private String headerText;
	private String footerText;
	private String applicationName;
	private Boolean assessmentType;
	private String companyName;
	private String companyLogo;
	private Integer createdById;
	private Integer updatedById;
	private Date createdDate;
	private Date updatedDate;
	private List<VulnerabilitiesSummaryDto> vulnerabilities;
	private List<VulnerabilitiesSummaryDto> duplicateVulnerabilities;
	
}
