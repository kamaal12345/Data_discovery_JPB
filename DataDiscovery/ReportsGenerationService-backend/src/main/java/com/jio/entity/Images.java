package com.jio.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "jio_images")
public class Images {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	private String name;

	@Lob
	@Column(name = "data_url", columnDefinition = "LONGTEXT")
	private String dataUrl;

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
