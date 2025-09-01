package com.jio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jio.entity.ModificationHistory;

@Repository
public interface ModificationHistoryRepository extends JpaRepository<ModificationHistory, Integer> {

	List<ModificationHistory> findByReportId(Integer reportId);

	Optional<ModificationHistory> findAllByReportId(Integer reportId);

}
