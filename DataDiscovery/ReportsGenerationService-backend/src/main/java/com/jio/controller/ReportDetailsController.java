package com.jio.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.jio.dto.ReportDetailsDto;
import com.jio.entity.TotalReport;
import com.jio.services.ReportDetailsService;

@RestController
@RequestMapping("/api/reports")
public class ReportDetailsController {

	@Autowired
	private ReportDetailsService reportDetailsService;

	@PostMapping("/add_report_details")
	public ResponseEntity<ReportDetailsDto> createFUI(@RequestBody ReportDetailsDto dto) throws Exception {
		return new ResponseEntity<ReportDetailsDto>(reportDetailsService.createOrUpdateReportDetailsFUI(dto),
				HttpStatus.CREATED);
	}

	@PostMapping("/add_report_details_f_excel")
	public ResponseEntity<ReportDetailsDto> createFExcel(@RequestBody ReportDetailsDto dto) throws Exception {
		return new ResponseEntity<ReportDetailsDto>(reportDetailsService.createOrUpdateReportDetailsFExcel(dto),
				HttpStatus.CREATED);
	}

	@GetMapping("/check-scope")
	public ResponseEntity<Map<String, Object>> checkScope(@RequestParam String scope) {
		Map<String, Object> response = reportDetailsService.checkReportScope(scope);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/update_report_details")
	public ResponseEntity<ReportDetailsDto> update(@RequestBody ReportDetailsDto dto) throws Exception {
		ReportDetailsDto updatedReportDetails = reportDetailsService.createOrUpdateReportDetailsFUI(dto);
		return new ResponseEntity<ReportDetailsDto>(updatedReportDetails, HttpStatus.OK);
	}

	@GetMapping("/{reportId}")
	public ResponseEntity<ReportDetailsDto> update(@PathVariable("reportId") Integer reportId) {
		return ResponseEntity.ok(reportDetailsService.getReportDetailseById(reportId));
	}

	@GetMapping("/report_details_View/{reportId}")
	public ResponseEntity<ReportDetailsDto> getReportDetailsById(@PathVariable("reportId") Integer reportId) {
		ReportDetailsDto ReportDetailsRetrieved = reportDetailsService.getReportDetailseById(reportId);
		return new ResponseEntity<>(ReportDetailsRetrieved, HttpStatus.OK);
	}

	@GetMapping("/reportDetails-list")
	private APIResponse<Page<ReportDetailsDto>> getReportDetailsList(
			@RequestParam(name = "offset", defaultValue = "0") Integer offset,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(name = "field", defaultValue = "") String field,
			@RequestParam(name = "sort", required = false, defaultValue = "0") Integer sort) {
		Page<ReportDetailsDto> reportDetailsList = reportDetailsService.getAllReportDetails(offset, pageSize, field,
				sort);
		return new APIResponse<>(reportDetailsList.getSize(), reportDetailsList);
	}

	@GetMapping("/view_report_details_by/{reportId}")
	public ResponseEntity<ReportDetailsDto> getById(@PathVariable("reportId") Integer reportId) {
		return ResponseEntity.ok(reportDetailsService.getById(reportId));
	}

	@GetMapping("/list_report_details")
	public ResponseEntity<List<ReportDetailsDto>> getAll() {
		return ResponseEntity.ok(reportDetailsService.getAllReports());
	}

	@DeleteMapping("/delete_report_details_by/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
		reportDetailsService.deleteReport(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/total/{reportId}")
	public ResponseEntity<TotalReport> getTotalReport(@PathVariable("reportId") Integer reportId) {
		TotalReport totalReport = reportDetailsService.getTotalReportByReportId(reportId);
		return ResponseEntity.ok(totalReport);
	}
}
