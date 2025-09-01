package com.jio.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jio.convertor.HostConvertor;
import com.jio.customexception.BusinessException;
import com.jio.dto.HostDto;
import com.jio.entity.Host;
import com.jio.repository.HostRepository;

@Service
public class HostService {

	@Autowired
	private HostRepository hostRepository;

	@Autowired
	private HostConvertor hostConvertor;
	
	private static final int ACTIVE_STATUS = 1;

	public HostDto createHost(HostDto dto) {
		if (hostRepository.existsByIp(dto.getIp())) {
			throw new BusinessException("DUPLICATE_IP", "Host with this IP already exists");
		}
		Host entity = hostConvertor.convertToEntity(dto);
		Host saved = hostRepository.save(entity);
		return hostConvertor.convertToDto(saved);
	}

	public HostDto updateHost(Integer id, HostDto dto) {
		Host existing = hostRepository.findById(id)
				.orElseThrow(() -> new BusinessException("HOST_NOT_FOUND", "Host not found with id: " + id));

		if (!existing.getIp().equals(dto.getIp()) && hostRepository.existsByIp(dto.getIp())) {
			throw new BusinessException("DUPLICATE_IP", "Another host with this IP already exists");
		}

		Host updatedEntity = hostConvertor.convertToEntity(dto);
		updatedEntity.setId(existing.getId());

		Host saved = hostRepository.save(updatedEntity);
		return hostConvertor.convertToDto(saved);
	}

	public List<HostDto> getAllHosts() {
		List<Host> allEntities = hostRepository.findAll();
		return hostConvertor.convertToDtoList(allEntities);
	}
	
	public List<HostDto> getAllActiveHosts() {
	    List<Host> activeHosts = hostRepository.findByStatus(ACTIVE_STATUS);
	    return hostConvertor.convertToDtoList(activeHosts);
	}

	public List<HostDto> createHostsList(List<HostDto> dtos) {
		List<HostDto> savedDtos = new ArrayList<>();

		for (HostDto dto : dtos) {
			if (hostRepository.existsByIp(dto.getIp())) {
				throw new BusinessException("DUPLICATE_IP", "Host with IP " + dto.getIp() + " already exists");
			}

			Host entity = hostConvertor.convertToEntity(dto);
			Host saved = hostRepository.save(entity);
			savedDtos.add(hostConvertor.convertToDto(saved));
		}

		return savedDtos;
	}

	public HostDto getHostById(Integer id) {
		Host existing = hostRepository.findById(id)
				.orElseThrow(() -> new BusinessException("HOST_NOT_FOUND", "Host not found with id: " + id));
		return hostConvertor.convertToDto(existing);
	}

}
