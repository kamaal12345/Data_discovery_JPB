package com.jio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jio.dto.ImageDTO;
import com.jio.entity.Images;
import com.jio.services.ImageService;

@RestController
@RequestMapping("/api/image")
public class ImageController {

	@Autowired
	private ImageService imageService;

	@PostMapping("/add-document")
	public ResponseEntity<ImageDTO> uploadImage(@RequestBody ImageDTO imageDTO) {
		ImageDTO saved = imageService.saveImage(imageDTO);
		return ResponseEntity.ok(saved);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Images> getImageById(@PathVariable("id") Integer id) {
		return imageService.getImageById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/edit-documentImage/{id}")
	public Images updateImage(@PathVariable("id") Integer id, @RequestBody Images images) {
		return imageService.editDocumentImage(id, images);
	}

	@GetMapping("/all")
	public ResponseEntity<List<ImageDTO>> getAllImages() {
		List<ImageDTO> images = imageService.getAllImages();
		return ResponseEntity.ok(images);
	}

}
