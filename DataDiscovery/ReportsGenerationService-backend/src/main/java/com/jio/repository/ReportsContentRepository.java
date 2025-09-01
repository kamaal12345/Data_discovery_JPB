package com.jio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jio.entity.ReportsContent;

@Repository
public interface ReportsContentRepository extends JpaRepository<ReportsContent, Integer> {

}
