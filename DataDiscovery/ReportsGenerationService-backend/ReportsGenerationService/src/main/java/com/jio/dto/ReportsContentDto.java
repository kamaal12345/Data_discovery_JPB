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
public class ReportsContentDto {

	private Integer id;
	private String disclaimer;
	private String confiAndPropText;
	private String reportAnalysis;
	private String executiveSummary;
	private String progressReport;
	private String methodologyText;
	private String methodologyImage;
	private Integer createdId;
	private Integer updatedId;
	private Date createdDate;
	private Date updatedDate;
}
