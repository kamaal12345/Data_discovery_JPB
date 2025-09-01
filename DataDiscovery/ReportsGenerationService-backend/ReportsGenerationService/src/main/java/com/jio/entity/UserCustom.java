package com.jio.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCustom {
	private Integer userId;
	private String employeeId;
	private String firstName;
	private String lastName;

}