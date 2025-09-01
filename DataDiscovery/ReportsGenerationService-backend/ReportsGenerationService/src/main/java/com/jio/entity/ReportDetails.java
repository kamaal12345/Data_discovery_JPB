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
@Table(name = "jio_report_details")
public class ReportDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "report_id", unique = true, nullable = false)
	private Integer reportId;

//	@Column(name = "report_title")
//	private String reportTitle;
//
//	@Column(name = "report_sub_title")
//	private String reportSubTitle;

	@Column(name = "scope")
	private String scope;

	@Column(name = "type_of_testing")
	private String typeOfTesting;

	@Column(name = "header_image", columnDefinition = "LONGTEXT")
	private String headerImage;

	@Column(name = "header_text")
	private String headerText;

	@Column(name = "footer_text")
	private String footerText;

	@Column(name = "application_name")
	private String applicationName;

	@Column(name = "assessment_type")
	private Boolean assessmentType;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "company_logo", columnDefinition = "LONGTEXT")
	private String companyLogo;

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
