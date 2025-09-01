package com.jio.dto;

import java.sql.Date;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserDto {

	private Integer userId;

	@NotEmpty(message = "UserName required")
	@Pattern(regexp = "[a-z A-Z 0-9]{4,45}", message = "Alphanumeric and length should be 4-45 characters")
	private String username;

	private String password;

	@Pattern(regexp = "[A-Za-z]{2,4}[0-9]{2,6}", message = "Alphanumeric and length should be 4-45 characters")
	private String employeeId;

	@NotEmpty(message = "FirstName required")
	@Pattern(regexp = "[a-z A-Z]{4,100}", message = "Only Alphabets and length should be 4-100 characters")
	private String firstName;

	@NotEmpty(message = "LastName required")
	@Pattern(regexp = "[a-z A-Z]{4,45}", message = "Only Alphabets and length should be 4-100 characters")
	private String lastName;

	@Email(message = "Invalid Email")
	@NotBlank(message = "Email required")
	private String email;

	@NotEmpty(message = "Mobile Number required")
	@Pattern(regexp = "[0-9]{10}", message = "Invalid Mobile Number")
	private String mobilePhone;

	private Integer gender;
	private Date dob;
	private Boolean status;
	private String comments;
	private String profileImg;
	private List<Integer> designation;
	private Integer department;
	private List<Integer> roleValue;
	private Boolean loggedIn;
	private Boolean metaStatus;
	private Integer createdById;
	private Integer updatedById;
	private Date createdDate;
	private Date updatedDate;

}
