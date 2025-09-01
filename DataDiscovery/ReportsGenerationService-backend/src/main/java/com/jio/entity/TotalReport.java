package com.jio.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalReport {
	
	private ReportDetails reportDetails;
	private List<ModificationHistory>modificationHistories;
	private List<VulnerabilitiesSummary>vulnerabilitiesSummaries;

}
