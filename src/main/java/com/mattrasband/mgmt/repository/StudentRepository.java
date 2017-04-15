package com.mattrasband.mgmt.repository;

import com.mattrasband.mgmt.model.dao.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author matt
 */
@Repository
public interface StudentRepository extends CrudRepository<Student, Integer> {
}
