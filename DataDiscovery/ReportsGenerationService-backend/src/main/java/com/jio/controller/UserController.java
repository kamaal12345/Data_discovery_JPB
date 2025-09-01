package com.jio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jio.customexception.BusinessException;
import com.jio.dto.APIResponse;
import com.jio.dto.ProfileImgDto;
import com.jio.dto.StatsDto;
import com.jio.dto.UserDto;
import com.jio.services.StatsService;
import com.jio.services.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
    private StatsService statsService;

	@PostMapping("/create")
	public ResponseEntity<UserDto> createUser(@RequestBody UserDto userdto) throws BusinessException {
		UserDto createdUser = userService.createUser(userdto);
		return ResponseEntity.ok(createdUser);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<UserDto> getUserById(@PathVariable("userId") Integer userIdL,
			@RequestHeader(name = "Authorization", required = true) final String authorization) {
		UserDto userRetrived = userService.getUserByUserId(userIdL);
		return new ResponseEntity<UserDto>(userRetrived, HttpStatus.OK);
	}
	
	@PutMapping("/edit-user/{id}")
	public ResponseEntity<UserDto> updateUserById(@PathVariable Integer id, @RequestBody UserDto userDto)
			throws Exception {
		UserDto updatedUser = userService.updateUserById(id, userDto);
		return new ResponseEntity<UserDto>(updatedUser, HttpStatus.CREATED);
	}

	@GetMapping("/users-list")
	private APIResponse<Page<UserDto>> getUsersWithPagination(@RequestParam(defaultValue = "0") Integer offset,
			@RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "") String field,
			@RequestParam Integer sort, @RequestParam String searchText,
			@RequestParam(required = false) Boolean status) {
		Page<UserDto> usersWithPagination = userService.getAllUsers(offset, pageSize, field, sort, searchText, status);
		return new APIResponse<>(usersWithPagination.getSize(), usersWithPagination);
	}
	
	@GetMapping("/users-profile/{userId}")
	public ResponseEntity<UserDto> getUserDetailsById(@PathVariable("userId") Integer userId,
			@RequestHeader(name = "Authorization", required = true) final String authorization) {
		UserDto userRetrived = userService.getUserByUserId(userId);
		return new ResponseEntity<UserDto>(userRetrived, HttpStatus.OK);
	}
	

	@PutMapping("/edit-profile/{id}")
	public ResponseEntity<UserDto> updateUserProfileById(@PathVariable Integer id, @RequestBody UserDto userDto)
			throws Exception {
		UserDto updatedUser = userService.updateUserById(id, userDto);
		return new ResponseEntity<UserDto>(updatedUser, HttpStatus.CREATED);
	}
	
	@GetMapping("/user-profileImg/{id}")
	public ResponseEntity<ProfileImgDto> getUserProfileImgById(@PathVariable Integer id) {
		ProfileImgDto userProfileImg = userService.getUserProfileImgById(id);
		return new ResponseEntity<ProfileImgDto>(userProfileImg, HttpStatus.OK);

	}
	
    @GetMapping("/stats")
    public StatsDto getStats() {
        return statsService.getStatistics();
    }

}
