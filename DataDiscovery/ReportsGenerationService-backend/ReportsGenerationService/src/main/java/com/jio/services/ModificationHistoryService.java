package com.jio.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jio.convertor.ModificationHistoryConvertor;
import com.jio.dto.ModificationHistoryDto;
import com.jio.entity.ModificationHistory;
import com.jio.repository.ModificationHistoryRepository;
import com.jio.repository.VulnerabilitiesSummaryRepository;

@Service
public class ModificationHistoryService {

	private final ModificationHistoryRepository modificationHistoryRepository;

	public ModificationHistoryService(ModificationHistoryRepository modificationHistoryRepository,
			VulnerabilitiesSummaryRepository vulnerabilitiesSummaryRepository) {
		this.modificationHistoryRepository = modificationHistoryRepository;
	}

	public ModificationHistoryDto createModificationHistory(ModificationHistoryDto dto) {
		ModificationHistory entity = ModificationHistoryConvertor.toEntity(dto);
		return ModificationHistoryConvertor.toDTO(modificationHistoryRepository.save(entity));
	}

	public ModificationHistoryDto updateHistory(Integer id, ModificationHistoryDto dto) {
		ModificationHistory existing = modificationHistoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("History not found with id: " + id));

		existing.setVersion(dto.getVersion());
		existing.setDate(dto.getDate());
		existing.setAuthor(dto.getAuthor());
		existing.setDescription(dto.getDescription());
		existing.setReportId(dto.getReportId());

		return ModificationHistoryConvertor.toDTO(modificationHistoryRepository.save(existing));
	}

	public ModificationHistoryDto getHistoryById(Integer id) {
		return modificationHistoryRepository.findById(id).map(ModificationHistoryConvertor::toDTO)
				.orElseThrow(() -> new RuntimeException("History not found with id: " + id));
	}

	public List<ModificationHistoryDto> getAllModificationHistories(Integer reportId) {
		return modificationHistoryRepository.findAllByReportId(reportId).stream()
				.map(ModificationHistoryConvertor::toDTO).collect(Collectors.toList());
	}

	public List<ModificationHistoryDto> getAllModificationHistories() {
		return modificationHistoryRepository.findAll().stream().map(ModificationHistoryConvertor::toDTO)
				.collect(Collectors.toList());
	}

	public void deleteModificationHistory(Integer id) {
		modificationHistoryRepository.deleteById(id);
	}
}
