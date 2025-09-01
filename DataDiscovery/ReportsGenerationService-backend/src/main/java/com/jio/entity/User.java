package com.jio.entity;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "jio_user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
	private Integer userId;

	@Column(name = "user_name", unique = true, nullable = false)
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "employee_id", unique = true, nullable = false)
	private String employeeId;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "email_primary", unique = true, nullable = false)
	private String email;

	@Column(name = "mobile_phone", unique = true, nullable = false)
	private String mobilePhone;

	@Column(name = "gender")
	private Integer gender;

	@Column(name = "date_of_birth")
	private Date dob;

	@Column(name = "status", columnDefinition = "TINYINT(1)", nullable = false)
	private Boolean status;

	@Column(name = "comments")
	private String comments;

	@Lob
	@Column(name = "profile_img", columnDefinition = "LONGTEXT")
	private String profileImg;

	@Column(name = "designation", columnDefinition = "json")
	@Convert(converter = com.jio.convertor.JsonIntegerListConverter.class)
	private List<Integer> designation;

	@Column(name = "department")
	private Integer department;

	@Column(name = "is_logged_in")
	private Boolean loggedIn;

	@Column(name = "metastatus", nullable = false)
	private Boolean metaStatus;

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

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "jio_user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Roles> roles = new HashSet<>();

//	@Column(name = "shift")
//	private Integer shift;
//
//	@Column(name = "working_country")
//	private Integer workingCountry;
//
//	@Column(name = "working_state")
//	private Integer workingState;
//
//	@Column(name = "reporting_manager")
//	private Integer reportingManager;
//
//	@Column(name = "location")
//	private Integer location;
//
//	@Column(name = "branch")
//	private Integer branch;
//
//	@Column(name = "job_title")
//	private String jobTitle;
//	
//	@Column(name = "work_mode")
//	private Integer workMode;
//	
//	@Column(name = "wm_start_date")
//	private Date wmStartDate;
//	
//	@Column(name = "wm_end_date")
//	private Date wmEndDate;
//
//	@Column(name = "office_days")
//	private Date officeDays;
//	
//	@Column(name = "home_days")
//	private Integer homeDays;
//
//	@Column(name = "grade")
//	private Integer grade;

}