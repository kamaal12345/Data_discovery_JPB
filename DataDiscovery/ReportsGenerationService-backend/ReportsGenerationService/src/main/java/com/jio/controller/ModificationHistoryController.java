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

import com.jio.dto.ModificationHistoryDto;
import com.jio.services.ModificationHistoryService;

@RestController
@RequestMapping("/api/modification-history")
public class ModificationHistoryController {

	@Autowired
	private ModificationHistoryService modificationHistoryService;

	@PostMapping("/add")
	public ResponseEntity<ModificationHistoryDto> create(@RequestBody ModificationHistoryDto dto) {
		return ResponseEntity.ok(modificationHistoryService.createModificationHistory(dto));
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<ModificationHistoryDto> update(@PathVariable("id") Integer id,
			@RequestBody ModificationHistoryDto dto) {
		return ResponseEntity.ok(modificationHistoryService.updateHistory(id, dto));
	}

	@GetMapping("/view/{id}")
	public ResponseEntity<ModificationHistoryDto> getById(@PathVariable("id") Integer id) {
		return ResponseEntity.ok(modificationHistoryService.getHistoryById(id));
	}

	@GetMapping("/list")
	public ResponseEntity<List<ModificationHistoryDto>> getAll() {
		return ResponseEntity.ok(modificationHistoryService.getAllModificationHistories());
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
		modificationHistoryService.deleteModificationHistory(id);
		return ResponseEntity.noContent().build();
	}
}
