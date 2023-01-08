package fr.univtln.lhd.facade;

import fr.univtln.lhd.model.entities.dao.slots.ClassroomDAO;
import fr.univtln.lhd.model.entities.dao.slots.GroupDAO;
import fr.univtln.lhd.model.entities.dao.slots.SlotDAO;
import fr.univtln.lhd.model.entities.dao.slots.SubjectDAO;
import fr.univtln.lhd.model.entities.dao.users.AdminDAO;
import fr.univtln.lhd.model.entities.dao.users.ProfessorDAO;
import fr.univtln.lhd.model.entities.dao.users.StudentDAO;
import fr.univtln.lhd.model.entities.slots.Classroom;
import fr.univtln.lhd.model.entities.slots.Group;
import fr.univtln.lhd.model.entities.slots.Slot;
import fr.univtln.lhd.model.entities.slots.Subject;
import fr.univtln.lhd.model.entities.users.Admin;
import fr.univtln.lhd.model.entities.users.Professor;
import fr.univtln.lhd.model.entities.users.Student;
import fr.univtln.lhd.model.entities.users.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Slf4j
class ScheduleTest {


    private Map<String, Object> mapOfSlot;

    @BeforeEach
    public void GetNewRealSlot() throws SQLException {
        mapOfSlot = new HashMap<>();
        Professor professorTest = Professor.of("test", "test", "ProfessorTest@test.fr" + Math.random(), "testTitle");
        ProfessorDAO professorDAO = ProfessorDAO.of();
        professorDAO.save(professorTest, "test");
        mapOfSlot.put("Professor", professorTest);
        Subject subjectTest = Subject.getInstance("testSubject" + Math.random(), 42);
        SubjectDAO subjectDAO = SubjectDAO.getInstance();
        subjectDAO.save(subjectTest);
        mapOfSlot.put("Subject", subjectTest);
        Group groupTest = Group.getInstance("GroupTest" + Math.random());
        GroupDAO groupDAO = GroupDAO.getInstance();
        groupDAO.save(groupTest);
        mapOfSlot.put("Group", groupTest);
        Student studentTest = Student.of("test", "test", "StudentTest@test.fr" + Math.random());
        StudentDAO studentDAO = StudentDAO.getInstance();
        studentTest.add(groupTest);
        studentDAO.save(studentTest, "test");
        mapOfSlot.put("Student", studentTest);
        Classroom classroomTest = Classroom.getInstance("ClassTest" + Math.random());
        ClassroomDAO classroomDAO = ClassroomDAO.getInstance();
        classroomDAO.save(classroomTest);
        mapOfSlot.put("Classroom", classroomTest);
        List<Group> lgroup = new ArrayList<>();
        lgroup.add(groupTest);
        List<Professor> lprofessor = new ArrayList<>();
        lprofessor.add(professorTest);
        Interval intervalTest = Interval.of(Instant.now(), Duration.ofHours(1));
        Slot slotTest = Slot.getInstance(Slot.SlotType.CM, classroomTest, subjectTest, lgroup, lprofessor, intervalTest);
        mapOfSlot.put("Type", Slot.SlotType.CM);
        SlotDAO slotdao = SlotDAO.getInstance();
        slotdao.save(slotTest);
        mapOfSlot.put("Slot", slotTest);
    }

    @AfterEach
    public void cleanDbFromRealSlot() {
        try {
            ProfessorDAO professorDAO = ProfessorDAO.of();
            professorDAO.delete((Professor) mapOfSlot.get("Professor"));
            SubjectDAO subjectDAO = SubjectDAO.getInstance();
            subjectDAO.delete((Subject) mapOfSlot.get("Subject"));
            GroupDAO groupDAO = GroupDAO.getInstance();
            groupDAO.delete((Group) mapOfSlot.get("Group"));
            StudentDAO studentDAO = StudentDAO.getInstance();
            studentDAO.delete((Student) mapOfSlot.get("Student"));
            ClassroomDAO classroomDAO = ClassroomDAO.getInstance();
            classroomDAO.delete((Classroom) mapOfSlot.get("Classroom"));
            SlotDAO slotdao = SlotDAO.getInstance();
            slotdao.delete((Slot) mapOfSlot.get("Slot"));
        } catch (SQLException e) {
            log.error("did not clean after test:" + e.getMessage());
        }
    }


    @Test
    void getScheduleOfStudentWithLocalDate() throws SQLException {
        Slot slotOfMap = (Slot) mapOfSlot.get("Slot");
        Student studentOfMap = (Student) mapOfSlot.get("Student");
        LocalDate today = LocalDate.now();
        LocalDate tomorow = LocalDate.now().plusDays(1);
        List<Slot> scheduleFetch = Schedule.getSchedule(studentOfMap, today, tomorow);
        Assertions.assertEquals(slotOfMap.getId(), scheduleFetch.get(0).getId());
    }

    @Test
    void getScheduleOfUser() throws SQLException {
        Slot slotOfMap = (Slot) mapOfSlot.get("Slot");
        User userOfMap = (User) mapOfSlot.get("Student");
        LocalDate today = LocalDate.now();
        LocalDate tomorow = LocalDate.now().plusDays(1);
        List<Slot> scheduleFetch = Schedule.getSchedule(userOfMap, today, tomorow);
        Assertions.assertEquals(slotOfMap.getId(), scheduleFetch.get(0).getId());
    }

    @Test
    void getScheduleOfUserProfessor() throws SQLException {
        Slot slotOfMap = (Slot) mapOfSlot.get("Slot");
        User userOfMap = (User) mapOfSlot.get("Professor");
        LocalDate today = LocalDate.now();
        LocalDate tomorow = LocalDate.now().plusDays(1);
        List<Slot> scheduleFetch = Schedule.getSchedule(userOfMap, today, tomorow);
        Assertions.assertEquals(slotOfMap.getId(), scheduleFetch.get(0).getId());
    }

    @Test
    void getScheduleOfNonePersistedStudentWithLocalDate() throws SQLException {
        Slot slotOfMap = (Slot) mapOfSlot.get("Slot");
        Student student = Student.of("Does", "not", "Exist");
        LocalDate today = LocalDate.now();
        LocalDate tomorow = LocalDate.now().plusDays(1);
        List<Slot> scheduleFetch = Schedule.getSchedule(student, today, tomorow);
        Assertions.assertEquals(0, scheduleFetch.size());
    }

    @Test
    void getScheduleOfGroupWithLocalDate() throws SQLException {
        Slot slotOfMap = (Slot) mapOfSlot.get("Slot");
        Group grouOfMap = (Group) mapOfSlot.get("Group");
        LocalDate today = LocalDate.now();
        LocalDate tomorow = LocalDate.now().plusDays(1);
        List<Slot> scheduleFetch = Schedule.getSchedule(grouOfMap, today, tomorow);
        Assertions.assertEquals(slotOfMap.getId(), scheduleFetch.get(0).getId());
    }

    @Test
    void getScheduleOfNonePersistedGroupWithLocalDate() throws SQLException {
        Slot slotOfMap = (Slot) mapOfSlot.get("Slot");
        Group group = Group.getInstance("Noteexiste");
        LocalDate today = LocalDate.now();
        LocalDate tomorow = LocalDate.now().plusDays(1);
        List<Slot> scheduleFetch = Schedule.getSchedule(group, today, tomorow);
        Assertions.assertEquals(0, scheduleFetch.size());
    }

    @Test
    void getScheduleOfGroup() throws SQLException {
        Slot slotOfMap = (Slot) mapOfSlot.get("Slot");
        Group groupOfMap = (Group) mapOfSlot.get("Group");
        List<Slot> scheduleFetch = Schedule.getSchedule(groupOfMap, slotOfMap.getTimeRange());
        Assertions.assertEquals(slotOfMap.getId(), scheduleFetch.get(0).getId());
    }

    @Test
    void shouldReturnTheTestStudent() throws SQLException {
        String email = "StudentTest@test.fr" + Math.random();
        Student studentTestAuth = Student.of("test", "test", email);
        StudentDAO studentDAO = StudentDAO.getInstance();
        studentDAO.save(studentTestAuth, "test");
        Optional<Student> resultGetter = Schedule.getStudentFromAuth(email, "test");
        Assertions.assertEquals(studentTestAuth, resultGetter.orElseThrow(AssertionError::new));
        studentDAO.delete(studentTestAuth);
    }

    @Test
    void getProfessorFromAuth() {
        String email = "professorTest@test.fr" + Math.random();
        Professor professorTestAuth = Professor.of("test", "test", email, "testTitle");
        ProfessorDAO DAO = ProfessorDAO.of();
        DAO.save(professorTestAuth, "test");
        Optional<Professor> resultGetter = Schedule.getProfessorFromAuth(email, "test");
        Assertions.assertEquals(professorTestAuth, resultGetter.orElseThrow(AssertionError::new));
        DAO.delete(professorTestAuth);
    }

    @Test
    void shouldNotAddSlotToDatabase() {
        List<Group> groups = new ArrayList<Group>();
        groups.add(Group.getInstance("groupe"));
        List<Professor> professors = new ArrayList<Professor>();
        professors.add(Professor.of("n", "f", "m" + Math.random(), "t"));
        Slot newSlot = Slot.getInstance(Slot.SlotType.CM,
                Classroom.getInstance("ClasssUnitTest"),
                Subject.getInstance("UnitTestaddSlotFacade", 42),
                groups,
                professors,
                Interval.of(Instant.now(), Duration.ofHours(1)));
        Assertions.assertTrue(Schedule.addToSchedule(newSlot));
    }

    @Test
    void shouldAddSlotToDatabase() throws SQLException {
        List<Group> groups = new ArrayList<Group>();
        groups.add((Group) mapOfSlot.get("Group"));
        List<Professor> professors = new ArrayList<Professor>();
        professors.add((Professor) mapOfSlot.get("Professor"));
        Slot newSlot = Slot.getInstance(Slot.SlotType.CM,
                (Classroom) mapOfSlot.get("Classroom"),
                (Subject) mapOfSlot.get("Subject"),
                groups,
                professors,
                Interval.of(Instant.now().plusSeconds(3600), Duration.ofHours(1)));
        Schedule.addToSchedule(newSlot);
        SlotDAO dao = SlotDAO.getInstance();
        Optional<Slot> slotDb = null;
        try {
            slotDb = dao.get(newSlot.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(slotDb.get().getId(), newSlot.getId());
        dao.delete(newSlot);
    }

    @Test
    void shouldDeleteSlotFromDB() throws SQLException {
        List<Group> groups = new ArrayList<Group>();
        groups.add((Group) mapOfSlot.get("Group"));
        List<Professor> professors = new ArrayList<Professor>();
        professors.add((Professor) mapOfSlot.get("Professor"));
        Slot newSlot = Slot.getInstance(Slot.SlotType.CM,
                (Classroom) mapOfSlot.get("Classroom"),
                (Subject) mapOfSlot.get("Subject"),
                groups,
                professors,
                Interval.of(Instant.now().plusSeconds(3600), Duration.ofHours(1)));
        SlotDAO dao = SlotDAO.getInstance();
        dao.save(newSlot);
        int oldSize = dao.getAll().size();
        Schedule.deleteInSchedule(newSlot);
        int newSize = dao.getAll().size();
        Assertions.assertEquals(oldSize - 1, newSize);
    }


    @Test
    void shouldUpdateSlotInDB() throws SQLException {
        List<Group> groups = new ArrayList<Group>();
        groups.add((Group) mapOfSlot.get("Group"));
        List<Professor> professors = new ArrayList<Professor>();
        professors.add((Professor) mapOfSlot.get("Professor"));
        Slot newSlot = Slot.getInstance(Slot.SlotType.TD,
                (Classroom) mapOfSlot.get("Classroom"),
                (Subject) mapOfSlot.get("Subject"),
                groups,
                professors,
                Interval.of(Instant.now().plusSeconds(3600), Duration.ofHours(1)));
        SlotDAO dao = SlotDAO.getInstance();
        Schedule.updateInSchedule((Slot) mapOfSlot.get("Slot"), newSlot);
        Optional<Slot> slotfromDb = dao.get(((Slot) mapOfSlot.get("Slot")).getId());
        Assertions.assertNotEquals(slotfromDb.orElseThrow(AssertionError::new).getType(), mapOfSlot.get("Type"));
    }

    @Test
    void shouldNotUpdateSlotInDB() throws SQLException {
        List<Group> groups = new ArrayList<Group>();
        groups.add((Group) mapOfSlot.get("Group"));
        List<Professor> professors = new ArrayList<Professor>();
        professors.add((Professor) mapOfSlot.get("Professor"));
        Slot newSlot = Slot.getInstance(Slot.SlotType.TD,
                (Classroom) mapOfSlot.get("Classroom"),
                (Subject) mapOfSlot.get("Subject"),
                groups,
                professors,
                Interval.of(Instant.now().plusSeconds(3600), Duration.ofHours(1)));
        Assertions.assertTrue(Schedule.updateInSchedule(newSlot, newSlot));
    }

    @Test
    void shouldNotDeleteSlotFromDB() throws SQLException {
        List<Group> groups = new ArrayList<Group>();
        groups.add((Group) mapOfSlot.get("Group"));
        List<Professor> professors = new ArrayList<Professor>();
        professors.add((Professor) mapOfSlot.get("Professor"));
        Slot newSlot = Slot.getInstance(Slot.SlotType.CM,
                (Classroom) mapOfSlot.get("Classroom"),
                (Subject) mapOfSlot.get("Subject"),
                groups,
                professors,
                Interval.of(Instant.now().plusSeconds(3600), Duration.ofHours(1)));
        SlotDAO dao = SlotDAO.getInstance();
        int oldSize = dao.getAll().size();
        Assertions.assertTrue(Schedule.deleteInSchedule(newSlot));
    }

    @Test
    void getAdminFromAuth() {
        String email = "adminTest@test.fr" + Math.random();
        Admin adminTestAuth = Admin.of("test", "test", email, "testFaculty");
        AdminDAO studentDAO = AdminDAO.of();
        studentDAO.save(adminTestAuth, "test");
        Optional<Admin> resultGetter = Schedule.getAdminFromAuth(email, "test");
        Assertions.assertEquals(adminTestAuth, resultGetter.get());
        studentDAO.delete(adminTestAuth);
    }

    /*
    @Test
    void subscribeTest(){
        Observer observer = new Observer() {
            @Override
            public void udpate(List<EventChange<?>> changes) {
                Assertions.assertEquals(0,changes.size());
            }
        };
        Schedule.subscribe("event",observer);
        Schedule instance = new Schedule();
        instance.notifyChanges("event",new ArrayList<>());
    }
     */


    @Test
    void addSubject() {
        SubjectDAO dao = SubjectDAO.getInstance();
        int oldSize = dao.getAll().size();
        Subject newSubject = Schedule.addSubjectInSchedule("Test", 42);
        Assertions.assertEquals(oldSize + 1, dao.getAll().size());
        dao.delete(newSubject);
    }

    @Test
    void deleteSubject() {
        SubjectDAO dao = SubjectDAO.getInstance();
        int oldSize = dao.getAll().size();
        Subject newSubject = Schedule.addSubjectInSchedule("Test", 42);
        Schedule.deleteSubjectInSchedule(newSubject);
        Assertions.assertEquals(oldSize, dao.getAll().size());
        dao.delete(newSubject);
    }

    @Test
    void updateSubject() {
        SubjectDAO dao = SubjectDAO.getInstance();
        int oldSize = dao.getAll().size();
        Subject newSubject = Subject.getInstance("tttt", 42);
        Schedule.updateSubjectInSchedule((Subject) mapOfSlot.get("Subject"), newSubject);
        Assertions.assertEquals(((Subject) mapOfSlot.get("Subject")).getId(), newSubject.getId());
        Schedule.deleteSubjectInSchedule(newSubject);
    }

    @Test
    void addSubjectWithMoreParameter() {
        SubjectDAO dao = SubjectDAO.getInstance();
        int oldSize = dao.getAll().size();
        Subject newSubject = Schedule.addSubjectInSchedule("Test", 23, 42);
        Assertions.assertEquals(oldSize + 1, dao.getAll().size());
        dao.delete(newSubject);
    }

    @Test
    void addGroup() throws SQLException {
        GroupDAO dao = GroupDAO.getInstance();
        int oldSize = dao.getAll().size();
        Group newGroup = Schedule.addGroupInSchedule("Test");
        Assertions.assertEquals(oldSize + 1, dao.getAll().size());
        dao.delete(newGroup);
    }

    @Test
    void addGroupWithParameter() throws SQLException {
        GroupDAO dao = GroupDAO.getInstance();
        int oldSize = dao.getAll().size();
        Group newGroup = Schedule.addGroupInSchedule("Test", 3);
        Assertions.assertEquals(oldSize + 1, dao.getAll().size());
        dao.delete(newGroup);
    }

    @Test
    void deleteGroup() throws SQLException {
        GroupDAO dao = GroupDAO.getInstance();
        int oldSize = dao.getAll().size();
        Group newGroup = Schedule.addGroupInSchedule("Test", 3);
        Schedule.deleteGroupInSchedule(newGroup);
        Assertions.assertEquals(oldSize, dao.getAll().size());
    }

    @Test
    void updateGroup() throws SQLException {
        GroupDAO dao = GroupDAO.getInstance();
        int oldSize = dao.getAll().size();
        Group newGroup = Group.getInstance("tettt");
        Schedule.updateGroupInSchedule((Group) mapOfSlot.get("Group"), newGroup);
        Assertions.assertEquals(((Group) mapOfSlot.get("Group")).getId(), newGroup.getId());
        Schedule.deleteGroupInSchedule(newGroup);
    }

    @Test
    void updateClassroom() throws SQLException {
        ClassroomDAO dao = ClassroomDAO.getInstance();
        int oldSize = dao.getAll().size();
        Classroom newClassroom = Classroom.getInstance("testtt");
        Schedule.updateClassroomInSchedule((Classroom) mapOfSlot.get("Classroom"), newClassroom);
        Assertions.assertEquals(((Classroom) mapOfSlot.get("Classroom")).getId(), newClassroom.getId());
        dao.delete(newClassroom);
    }

    @Test
    void addGroupWithWrongParameter() throws SQLException {
        GroupDAO dao = GroupDAO.getInstance();
        int oldSize = dao.getAll().size();
        Group newGroup = Schedule.addGroupInSchedule("Test", 5);
        dao.delete(newGroup);
        Assertions.assertEquals("T", newGroup.getName().substring(0, 1));
    }

    @Test
    void addGroupWithWrong2Parameter() throws SQLException {
        GroupDAO dao = GroupDAO.getInstance();
        int oldSize = dao.getAll().size();
        Group newGroup = Schedule.addGroupInSchedule("Test", -1);
        dao.delete(newGroup);
        Assertions.assertEquals("T", newGroup.getName().substring(0, 1));
    }

    @Test
    void addClassroom() throws SQLException {
        ClassroomDAO dao = ClassroomDAO.getInstance();
        int oldSize = dao.getAll().size();
        Classroom newClassroom = Schedule.addClassroomInSchedule("Test");
        Assertions.assertEquals(oldSize + 1, dao.getAll().size());
        dao.delete(newClassroom);
    }

    @Test
    void addClassroomWithParameter() throws SQLException {
        ClassroomDAO dao = ClassroomDAO.getInstance();
        int oldSize = dao.getAll().size();
        Classroom newClassroom = Schedule.addClassroomInSchedule("Test", 42);
        Assertions.assertEquals(oldSize + 1, dao.getAll().size());
        dao.delete(newClassroom);
    }

    @Test
    void deleteClassroom() throws SQLException {
        ClassroomDAO dao = ClassroomDAO.getInstance();
        int oldSize = dao.getAll().size();
        Classroom newClassroom = Schedule.addClassroomInSchedule("Test", 42);
        Schedule.deleteClassroomInSchedule(newClassroom);
        Assertions.assertEquals(oldSize, dao.getAll().size());
    }

    @Test
    void shouldgetRatio() throws SQLException {
        List<Group> groups = new ArrayList<Group>();
        groups.add((Group) mapOfSlot.get("Group"));
        List<Professor> professors = new ArrayList<Professor>();
        professors.add((Professor) mapOfSlot.get("Professor"));
        Slot newSlot = Slot.getInstance(Slot.SlotType.CM,
                (Classroom) mapOfSlot.get("Classroom"),
                (Subject) mapOfSlot.get("Subject"),
                groups,
                professors,
                Interval.of(Instant.now().plusSeconds(3600), Duration.ofHours(1)));
        Schedule.addToSchedule(newSlot);
        SlotDAO dao = SlotDAO.getInstance();
        Optional<Slot> slotDb = null;
        try {
            slotDb = dao.get(newSlot.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals((float) 0.02, Schedule.getPercentageOf((Group) mapOfSlot.get("Group"), newSlot));
        dao.delete(newSlot);
    }

    @Test
    void checkScheduleAllGroupsIntegrity() {
        GroupDAO dao = GroupDAO.getInstance();
        try {
            int expectedSize = dao.getAll().size();
            int actualSize = Schedule.getAllGroups().size();
            Assertions.assertEquals(expectedSize, actualSize);
        } catch (SQLException e) {
            throw new AssertionError();
        }
    }

    @Test
    void checkScheduleAllClassroomsIntegrity() {
        ClassroomDAO dao = ClassroomDAO.getInstance();
        try {
            int expectedSize = dao.getAll().size();
            int actualSize = Schedule.getAllClassrooms().size();
            Assertions.assertEquals(expectedSize, actualSize);
        } catch (SQLException e) {
            throw new AssertionError();
        }
    }

    @Test
    void checkScheduleAllSubjectsIntegrity() {
        SubjectDAO dao = SubjectDAO.getInstance();
        int expectedSize = dao.getAll().size();
        int actualSize = Schedule.getAllSubjects().size();
        Assertions.assertEquals(expectedSize, actualSize);
    }

    @Test
    void checkScheduleAllProfessorsIntegrity() {
        ProfessorDAO dao = ProfessorDAO.of();
        int expectedSize = dao.getAll().size();
        int actualSize = Schedule.getAllProfessors().size();
        Assertions.assertEquals(expectedSize, actualSize);
    }

}