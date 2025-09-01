package com.jio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jio.dto.RolesDto;
import com.jio.services.RolesService;

@RestController
@RequestMapping("/api/roles")
public class RolesController {

	@Autowired
	private RolesService rolesService;

	// roles list
	@GetMapping("/roles-list")
	public ResponseEntity<?> getAllroles(
			@RequestHeader(name = "Authorization", required = true) final String authorization) {
		List<RolesDto> listRoles = rolesService.getAllRoles();
		return ResponseEntity.ok(listRoles);

	}

}
