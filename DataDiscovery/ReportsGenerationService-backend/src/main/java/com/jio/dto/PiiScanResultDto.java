package com.jio.dto;

import java.util.Date;

import lombok.Data;

@Data
public class PiiScanResultDto {
	private Long resultId;
	private String piiType;
	private String filePath;
	private String matchedData;
	private Integer createdById;
	private Integer updatedById;
	private Date createdDate;
	private Date updatedDate;
	private String ip;
}
