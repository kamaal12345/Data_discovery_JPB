package com.jio.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class PiiScanRequestDto {

	private Long requestId;
	private String serverType;
	private String targetName;
	private Long maxFileSize; // by Default 10 Mb
	private Integer stopScannAfter;
	private String filePath;
	private List<String> piiTypes;
	private String excludePatterns;
	private Integer createdById;
	private Integer updatedById;
	private Date createdDate;
	private Date updatedDate;
	private List<PiiScanResultDto> piiScanResults;

}
