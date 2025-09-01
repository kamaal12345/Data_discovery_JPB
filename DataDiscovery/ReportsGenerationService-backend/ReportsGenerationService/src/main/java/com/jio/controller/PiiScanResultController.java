package com.jio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jio.dto.PiiScanResultDto;
import com.jio.services.PiiScanResultService;

@RestController
@RequestMapping("/api/pii-scanres")
public class PiiScanResultController {

    @Autowired
    private PiiScanResultService piiScanResultService;

    @GetMapping("/{requestId}/results")
    public Page<PiiScanResultDto> getChildResults(
            @PathVariable Long requestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchText) {

        return piiScanResultService.getResultsByRequestId(requestId, page, size, searchText);
    }
    
    @GetMapping("/{requestId}/all-results")
    public List<PiiScanResultDto> getAllResults(@PathVariable Long requestId) {
        return piiScanResultService.getAllResultsByRequestId(requestId);
    }

}
