package com.jio.customexception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 5638595701405354094L;
	private String errorCode;
	private String message;
}
