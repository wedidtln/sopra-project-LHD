package fr.univtln.lhd.model.entities.dao.users;

import fr.univtln.lhd.exceptions.IdException;
import fr.univtln.lhd.model.entities.slots.Group;
import fr.univtln.lhd.model.entities.users.Student;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class StudentDAOTest {
    public static final StudentDAO dao = StudentDAOTest.getDAO();

    public static StudentDAO getDAO() {
        return StudentDAO.getInstance();
    }

    private Student getRandomNewStudent(){
        return Student.of("UnitTest","UnitTestFirstName",
                "UnitTestName.Firstname"+Math.random()+"@email.com");
    }

    private Student getRandomNewStudentWithGroup(){
        Group groupstudent = Group.getInstance("L1");
        Student student = Student.of("UnitTest","UnitTestFirstName",
                "UnitTestName.Firstname"+Math.random()+"@email.com");//A student is a new one if is email is new
        student.add(groupstudent);
        return student;
    }

    private Student getTheTestStudent(){
        return Student.of("TheTestStudent","UnitTestFirstName",
                "UnitTestName.Firstname@email.com");
    }

    @Test
    void CreateADAO(){
        Assertions.assertNotNull(dao);
    }

    @Test
    void addNewStudent() throws SQLException {
        Student student = getRandomNewStudent();
        int oldsize = dao.getAll().size();
        dao.save(student,"1234");
        assertEquals(oldsize+1,dao.getAll().size());
        dao.delete(student);
    }

    //@Test Need to fix circular import
    void addNewStudentWithGroup() throws SQLException {
        Student student = getRandomNewStudentWithGroup();
        int oldsize = dao.getAll().size();
        dao.save(student,"1234");
        assertEquals(oldsize+1,dao.getAll().size());
        dao.delete(student);
    }

    @Test
    void updateAstudent() throws IdException, SQLException {
        Student student = getRandomNewStudent();
        Student student1 = Student.of(student.getName()+"1",student.getFname()+"1",student.getEmail()+"1");
        dao.save(student,"1234");
        student1.setId(student.getId());
        dao.update(student1);
        assertEquals(dao.get(student.getId()).orElseThrow(AssertionError::new),student1);
    }

    @Test
    void addSameStudent() throws SQLException {
        Student student = getTheTestStudent();
        dao.save(student,"1234");
        int oldsize = dao.getAll().size();
        dao.save(student,"1234");
        assertEquals(oldsize,dao.getAll().size());
    }


    @Test
    void deleteTheStudent() throws SQLException {
        Student student = getRandomNewStudent();
        dao.save(student,"1234");
        int oldsize = dao.getAll().size();
        dao.delete(student);
        assertEquals(oldsize-1,dao.getAll().size());
    }

}