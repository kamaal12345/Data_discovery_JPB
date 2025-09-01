package com.jio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jio.dto.ReportsContentDto;
import com.jio.services.ReportsContentService;

@RestController
@RequestMapping("/api/reports-content")
public class ReportsContentController {

	@Autowired
	private ReportsContentService service;

	@PostMapping("/add")
	public ResponseEntity<ReportsContentDto> create(@RequestBody ReportsContentDto dto) {
		return ResponseEntity.ok(service.createContent(dto));
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<ReportsContentDto> update(@PathVariable("id") Integer id,
			@RequestBody ReportsContentDto dto) {
		return ResponseEntity.ok(service.updateContent(id, dto));
	}

	@GetMapping("/view/{id}")
	public ResponseEntity<ReportsContentDto> getById(@PathVariable("id") Integer id) {
		return ResponseEntity.ok(service.getById(id));
	}

	@GetMapping("/list")
	public ResponseEntity<List<ReportsContentDto>> getAll() {
		return ResponseEntity.ok(service.getAll());
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
