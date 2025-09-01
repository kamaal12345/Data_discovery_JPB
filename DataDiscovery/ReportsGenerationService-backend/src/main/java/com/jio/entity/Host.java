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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "jio_hosts", uniqueConstraints = @UniqueConstraint(columnNames = "ip"))
public class Host {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
    private Integer id;

    private String name;

    @Column(nullable = false, unique = true)
    private String ip;

    private Integer status;

    private Integer category;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

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
