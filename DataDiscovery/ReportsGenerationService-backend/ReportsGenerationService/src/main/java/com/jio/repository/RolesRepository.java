package com.jio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jio.entity.Roles;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Integer> {

	Optional<Roles> findRoleByRoleValue(Integer roleValue);

	Optional<Roles> findByRoleName(String name);
}
