package com.jio.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jio.utils.ScanProgressTracker;

@RestController
@RequestMapping("/api/scan")
public class ScanProgressController {

	@GetMapping("/progress/{scanId}")
	public ResponseEntity<Map<String, Object>> getScanProgress(@PathVariable Long scanId) {
		ScanProgressTracker.ScanProgress progress = ScanProgressTracker.getProgress(scanId);
		Map<String, Object> response = new HashMap<>();
		response.put("scannedFiles", progress.getScannedFiles());
		response.put("totalFiles", progress.getTotalFiles());
		response.put("percentage", progress.getPercentage());
		return ResponseEntity.ok(response);
	}
}
