package com.jio.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jio.convertor.UserConvertor;
import com.jio.customexception.BusinessException;
import com.jio.dto.AuthenticationRequest;
import com.jio.dto.ProfileImgDto;
import com.jio.dto.UserDetailsImpl;
import com.jio.dto.UserDto;
import com.jio.entity.Roles;
import com.jio.entity.User;
import com.jio.repository.RolesRepository;
import com.jio.repository.UserRepository;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	PasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private RolesRepository rolesRepository;

	@Autowired
	private UserConvertor userConvertor;

	@Override
	public UserDetailsImpl loadUserByUsername(String userName) throws UsernameNotFoundException, BusinessException {
		User user = userRepository.findByUsername(userName)
				.orElseThrow(() -> new UsernameNotFoundException("User not present"));
		Boolean status = user.getStatus();
		if (status == false) {
			throw new BusinessException("601", "User disable,Please contact admin");
		}
		return new UserDetailsImpl(user.getUserId(), user.getUsername(), user.getPassword(), user.getFirstName(),
				user.getLastName(), user.getGender(), user.getDesignation(), user.getRoles());
	}

	@Transactional
	public UserDto createUser(UserDto userdto) throws BusinessException {

		if (userRepository.findByUsername(userdto.getUsername()).isPresent()) {
			throw new BusinessException("801", "Username already exists");
		}

		if (userRepository.findByEmail(userdto.getEmail()).isPresent()) {
			throw new BusinessException("802", "Email already exists");
		}

		userdto.setPassword(bCryptPasswordEncoder.encode(userdto.getPassword()));

		if (userdto.getLoggedIn() == null) {
			userdto.setLoggedIn(false);
		}
		if (userdto.getStatus() == null) {
			userdto.setStatus(true);
		}

		if (userdto.getRoleValue() == null || userdto.getRoleValue().isEmpty()) {
			Roles employeeRole = rolesRepository.findByRoleName("Employee")
					.orElseThrow(() -> new BusinessException("803", "Default role 'Employee' not found"));
			userdto.setRoleValue(List.of(employeeRole.getId()));
		}

		User userEntity = userConvertor.convertToEntity(userdto);
		User savedUser = userRepository.save(userEntity);
		return userConvertor.convertToDto(savedUser);
	}

	public void logOutUserByUsername(String username) {
		Optional<User> userLogOut = userRepository.findByUsername(username);
		User user = userLogOut.get();
		if (user == null) {
			return;
		}
		user.setLoggedIn(false);
		userRepository.save(user);
	}

	public UserDto getUserByUserId(int userId) {
		User userData = userRepository.findById(userId).get();
		UserDto userDto = userConvertor.convertToDto(userData);
		return userDto;
	}

	public Page<UserDto> getAllUsers(int offset, int pageSize, String field, Integer sort, String searchText,
			Boolean status) {
		Pageable pageable = null;
		Direction direction = sort != null && sort == 0 ? Direction.DESC : Direction.ASC;

		if (!StringUtils.isEmpty(field)) {
			pageable = PageRequest.of(offset, pageSize, Sort.by(direction, field));

		} else {
			pageable = PageRequest.of(offset, pageSize, Sort.by(direction, "userId"));
		}
		Page<User> users = searchUsers(offset, pageSize, field, sort, searchText, status);
		Page<UserDto> uPage = userConvertor.convertToUserPageDTO(users, pageable);
		return uPage;
	}

	private Page<User> searchUsers(int offset, int pageSize, String field, Integer sort, String searchText,
			Boolean status) {
		Pageable pageable = null;

		Direction direction = sort != null && sort == 0 ? Direction.DESC : Direction.ASC;

		if (!StringUtils.isEmpty(field)) {
			pageable = PageRequest.of(offset, pageSize, Sort.by(direction, field));

		} else {
			pageable = PageRequest.of(offset, pageSize, Sort.by(direction, "userId"));
		}

		if (searchText != "") {
			Page<User> foundUsers = userRepository.search(searchText, pageable);
			return foundUsers;
		}
		if (status != null) {
			Page<User> findUserByStatus = userRepository.findUserByStatus(status, pageable);
			return findUserByStatus;
		}
		Page<User> findUserByMetaStatus = userRepository.findUserByMetaStatus(status, pageable);
		return findUserByMetaStatus;

	}

	@Transactional
	public UserDto updateUserById(Integer id, UserDto userDto) throws Exception {
		Optional<User> optionalUser = userRepository.findById(id);

		if (!optionalUser.isPresent()) {
			throw new Exception("User not found with id: " + id);
		}

		User existingUser = optionalUser.get();

		// Update fields only if they're present
		if (userDto.getUsername() != null)
			existingUser.setUsername(userDto.getUsername());
		if (userDto.getEmail() != null)
			existingUser.setEmail(userDto.getEmail());
		if (userDto.getFirstName() != null)
			existingUser.setFirstName(userDto.getFirstName());
		if (userDto.getLastName() != null)
			existingUser.setLastName(userDto.getLastName());
		if (userDto.getEmployeeId() != null)
			existingUser.setEmployeeId(userDto.getEmployeeId());

		String oldPass = existingUser.getPassword();
		if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
			existingUser.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
		} else {
			userDto.setPassword(oldPass);
		}

		if (userDto.getMobilePhone() != null)
			existingUser.setMobilePhone(userDto.getMobilePhone());
		if (userDto.getComments() != null)
			existingUser.setComments(userDto.getComments());
		if (userDto.getProfileImg() != null)
			existingUser.setProfileImg(userDto.getProfileImg());
		if (userDto.getStatus() != null)
			existingUser.setStatus(userDto.getStatus());
		if (userDto.getMetaStatus() != null)
			existingUser.setMetaStatus(userDto.getMetaStatus());
		if (userDto.getLoggedIn() != null)
			existingUser.setLoggedIn(userDto.getLoggedIn());
		if (userDto.getGender() != null)
			existingUser.setGender(userDto.getGender());
		if (userDto.getDob() != null)
			existingUser.setDob(userDto.getDob());
		if (userDto.getDesignation() != null)
			existingUser.setDesignation(userDto.getDesignation());
		if (userDto.getDepartment() != null)
			existingUser.setDepartment(userDto.getDepartment());
		if (userDto.getUpdatedById() != null)
			existingUser.setUpdatedById(userDto.getUpdatedById());
		if (userDto.getUpdatedDate() != null)
			existingUser.setUpdatedDate(userDto.getUpdatedDate());
		// Handle roles
		if (userDto.getRoleValue() != null && !userDto.getRoleValue().isEmpty()) {
			existingUser.setRoles(userConvertor.convertToEntity(userDto).getRoles());
		}

		User savedUser = userRepository.save(existingUser);

		return userConvertor.convertToDto(savedUser);
	}
	
	public ProfileImgDto getUserProfileImgById(Integer id) {
		ProfileImgDto imgDto = new ProfileImgDto();
		Optional<User> findByProfileImg = userRepository.findById(id);
		String profileImg = findByProfileImg.get().getProfileImg();
		imgDto.setProfileImg(profileImg);
		return imgDto;
	}
	
	// alter logout and session log out

	public String logOutUser(AuthenticationRequest authenticationRequest) {
		User userLogOut = userRepository.getLoggedInByUsername(authenticationRequest.getUsername());
		if (userLogOut == null) {
			return "Invalid username or password";
		}
		userLogOut.setLoggedIn(false);
		userRepository.save(userLogOut);

		return "Logout";
	}
	
	public String sessionLogOutUser(AuthenticationRequest authenticationRequest) {
		User userLogOut = userRepository.getLoggedInByUsername(authenticationRequest.getUsername());
		if (userLogOut == null) {
			return "Invalid username or password";
		}
		userLogOut.setLoggedIn(false);
		userRepository.save(userLogOut);

		return "Logout";
	}
}