package com.jio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jio.dto.PropertyValuesDto;
import com.jio.entity.PropertyValues;
import com.jio.entity.UserCustom;

@Repository
public interface PropertyValuesRepository extends JpaRepository<PropertyValues, Integer> {

	@Query(value = "select * from jio_lookup_code_val  where jio_lookup_code_val.lookup_code_set_id = :id", nativeQuery = true)
	List<PropertyValues> getPropertyValuesListByIdLookupCodeSetId(Integer id);

	List<PropertyValues> findByLookupCodeSetId(Integer lookupCodeSetId);

	public List<PropertyValues> findByLookupCodeSetId(int lookupCodeSetId);

	public void save(PropertyValuesDto propertyValues);

	@Query("SELECT NEW com.jio.entity.UserCustom(u.userId, u.employeeId, u.firstName, u.lastName) "
			+ "FROM User u WHERE u.designation = :valuefound")
	List<UserCustom> findByDesignation(Integer valuefound);

	@Query("select p.value from PropertyValues p  where p.description= :designation")
	Integer findByDescription(String designation);

}
