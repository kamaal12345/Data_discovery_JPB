package com.jio.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jio.convertor.PiiScanResultConvertor;
import com.jio.dto.PiiScanResultDto;
import com.jio.entity.PiiScanResult;
import com.jio.repository.PiiScanResultRepository;

@Service
public class PiiScanResultService {

	@Autowired
	private PiiScanResultRepository piiScanResultRepository;
	
	
	 public Page<PiiScanResultDto> getResultsByRequestId(Long requestId, int page, int size, String searchText) {
	        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

	        Page<PiiScanResult> resultPage;
	        if (searchText != null && !searchText.trim().isEmpty()) {
	            resultPage = piiScanResultRepository.searchByRequestIdAndText(requestId, searchText, pageable);
	        } else {
	            resultPage = piiScanResultRepository.findByPiiScanRequest_RequestId(requestId, pageable);
	        }

	        return resultPage.map(PiiScanResultConvertor::convertToPiiScanResultDTO);
	    }
	 
	 
	 public List<PiiScanResultDto> getAllResultsByRequestId(Long requestId) {
		    List<PiiScanResult> entities = piiScanResultRepository.findByPiiScanRequest_RequestId(requestId);
		    return entities.stream()
		                   .map(PiiScanResultConvertor::convertToPiiScanResultDTO)
		                   .collect(Collectors.toList());
		}

}
