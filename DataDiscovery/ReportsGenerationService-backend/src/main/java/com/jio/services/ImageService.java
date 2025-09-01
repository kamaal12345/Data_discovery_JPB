package com.jio.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.jio.convertor.ImageConverter;
import com.jio.dto.ImageDTO;
import com.jio.entity.Images;
import com.jio.repository.ImageRepository;

@Service
public class ImageService {

	@Autowired
	private ImageRepository imageRepository;

	public ImageDTO saveImage(ImageDTO dto) {
		boolean nameExists = imageRepository.existsByName(dto.getName());
		boolean dataUrlExists = imageRepository.existsByDataUrl(dto.getDataUrl());

		if (nameExists || dataUrlExists) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"An image with the same name or data URL already exists.");
		}

		Images entity = ImageConverter.toEntity(dto);
		Images saved = imageRepository.save(entity);
		return ImageConverter.toDTO(saved);
	}

	public Optional<Images> getImageById(Integer id) {
		return imageRepository.findById(id);
	}

	public Images editDocumentImage(Integer id, Images imageEntity) {

		Images image = imageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));

		image.setName(imageEntity.getName());
		image.setDataUrl(imageEntity.getDataUrl());

		return imageRepository.save(image);
	}

	public List<ImageDTO> getAllImages() {
		List<Images> entities = imageRepository.findAll();
		return entities.stream().map(ImageConverter::toDTO).collect(Collectors.toList());
	}

}
