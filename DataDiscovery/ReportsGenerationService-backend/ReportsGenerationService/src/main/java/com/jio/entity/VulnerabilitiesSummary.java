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
@Table(name = "jio_vulnerabilities_summary")
public class VulnerabilitiesSummary {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	@Column(name = "vul_id")
	private String vulId;

	@Column(name = "severity")
	private String severity;

	@Column(name = "vulnerability", columnDefinition = "LONGTEXT")
	private String vulnerability;

	@Column(name = "affected_scope", columnDefinition = "LONGTEXT")
	private String affectedScope;

	@Column(name = "description", columnDefinition = "LONGTEXT")
	private String description;

	@Column(name = "observation", columnDefinition = "LONGTEXT")
	private String observation;

	@Column(name = "test_details", columnDefinition = "LONGTEXT")
	private String testDetails;

	@Column(name = "remediation", columnDefinition = "LONGTEXT")
	private String remediation;

	@Column(name = "`references`", columnDefinition = "LONGTEXT")
	private String references;

	@Column(name = "report_id")
	private Integer reportId;

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