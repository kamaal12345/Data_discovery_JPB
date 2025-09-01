package com.jio.dto;

import java.util.Date;

import lombok.*;

@Data
@NoArgsConstructor 
@AllArgsConstructor
@ToString
public class ImageDTO {
	private Integer id;
    private String name;
    private String dataUrl;
	private Integer createdById;
	private Integer updatedById;
	private Date createdDate;
	private Date updatedDate;	
}
