package com.jio.entity;

import java.sql.Date;

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
@Table(name = "jio_roles")
public class Roles {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Integer id;

	@Column(name = "role_name", unique = true, nullable = false)
	private String roleName;

	@Column(name = "description")
	private String description;

	@Column(name = "status", nullable = false)
	private Boolean status;

	@Column(name = "created_by_id", nullable = false)
	private Integer createdById;

	@Column(name = "updated_by_id", nullable = false)
	private Integer updatedById;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "updated_date")
	private Date updatedDate;

	@Column(name = "role_value")
	private Integer roleValue;

}
