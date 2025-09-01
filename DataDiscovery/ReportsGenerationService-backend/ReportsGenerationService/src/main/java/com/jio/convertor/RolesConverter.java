package com.jio.convertor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jio.dto.RolesDto;
import com.jio.entity.Roles;

@Service
public class RolesConverter {

	public RolesDto convertToDto(final Roles rolesEntity) {
		RolesDto rolesDto = new RolesDto();
		rolesDto.setId(rolesEntity.getId());
		rolesDto.setRoleValue(rolesEntity.getRoleValue());
		rolesDto.setRoleName(rolesEntity.getRoleName());
		rolesDto.setDescription(rolesEntity.getDescription());
		rolesDto.setStatus(rolesEntity.getStatus());
		return rolesDto;
	}

	public Roles convertToEntity(final RolesDto rolesDto) {
		Roles rolesEntity = new Roles();
		rolesEntity.setId(rolesDto.getId());
		rolesEntity.setRoleName(rolesDto.getRoleName());
		rolesEntity.setRoleValue(rolesDto.getRoleValue());
		rolesEntity.setDescription(rolesDto.getDescription());
		rolesEntity.setStatus(rolesDto.getStatus());
		return rolesEntity;
	}

	public List<RolesDto> convertToDtos(final List<Roles> rolesEntity) {
		List<RolesDto> rolesDto = new ArrayList<>();
		rolesEntity.parallelStream().forEach(e -> rolesDto.add(convertToDto(e)));

		return rolesDto;
	}

}
