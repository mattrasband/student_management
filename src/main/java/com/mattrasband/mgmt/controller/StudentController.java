package com.mattrasband.mgmt.controller;

import com.mattrasband.mgmt.exception.NotFoundException;
import com.mattrasband.mgmt.model.dao.Student;
import com.mattrasband.mgmt.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping(value = "/api/students", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class StudentController {
    private final StudentRepository studentRepository;

    @Autowired
    StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @PostMapping("")
    public Student createStudent(@RequestBody @Valid Student student) {
        return this.studentRepository.save(student);
    }

    @GetMapping("")
    public Iterable<Student> getStudents() {
        return this.studentRepository.findAll();
    }

    @PutMapping("/{student}")
    public Student updateStudent(@PathVariable("student") Student existingStudent,
                                                                                @RequestBody @Valid Student update) {
        if (existingStudent == null) {
            log.debug("Attempt to update a non-existing student, bailing.");
            throw new NotFoundException("Student not found.");
        }

        log.debug("Updating student {}", existingStudent.getId());

        existingStudent.setFirst(update.getFirst());
        existingStudent.setLast(update.getLast());

        return this.studentRepository.save(existingStudent);
    }

    @DeleteMapping("/{student}")
    public void deleteStudent(@PathVariable("student") Student student) {
        if (student == null) {
            throw new NotFoundException("Not Found");
        }
        this.studentRepository.delete(student);
    }

    @GetMapping("/{student}")
    public Student getStudent(@PathVariable("student") Student student) {
        if (student == null) {
            throw new NotFoundException("Not Found.");
        }
        return student;
    }
}
