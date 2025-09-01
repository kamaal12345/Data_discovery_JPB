package com.jio.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jio.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);
	
	@Query("""
		    SELECT u FROM User u
		    WHERE (
		        LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
		        LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
		        LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
		        LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
		        u.mobilePhone LIKE CONCAT('%', :searchText, '%')
		    )
		    AND u.metaStatus = true
		""")
		Page<User> search(@Param("searchText") String searchText, Pageable pageable);

	
	@Query("SELECT u FROM User u WHERE u.status = :status")
	Page<User> findUserByStatus(Boolean status, Pageable pageable);
	
	@Query("select u from User u where u.metaStatus = true and (:status is null or u.status=:status)")
	Page<User> findUserByMetaStatus(Boolean status,Pageable pageable);
	
	@Query("""
		    SELECT COUNT(DISTINCT u) 
		    FROM User u 
		    JOIN u.roles r 
		    WHERE r.roleName <> 'Super Admin'
		""")
		long countUsersExcludingSuperAdmin();

		@Query("""
		    SELECT COUNT(DISTINCT u) 
		    FROM User u 
		    JOIN u.roles r 
		    WHERE u.status = true 
		    AND r.roleName <> 'Super Admin'
		""")
		long countActiveUsersExcludingSuperAdmin();
		
		
	Optional<User> findUserByUsername(String username);
	User getLoggedInByUsername(String username);


}
