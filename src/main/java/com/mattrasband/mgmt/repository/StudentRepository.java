package com.mattrasband.mgmt.repository;


import com.google.common.collect.ImmutableList;
import com.mattrasband.mgmt.exception.RepositoryException;
import com.mattrasband.mgmt.model.Student;
import com.mattrasband.mgmt.service.ServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;

@Repository
public class StudentRepository {
    private static final String DBCONN = String.format("jdbc:postgresql://%s/%s",
            ServiceProperties.getDbHost(), ServiceProperties.getDbDatabase());
    private static final Logger LOG = LoggerFactory.getLogger(StudentRepository.class);

    private final Connection conn;

    /**
     * Persistence store for student data.
     */
    public StudentRepository() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(DBCONN, ServiceProperties.getDbUser(), ServiceProperties.getDbPass());
            init();
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to load database driver.");
            throw new RepositoryException("Unable to load database driver.", e);
        } catch (SQLException e) {
            LOG.error("Unable to connect to the database", e);
            throw new RepositoryException("Unable to connect to the database.", e);
        }
    }

    /**
     * Prepare the database for storage, this includes adding tables and migrating if necessary.
     *
     * @throws SQLException
     */
    private void init() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS STUDENTS (" +
                "id TEXT PRIMARY KEY NOT NULL," +
                "first TEXT NOT NULL," +
                "last TEXT NOT NULL);";
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * Add a validated student to the data store.
     *
     * @param student Student to add to the data store.
     */
    public void addStudent(Student student) {
        try {
            String sql = "INSERT INTO STUDENTS(id, first, last) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, student.getId());
                stmt.setString(2, student.getFirst());
                stmt.setString(3, student.getLast());
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Unable to add the student.", e);
        }
    }

    /**
     * Get all students stored.
     *
     * @return List of students
     */
    public List<Student> getStudents() {
        try {
            List<Student> students;

            try (Statement stmt = conn.createStatement()) {
                String sql = "SELECT * FROM STUDENTS;";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    students = resultSetToStudents(rs);
                }
            }

            return students;
        } catch (SQLException e) {
            throw new RepositoryException("Unable to get all students.", e);
        }
    }

    /**
     * Get a specific student.
     *
     * @param studentId Student ID to get.
     * @return Found student, or null if they could not be found.
     */
    public Student getStudent(String studentId) {
        try {
            String sql = "SELECT * FROM STUDENTS WHERE id = ?";
            Student student = null;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                List<Student> students = resultSetToStudents(rs);
                // Length of 1 will be enforced via primary key, it won't be
                // possible for non-unique keys to exist.
                if (students.size() > 0) {
                    student = students.get(0);
                }
            }
            return student;
        } catch (SQLException e) {
            throw new RepositoryException("Unable to get student with ID " + studentId, e);
        }
    }

    /**
     * Delete a specific student.
     *
     * @param studentId Student ID to delete.
     */
    public void deleteStudent(String studentId) {
        try {
            String sql = "DELETE FROM STUDENTS WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error deleting student with ID " + studentId, e);
        }
    }

    /**
     * Update data on a student.
     *
     * @param studentId Student ID to update
     * @param student Updated student.
     */
    public void updateStudent(String studentId, Student student) {
        try {
            String sql = "UPDATE STUDENTS set first = ?, last = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, student.getFirst());
                stmt.setString(2, student.getLast());
                stmt.setString(3, studentId);
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error updating student with ID " + studentId, e);
        }
    }

    /**
     * Extract all students from a query result.
     *
     * @param rs Result set to get items from.
     * @return List of students extracted
     * @throws SQLException
     */
    private List<Student> resultSetToStudents(ResultSet rs) throws SQLException {
        ImmutableList.Builder<Student> students = ImmutableList.builder();

        while (rs.next()) {
            Student s = new Student();
            s.setId(rs.getString("id"));
            s.setLast(rs.getString("last"));
            s.setFirst(rs.getString("first"));
            students.add(s);
        }

        return students.build();
    }
}
