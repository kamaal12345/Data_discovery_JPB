package com.jio.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jio.dto.UserDto;
import com.jio.entity.Roles;
import com.jio.entity.User;
import com.jio.repository.RolesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserConvertor {

	private final RolesRepository rolesRepository;

	public UserDto convertToDto(final User entity) {
		UserDto dto = new UserDto();

		dto.setUserId(entity.getUserId());
		dto.setUsername(entity.getUsername());
		dto.setEmail(entity.getEmail());
		dto.setFirstName(entity.getFirstName());
		dto.setLastName(entity.getLastName());
		dto.setEmployeeId(entity.getEmployeeId());
		dto.setComments(entity.getComments());
		dto.setMobilePhone(entity.getMobilePhone());
		dto.setCreatedById(entity.getCreatedById());
		dto.setUpdatedById(entity.getUpdatedById());
		dto.setStatus(entity.getStatus());
		dto.setMetaStatus(entity.getMetaStatus());
		dto.setLoggedIn(entity.getLoggedIn());
		dto.setGender(entity.getGender());
		dto.setDob(entity.getDob());
		dto.setProfileImg(entity.getProfileImg());

		if (entity.getRoles() != null) {
			List<Integer> roleValues = entity.getRoles().stream()
				.map(Roles::getId)
				.collect(Collectors.toList());
			dto.setRoleValue(roleValues);
		}

		dto.setCreatedDate(entity.getCreatedDate());
		dto.setUpdatedDate(entity.getUpdatedDate());
		dto.setDesignation(entity.getDesignation());
		dto.setDepartment(entity.getDepartment());

		return dto;
	}

	public User convertToEntity(final UserDto dto) {
		User entity = new User();
		entity.setUserId(dto.getUserId());
		entity.setUsername(dto.getUsername());
		entity.setEmail(dto.getEmail());
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setPassword(dto.getPassword());

		if (dto.getPassword() != null) {
			entity.setPassword(dto.getPassword());
		}
		entity.setEmployeeId(dto.getEmployeeId());
		entity.setStatus(dto.getStatus());
		entity.setMetaStatus(dto.getMetaStatus());
		entity.setLoggedIn(dto.getLoggedIn());
		entity.setCreatedById(dto.getCreatedById());
		entity.setUpdatedById(dto.getUpdatedById());
		entity.setMobilePhone(dto.getMobilePhone());
		entity.setComments(dto.getComments());
		entity.setProfileImg(dto.getProfileImg());

		// Convert role IDs (List<Integer>) to Set<Roles>
		if (dto.getRoleValue() != null && !dto.getRoleValue().isEmpty()) {
			Set<Roles> roles = rolesRepository.findAllById(dto.getRoleValue()).stream().collect(Collectors.toSet());
			entity.setRoles(roles);
		}
		
		if (entity.getRoles() != null) {
			List<Integer> roleValues = entity.getRoles().stream()
				.map(Roles::getId)
				.collect(Collectors.toList());
			dto.setRoleValue(roleValues);
		}

		entity.setGender(dto.getGender());
		entity.setDob(dto.getDob());
		entity.setCreatedDate(dto.getCreatedDate());
		entity.setUpdatedDate(dto.getUpdatedDate());
		entity.setDesignation(dto.getDesignation());
		entity.setDepartment(dto.getDepartment());

		return entity;
	}

	// Convert a List of User entities to a List of UserDto
	public List<UserDto> convertToDtos(final List<User> entity) {
		List<UserDto> dto = new ArrayList<>();
		entity.stream().forEach(e -> dto.add(convertToDto(e)));
		return dto;
	}

	// Convert a Page of User entities to a Page of UserDto
	public Page<UserDto> convertToUserPageDTO(Page<User> users, Pageable pageable) {
		return new PageImpl<UserDto>(convertToPageUserDTOs(users.getContent()), pageable, users.getTotalElements());
	}

	// Helper method to convert List of Users to List of UserDto
	private List<UserDto> convertToPageUserDTOs(List<User> users) {
		List<UserDto> userDtos = convertToDtos(users);
		return userDtos;
	}
}
