package com.jio.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jio.convertor.ReportsContentConvertor;
import com.jio.dto.ReportsContentDto;
import com.jio.entity.ReportsContent;
import com.jio.repository.ReportsContentRepository;

@Service
public class ReportsContentService {

	@Autowired
	private ReportsContentRepository repository;

	public ReportsContentDto createContent(ReportsContentDto dto) {
		ReportsContent entity = ReportsContentConvertor.toEntity(dto);
		return ReportsContentConvertor.toDto(repository.save(entity));
	}

	public ReportsContentDto updateContent(Integer id, ReportsContentDto dto) {
		ReportsContent existing = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Reports content not found with id: " + id));

		existing.setDisclaimer(dto.getDisclaimer());
		existing.setConfiAndPropText(dto.getConfiAndPropText());
		existing.setReportAnalysis(dto.getReportAnalysis());
		existing.setExecutiveSummary(dto.getExecutiveSummary());
		existing.setProgressReport(dto.getProgressReport());
		existing.setMethodologyText(dto.getMethodologyText());
		existing.setMethodologyImage(dto.getMethodologyImage());

		return ReportsContentConvertor.toDto(repository.save(existing));
	}

	public ReportsContentDto getById(Integer id) {
		return repository.findById(id).map(ReportsContentConvertor::toDto)
				.orElseThrow(() -> new RuntimeException("Reports content not found with id: " + id));
	}

	public List<ReportsContentDto> getAll() {
		return ReportsContentConvertor.toDtoList(repository.findAll());
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}
}
