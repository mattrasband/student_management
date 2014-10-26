package com.mattrasband.mgmt.controller;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.mattrasband.mgmt.exception.BadRequestException;
import com.mattrasband.mgmt.exception.NotFoundException;
import com.mattrasband.mgmt.exception.RepositoryException;
import com.mattrasband.mgmt.model.Student;
import com.mattrasband.mgmt.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping(value = "/api/students", produces = MediaType.APPLICATION_JSON_VALUE)
public class StudentController {
    private static final Logger LOG = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentRepository studentRepo;

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "")
    public ResponseEntity<Student> createStudent(@RequestBody Student body) {
        if (Strings.isNullOrEmpty(body.getFirst()) || Strings.isNullOrEmpty(body.getLast())) {
            LOG.warn("Bad creation request: " + body.toString());
            throw new BadRequestException("Invalid/Missing fields");
        }

        // Users cannot provide their own ID.
        body.setId(UUID.randomUUID().toString());
        studentRepo.addStudent(body);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "")
    public ResponseEntity<List<Student>> getStudents() {
        return new ResponseEntity<>(studentRepo.getStudents(), HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(method = {RequestMethod.PUT}, value = "/{studentId}")
    public ResponseEntity<Student> updateStudent(@PathVariable("studentId") String studentId,
                                                 @RequestBody Student body) {
        LOG.debug(String.format("Updating student %s", studentId));

        if (studentRepo.getStudent(studentId) == null) {
            throw new NotFoundException(String.format("Student '%s' not found.", studentId));
        }

        if (Strings.isNullOrEmpty(body.getLast()) || Strings.isNullOrEmpty(body.getFirst())) {
            throw new BadRequestException("Invalid/Missing fields in the request.");
        }

        // IDs are not editable.
        body.setId(studentId);

        studentRepo.updateStudent(studentId, body);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(method = {RequestMethod.DELETE}, value = "/{studentId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteStudent(@PathVariable("studentId") String studentId) {
        if (studentRepo.getStudent(studentId) == null) {
            throw new NotFoundException(String.format("Student '%s' not found.", studentId));
        }

        studentRepo.deleteStudent(studentId);
    }

    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET}, value = "/{studentId}")
    public ResponseEntity<Student> getStudent(@PathVariable("studentId") String studentId) {
        Student student = studentRepo.getStudent(studentId);
        if (student == null) {
            throw new NotFoundException(String.format("Student '%s' not found.", studentId));
        }

        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<Object> handleSqlError(HttpServletRequest req, Exception e) {
        LOG.error(String.format("SQL Error: %s - %s", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        Map<String, String> reply = ImmutableMap.<String, String>builder()
                .put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .put("message", e.getMessage())
                .build();
        return new ResponseEntity<>(reply, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
