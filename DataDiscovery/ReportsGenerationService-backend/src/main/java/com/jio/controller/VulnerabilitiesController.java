package com.jio.controller;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jio.dto.APIResponse;
import com.jio.dto.VulnerabilitiesSummaryDto;
import com.jio.entity.VulnerabilitiesSummary;
import com.jio.services.ModificationHistoryService;
import com.jio.services.ReportDetailsService;
import com.jio.services.VulnerabilitiesService;

@RestController
@RequestMapping("/api/vulnerabilities")
public class VulnerabilitiesController {

	private final VulnerabilitiesService vulnerabilitiesService;

	public VulnerabilitiesController(VulnerabilitiesService vulnerabilitiesService,
			ModificationHistoryService modificationHistoryService, ReportDetailsService reportDetailsService) {
		this.vulnerabilitiesService = vulnerabilitiesService;
	}

	@PostMapping("/add_vuler_summary")
	public ResponseEntity<VulnerabilitiesSummaryDto> createVulnerability(@RequestBody VulnerabilitiesSummaryDto dto) {
		VulnerabilitiesSummaryDto saved = vulnerabilitiesService.createVulnerability(dto);
		return new ResponseEntity<>(saved, HttpStatus.CREATED);
	}

	@PutMapping("/update_vuler_summary_by/{id}")
	public ResponseEntity<VulnerabilitiesSummaryDto> updateVulnerability(@PathVariable("id") Integer id,
			@RequestBody VulnerabilitiesSummaryDto dto) {
		return ResponseEntity.ok(vulnerabilitiesService.updateVulnerability(id, dto));
	}

	@GetMapping("/get_all_vuler_summaries")
	public ResponseEntity<List<VulnerabilitiesSummaryDto>> getAllVulnerabilities() {
		return ResponseEntity.ok(vulnerabilitiesService.getAllVulnerabilities());
	}

	@GetMapping("/vul_list")
	public APIResponse<Page<VulnerabilitiesSummary>> getVulnerabilitiesWithPagination(
			@RequestParam(name = "offset", defaultValue = "0") Integer offset,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(name = "field", defaultValue = "") String field,
			@RequestParam(name = "sort", required = false, defaultValue = "0") Integer sort,
			@RequestParam(name = "searchText", required = false, defaultValue = "") String searchText,
			@RequestParam(name = "startDate", defaultValue = "946665000000") Long startDate,
			@RequestParam(name = "endDate", defaultValue = "7289548200000") Long endDate) {
		Page<VulnerabilitiesSummary> results = vulnerabilitiesService.getAllVulnerabilities(offset, pageSize, field,
				sort, searchText, new Date(startDate), new Date(endDate));
		return new APIResponse<>(results.getSize(), results);
	}

	@GetMapping("/list")
	public String listVulnerabilities(Model model) {
		List<VulnerabilitiesSummaryDto> vulnerabilities = vulnerabilitiesService.getAllVulnerabilities();
		model.addAttribute("vulnerabilities", vulnerabilities);
		return "vulnerabilities"; // templates/vulnerabilities.html
	}

	@GetMapping("/get_vuler_summary_by/{id}")
	public ResponseEntity<VulnerabilitiesSummaryDto> getVulnerabilityById(@PathVariable("id") Integer id) {
		VulnerabilitiesSummaryDto dto = vulnerabilitiesService.getVulnerability(id);
		return ResponseEntity.ok(dto);
	}

	@DeleteMapping("/del_vuler_summary_by/{id}")
	public ResponseEntity<Void> deleteVulnerability(@PathVariable("id") Integer id) {
		vulnerabilitiesService.deleteVulnerability(id);
		return ResponseEntity.noContent().build();
	}

}
