package com.jio.entity;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jio.convertor.JsonStringListConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "jio_pii_scan_request")
public class PiiScanRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "request_id", unique = true, nullable = false)
	private Long requestId;
	
	@Column(name = "server_type")
	private String serverType;
	
	@Column(name = "target_name")
	private String targetName;

	@Column(name = "max_file_size")
	private Long maxFileSize;
	
	@Column(name = "stop_Scann_After")
	private Integer stopScannAfter;

	@Column(name = "file_path")
	private String filePath;

	@Column(name = "pii_types", columnDefinition = "json")
	@Convert(converter = JsonStringListConverter.class)
	private List<String> piiTypes;

	@Column(name = "exclude_patterns")
	private String excludePatterns;

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

	@OneToMany(mappedBy = "piiScanRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PiiScanResult> piiScanResults;

}
