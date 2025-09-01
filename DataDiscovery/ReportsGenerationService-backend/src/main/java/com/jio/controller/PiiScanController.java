package com.jio.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jio.customexception.BusinessException;
import com.jio.dto.APIResponse;
import com.jio.dto.PiiScanRequestDto;
import com.jio.dto.PiiScanResultDto;
import com.jio.dto.RemotePiiScanRequestDto;
import com.jio.services.PiiScanService;
import com.jio.services.RemotePiiScanService;
import com.jio.services.WindowsRemotePiiScanService;
import com.jio.utils.JwtUtils;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/pii-scan")
public class PiiScanController {

	@Autowired
	private PiiScanService piiScanService;

	@Autowired
	private RemotePiiScanService remotePiiScanService;
	
	@Autowired
	private WindowsRemotePiiScanService windowsRemotePiiScanService;
	
    @Autowired
    private JwtUtils jwtUtils;

	@PostMapping("/scan")
	public ResponseEntity<PiiScanRequestDto> scanFiles(@RequestBody PiiScanRequestDto dto) {
		try {
			PiiScanRequestDto result = piiScanService.scanFiles(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(result);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

//	@PostMapping("/remote-scan")
//	public ResponseEntity<PiiScanRequestDto> remoteScan(@RequestBody RemotePiiScanRequestDto requestDto) {
//		try {
//			return ResponseEntity.ok(piiScanService.scanViaSshOnce(requestDto));
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//		}
//	}

//	@PostMapping("/remote-scan")
//	public ResponseEntity<PiiScanRequestDto> remoteScan(@RequestBody RemotePiiScanRequestDto requestDto) {
//		try {
//			return ResponseEntity.ok(remotePiiScanService.scanViaSshOnce(requestDto));
//		} catch (BusinessException e) {
//			throw e;
//		} catch (Exception e) {
//			throw new RuntimeException(e.getMessage());
//		}
//	}
	
//	@PostMapping("/remote-scan")
//    public ResponseEntity<String> startScan(@RequestBody RemotePiiScanRequestDto dto) {
//        try {
//            remotePiiScanService.scanViaSshOnce(dto);
//            return ResponseEntity.accepted().body("Scan started");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Scan failed: " + e.getMessage());
//        }
//    }
	
//	@PostMapping("/remote-scan")
//	public ResponseEntity<String> startScan(
//	        @RequestHeader("Authorization") String token,
//	        @RequestBody RemotePiiScanRequestDto dto) {
//
//	    if (token == null || token.isEmpty()) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
//	    }
//
//	    if (!jwtUtils.validateRefreshToken(token)) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
//	    }
//
//	    String username = jwtUtils.extractUsername(token);
//
//	    String tokenType = jwtUtils.extractClaim(token, "type");
//	    if (!"refresh".equals(tokenType)) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is not a refresh token");
//	    }
//
//	    try {
//	        remotePiiScanService.scanViaSshOnce(dto); 
//	        return ResponseEntity.accepted().body("Long scan started for user: " + username);
//	    } catch (Exception e) {
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	                             .body("Scan failed: " + e.getMessage());
//	    }
//	}
	
//	@PostMapping("/remote-scan")
//	public ResponseEntity<?> startScan(
//	        @RequestHeader("Authorization") String token,
//	        @RequestBody RemotePiiScanRequestDto dto) {
//
//	    if (token == null || token.isEmpty()) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
//	    }
//
//	    if (!jwtUtils.validateRefreshToken(token)) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
//	    }
//
//	    String username = jwtUtils.extractUsername(token);
//	    String tokenType = jwtUtils.extractClaim(token, "type");
//
//	    if (!"refresh".equals(tokenType)) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is not a refresh token");
//	    }
//
//	    try {
//	        PiiScanRequestDto result = remotePiiScanService.scanViaSshOnce(dto);
//
//	        return ResponseEntity.ok(result);
//
//	    } catch (BusinessException be) {
//	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//	                             .body("Scan failed: " + be.getMessage());
//	    } catch (Exception e) {
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	                             .body("Unexpected error: " + e.getMessage());
//	    }
//	}

	@PostMapping("/remote-scan")
	public ResponseEntity<?> startScan(
	        @RequestHeader("Authorization") String token,
	        @RequestBody RemotePiiScanRequestDto dto) {

	    if (token == null || token.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
	    }

	    if (!jwtUtils.validateRefreshToken(token)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
	    }

	    String tokenType = jwtUtils.extractClaim(token, "type");

	    if (!"refresh".equals(tokenType)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is not a refresh token");
	    }

	    try {
	        String type = dto.getServerType() != null ? dto.getServerType().toUpperCase() : "";

	        PiiScanRequestDto result;

	        switch (type) {
	            case "LINUX":
	            case "UNIX":
	                result = remotePiiScanService.scanViaSshOnce(dto);
	                break;

	            case "WINDOWS":
	            	result = windowsRemotePiiScanService.scanViaWinRmOnce(dto);
	            	break;

	            default:
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body("Unsupported server type: " + type);
	        }

	        return ResponseEntity.ok(result);

	    } catch (BusinessException be) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                             .body("Scan failed: " + be.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("Unexpected error: " + e.getMessage());
	    }
	}


	@GetMapping("/scan_list")
	public APIResponse<Page<PiiScanRequestDto>> getScanListPaginated(
			@RequestParam(name = "offset", defaultValue = "0") Integer offset,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(name = "field", defaultValue = "") String field,
			@RequestParam(name = "sort", defaultValue = "0") Integer sort,
			@RequestParam(name = "searchText", defaultValue = "") String searchText) {
		Page<PiiScanRequestDto> page = piiScanService.getAllScanRequests(offset, pageSize, field, sort, searchText);
		return new APIResponse<>(page.getSize(), page);
	}
	
	
//	@GetMapping("/scan_list/{requestId}/results")
//	public APIResponse<Page<PiiScanResultDto>> getScanResults(
//	        @PathVariable Long requestId,
//	        @RequestParam(name = "page", defaultValue = "0") int page,
//	        @RequestParam(name = "size", defaultValue = "100") int size) {
//	    Page<PiiScanResultDto> results = piiScanService.getPagedScanResults(requestId, page, size);
//	    return new APIResponse<>(results.getSize(), results);
//	}


	@GetMapping("/export-pii-results/{requestId}")
	public void exportPiiResults(@PathVariable("requestId") Long requestId, HttpServletResponse response)
			throws Exception {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=pii-scan-results.xlsx");
		piiScanService.exportPiiResults(requestId, response.getOutputStream());
		response.getOutputStream().flush();
	}

}
