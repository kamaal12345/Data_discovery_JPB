package com.jio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jio.dto.HostDto;
import com.jio.services.HostService;

@RestController
@RequestMapping("/api/hosts")
public class HostController {

	@Autowired
	private HostService hostService;

	@PostMapping("/add_host")
	public ResponseEntity<HostDto> createHost(@RequestBody HostDto dto) {
		HostDto saved = hostService.createHost(dto);
		return new ResponseEntity<>(saved, HttpStatus.CREATED);
	}

	@PostMapping("/add_hosts_list")
	public ResponseEntity<List<HostDto>> createHosts(@RequestBody List<HostDto> hostDtos) {
		List<HostDto> saved = hostService.createHostsList(hostDtos);
		return new ResponseEntity<>(saved, HttpStatus.CREATED);
	}

	@GetMapping("/get_host/{id}")
	public ResponseEntity<HostDto> getHostById(@PathVariable("id") Integer id) {
		HostDto host = hostService.getHostById(id);
		return ResponseEntity.ok(host);
	}

	@PutMapping("/update_host/{id}")
	public ResponseEntity<HostDto> updateHost(@PathVariable("id") Integer id, @RequestBody HostDto dto) {
		return ResponseEntity.ok(hostService.updateHost(id, dto));
	}

	@GetMapping("/get_all_hosts")
	public ResponseEntity<List<HostDto>> getAllHosts() {
		return ResponseEntity.ok(hostService.getAllHosts());
	}
	
	@GetMapping("/get_active_hosts")
	public ResponseEntity<List<HostDto>> getAllActiveHosts() {
		return ResponseEntity.ok(hostService.getAllActiveHosts());
	}

}
