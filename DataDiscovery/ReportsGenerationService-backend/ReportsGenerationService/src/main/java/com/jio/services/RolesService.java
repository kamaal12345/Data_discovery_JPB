package com.jio.services;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jio.convertor.RolesConverter;
import com.jio.dto.RolesDto;
import com.jio.entity.Roles;
import com.jio.repository.RolesRepository;

@Service
public class RolesService {
	@Autowired
	private RolesRepository rolesRepository;

	@Autowired
	private RolesConverter rolesConverter;
	
	public List<RolesDto> getAllRoles() {
		List<Roles> roles = rolesRepository.findAll();
		if (!CollectionUtils.isEmpty(roles))
			return rolesConverter.convertToDtos(roles);

		return Collections.emptyList();
	}

}
