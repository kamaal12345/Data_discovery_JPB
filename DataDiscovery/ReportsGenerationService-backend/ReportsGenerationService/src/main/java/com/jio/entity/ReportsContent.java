package com.jio.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "jio_reports_content")
public class ReportsContent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	@Column(columnDefinition = "LONGTEXT")
	private String disclaimer;

	@Column(name = "confi_and_prop_text", columnDefinition = "LONGTEXT")
	private String confiAndPropText;

	@Column(name = "report_analysis", columnDefinition = "LONGTEXT")
	private String reportAnalysis;

	@Column(name = "excutive_summary", columnDefinition = "LONGTEXT")
	private String executiveSummary;

	@Column(name = "progress_report", columnDefinition = "LONGTEXT")
	private String progressReport;

	@Column(name = "methodology_text", columnDefinition = "LONGTEXT")
	private String methodologyText;

	@Column(name = "methodology_image", columnDefinition = "LONGTEXT")
	private String methodologyImage;

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
}
