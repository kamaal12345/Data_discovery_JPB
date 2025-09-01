package com.jio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jio.entity.Images;

@Repository
public interface ImageRepository extends JpaRepository<Images, Integer> {

	boolean existsByName(String name);

	boolean existsByDataUrl(String dataUrl);

}
