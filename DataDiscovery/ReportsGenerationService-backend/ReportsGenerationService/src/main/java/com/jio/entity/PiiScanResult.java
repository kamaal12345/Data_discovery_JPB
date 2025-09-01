package com.jio.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "jio_pii_scan_result")
public class PiiScanResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "result_id", unique = true, nullable = false)
	private Long resultId;

	@Column(name = "file_path")
	private String filePath;

	@Column(name = "matched_data")
	private String matchedData;

//	@Column(name = "pii_types", columnDefinition = "json")
//	@Convert(converter = JsonStringListConverter.class)
	@Column(name = "pii_type")

	private String piiType;

	// Add Many-to-One relationship to PiiScanRequest
	@ManyToOne(fetch = FetchType.LAZY)

	@JoinColumn(name = "request_id")

	@JsonIgnore

	private PiiScanRequest piiScanRequest;
	@JsonProperty("requestId")
	public Long getRequestIdOnly() {
		return piiScanRequest != null ? piiScanRequest.getRequestId() : null;
	}

	private String ip;
	
	@Column(name = "created_by_id")
	private Integer createdById;
	
	@Column(name = "updated_by_id")
	private Integer updatedById;

	@CreationTimestamp
	@Column(name = "created_date")

	private Date createdDate;

	@UpdateTimestamp
	@Column(name = "updated_date")

	private Date updatedDate;

	public PiiScanResult(String filePath, String piiType, String matchedData, String ip) {
		this.filePath = filePath;
		this.piiType = piiType;
		this.matchedData = matchedData;
		this.ip = ip;
	}

}
