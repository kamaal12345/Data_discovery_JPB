package com.jio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jio.entity.Properties;

@Repository
public interface PropertiesRepository extends JpaRepository<Properties, Integer> {

	Properties getById(int propertiesidL);

	@Query("select p from Properties p where p.lookupCodeSetValue=:lookupCodeSetValue")
	Properties getIdBylookupCodeSetValue(int lookupCodeSetValue);

}
