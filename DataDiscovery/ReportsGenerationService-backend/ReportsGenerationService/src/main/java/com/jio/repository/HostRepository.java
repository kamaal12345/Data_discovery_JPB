package com.jio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jio.entity.Host;

@Repository
public interface HostRepository extends JpaRepository<Host, Integer> {

	boolean existsByIp(String ip);

	List<Host> findByStatus(Integer status);
}
