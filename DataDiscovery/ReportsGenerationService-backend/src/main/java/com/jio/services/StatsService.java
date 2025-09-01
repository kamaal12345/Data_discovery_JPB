package com.jio.services;

import org.springframework.stereotype.Service;

import com.jio.dto.StatsDto;
import com.jio.repository.PiiScanRequestRepository;
import com.jio.repository.ReportDetailsRepository;
import com.jio.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsService {

	private final UserRepository userRepository;
	private final ReportDetailsRepository reportRepository;
	private final PiiScanRequestRepository scanRequestRepository;

	public StatsDto getStatistics() {
		long totalUsers = userRepository.countUsersExcludingSuperAdmin();
		long activeUsers = userRepository.countActiveUsersExcludingSuperAdmin();
		long reportCount = reportRepository.count();
		long scanCount = scanRequestRepository.count();

		return new StatsDto(totalUsers, activeUsers, reportCount, scanCount);
	}

}
