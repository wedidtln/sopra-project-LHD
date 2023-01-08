package fr.univtln.lhd.facade;

import fr.univtln.lhd.exceptions.IdException;
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
import org.threeten.extra.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * facade of the entities/dao and the ihm
 */
@Slf4j
public class Schedule implements Observable {
    public static final String SLOT_EVENT = "slotEvent";
    private static final String ERR_MSG_DELETE_UPDATE = "this entity doesn't seem persisted";

    private static final Schedule schedule = new Schedule();
    private static Map<String, List<Observer>> observerMap = new HashMap<>();

    /**
     * Get Professor of Database via email & password
     *
     * @param email    Email of the professor
     * @param password password of the professor
     * @return Professor entity if exist in database, otherwise return null.
     */
    public static Optional<Professor> getProfessorFromAuth(String email, String password) {
        ProfessorDAO dao = ProfessorDAO.of();
        Professor professor = null;
        try {
            professor = dao.get(email, password).orElseThrow(SQLException::new);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(professor);
    }

    /**
     * Get Admin of Database via email & password
     *
     * @param email    Email of the admin
     * @param password password of the admin
     * @return Admin entity if exist in database, otherwise return null.
     */
    public static Optional<Admin> getAdminFromAuth(String email, String password) {
        AdminDAO dao = AdminDAO.of();
        Admin admin = null;
        try {
            admin = dao.get(email, password).orElseThrow(SQLException::new);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(admin);
    }

    /**
     * Take a user(Student or Professor) and two LocalDate(start/end) and return every slot that the user is
     * in in the timerange of the two LocalDate
     * The user must be persisted or will return an empty array
     *
     * @param user      a persisted User
     * @param timeStart the start of the timerange
     * @param timeEnd   the end of the timerange
     * @return a List of slot that is in the timerange and with the Groups of the User
     */
    public static List<Slot> getSchedule(User user, LocalDate timeStart, LocalDate timeEnd) {
        if (user instanceof Student student) {
            return Schedule.getSchedule(student, timeStart, timeEnd);
        } else if (user instanceof Professor professor) {
            return Schedule.getSchedule(professor, timeStart, timeEnd);
        }
        return new ArrayList<>();
    }

    /**
     * Take a student and two LocalDate(start/end) and return every slot that the student is
     * in in the timerange of the two LocalDate
     * The student must be persisted or will return an empty array
     *
     * @param student   a persisted Student
     * @param timeStart the start of the timerange
     * @param timeEnd   the end of the timerange
     * @return a List of slot that is in the timerange and with the Groups of the Student
     */
    public static List<Slot> getSchedule(Student student, LocalDate timeStart, LocalDate timeEnd) {
        Interval timerange = schedule.getIntervalOf(timeStart, timeEnd);
        return Schedule.getSchedule(student, timerange);
    }


    /**
     * Take a Group and two LocalDate(start/end) and return every slot that the group is
     * in, in the timerange of the two LocalDate
     * The group must be persisted or will return an empty array
     *
     * @param group     a persisted group
     * @param timeStart the start of the timerange
     * @param timeEnd   the end of the timerange
     * @return a List of slot that is in the timerange with this group
     */
    public static List<Slot> getSchedule(Group group, LocalDate timeStart, LocalDate timeEnd) {
        Interval timerange = schedule.getIntervalOf(timeStart, timeEnd);
        return Schedule.getSchedule(group, timerange);
    }

    /**
     * Take a Professor and two LocalDate(start/end) and return every slot that the professor is
     * in, in the timerange of the two LocalDate
     * The professor must be persisted or will return an empty array
     *
     * @param professor a persisted professor
     * @param timeStart the start of the timerange
     * @param timeEnd   the end of the timerange
     * @return a List of slot that is in the timerange with this professor
     */
    public static List<Slot> getSchedule(Professor professor, LocalDate timeStart, LocalDate timeEnd) {
        Interval timerange = schedule.getIntervalOf(timeStart, timeEnd);
        return Schedule.getSchedule(professor, timerange);
    }

    /**
     * Take a professor and an Interval timerange and return every slot within the timerange
     * where the professor is in
     *
     * @param professor a persisted Professor
     * @param timerange An Interval
     * @return a List of slot that is in the timerange and with the Groups of the Professor
     */
    public static List<Slot> getSchedule(Professor professor, Interval timerange) {
        List<Slot> slotList = new ArrayList<>();
        if (professor.getId() < 0) {
            return slotList;
        }
        SlotDAO dao = SlotDAO.getInstance();
        try {
            slotList.addAll(dao.getSlotOfAProfessor(professor, timerange));
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        slotList = schedule.filterEverySlotOutsideTimerange(slotList, timerange);
        return slotList;
    }

    /**
     * Take a student and an Interval timerange and return every slot within the timerange
     * where the student is in
     *
     * @param student   a persisted Student
     * @param timerange An Interval
     * @return a List of slot that is in the timerange and with the Groups of the Student
     */
    public static List<Slot> getSchedule(Student student, Interval timerange) {
        List<Slot> slotList = new ArrayList<>();
        if (student.getId() < 0) {
            return slotList;
        }//Might be an edge case
        SlotDAO dao = SlotDAO.getInstance();
        for (Group group :
                student.getStudentGroup()) {
            try {
                List<Slot> allSlotOfGroup = dao.getSlotOfGroup(group, timerange);
                slotList.addAll(allSlotOfGroup);
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        slotList = schedule.filterEverySlotOutsideTimerange(slotList, timerange);
        return slotList;
    }


    /**
     * Take a Group and an Interval and return a List of the slot with this group
     * inside the interval
     * The group must be persisted or will return an empty array
     *
     * @param group     a persisted group
     * @param timerange an Interval
     * @return a List of slot that is in the timerange and with this group
     */
    public static List<Slot> getSchedule(Group group, Interval timerange) {
        if (group == null || group.getId() < 0) {
            return new ArrayList<>();
        }//Might be an edge case
        SlotDAO dao = SlotDAO.getInstance();
        List<Slot> allSlotOfGroup = new ArrayList<>();
        try {
            allSlotOfGroup = dao.getSlotOfGroup(group);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return schedule.filterEverySlotOutsideTimerange(allSlotOfGroup, timerange);
    }

    /**
     * Round to certain number of decimals
     * Author:jav-rock
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, RoundingMode.FLOOR);
        return bd.floatValue();
    }

    /**
     * Take a group and a slot and return a percentage of
     * the number of slot passed on the number of slot in the future
     * for the group
     * it is a rough estimate and doesn't take account the difference of length between slot
     *
     * @param group
     * @param slot
     * @return
     */
    public static float getPercentageOf(Group group, Slot slot) {
        int passed = 0;
        List<Slot> listOfSlot;
        SlotDAO dao = SlotDAO.getInstance();
        try {
            listOfSlot = dao.getSlotOfGroup(group);
        } catch (SQLException e) {
            return -1;
        }
        for (Slot xslot :
                listOfSlot) {
            if (xslot.getSubject().equals(slot.getSubject()) && xslot.getTimeRange().isBefore(slot.getTimeRange())) {
                passed += xslot.getTimeRange().toDuration().toHours();
            }
        }
        return round((((float) passed) / slot.getSubject().getHourCountMax()), 2);
    }

    /**
     * Returns all Groups currently stored in database
     *
     * @return List of Group
     */
    public static List<Group> getAllGroups() {
        GroupDAO dao = GroupDAO.getInstance();
        List<Group> groupList = new ArrayList<>();
        try {
            groupList = dao.getAll();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return groupList;
    }

    /**
     * Returns all Classrooms currently stored in database
     *
     * @return List of Classroom
     */
    public static List<Classroom> getAllClassrooms() {
        ClassroomDAO dao = ClassroomDAO.getInstance();
        List<Classroom> classroomList = new ArrayList<>();
        try {
            classroomList = dao.getAll();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return classroomList;
    }

    /**
     * Returns all Subjects currently stored in database
     *
     * @return List of Subject
     */
    public static List<Subject> getAllSubjects() {
        SubjectDAO dao = SubjectDAO.getInstance();
        return dao.getAll();
    }

    /**
     * Returns all Professor currently stored in database
     *
     * @return List of Professor
     */
    public static List<Professor> getAllProfessors() {
        ProfessorDAO dao = ProfessorDAO.of();
        return dao.getAll();
    }

    /**
     * Get Student of Database via email & password
     *
     * @param email    Email of the student
     * @param password password of the student
     * @return Student entity if exist in database, otherwise return null.
     */
    public static Optional<Student> getStudentFromAuth(String email, String password) {
        StudentDAO dao = StudentDAO.getInstance();
        Student student = null;
        try {
            student = dao.get(email, password).orElseThrow(SQLException::new);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(student);
    }

    /**
     * Take a slot and persisted it inside the database
     * if any necessary attribute is not persisted
     * the methode will fail
     *
     * @return True if failure False else
     */
    public static boolean addToSchedule(Slot slot) {
        SlotDAO dao = SlotDAO.getInstance();

        if (!schedule.verifyIfPersisted(slot).isEmpty()) {
            log.error("This attribute of slot are not persisted",
                    schedule.verifyIfPersisted(slot));
            return true;
        }
        try {
            dao.save(slot);
        } catch (SQLException e) {
            log.error(e.getMessage());
            return true;
        }

        schedule.notifyChanges(SLOT_EVENT, EventChange.of(EventChange.ChangeType.ADD, slot));
        return false;
    }

    /**
     * Take a Slot and delete it in the database
     *
     * @param slot
     * @return true if failure false if it's a success
     */
    public static boolean deleteInSchedule(Slot slot) {
        if (slot.getId() < 0) {
            log.error("slot is not in the database");//Might be better to replace an exception
            return true;
        }
        SlotDAO dao = SlotDAO.getInstance();
        try {
            dao.delete(slot);
        } catch (SQLException e) {
            log.error("no match for slot of ID:" + slot.getId());
            log.error(e.getMessage());
            return true;
        }
        schedule.notifyChanges(SLOT_EVENT, EventChange.of(EventChange.ChangeType.REMOVE, slot));
        return false;
    }

    /**
     * Take two slot and update the first by the second
     * if the first one is not in the database, the methode
     * will fail
     *
     * @param oldSlot the old slot, persisted
     * @param newSlot the new one, not yet persisted
     * @return true if failure false if it's a success
     */
    public static boolean updateInSchedule(Slot oldSlot, Slot newSlot) {
        SlotDAO slotDAO = SlotDAO.getInstance();
        ProfessorDAO professorDAO = ProfessorDAO.of();
        GroupDAO groupDAO = GroupDAO.getInstance();

        if (oldSlot.getId() < 0) {
            log.error("slot [" + oldSlot.toString() +
                    "]\nis not in database, furthermore cannot update" +
                    " this slot");
            return true;
        }
        try {
            newSlot.setId(oldSlot.getId());
            slotDAO.update(newSlot);

            if (professorDAO.getProfessorOfSlots(newSlot.getId()).isEmpty() && !newSlot.getProfessors().isEmpty())
                professorDAO.save(newSlot.getId(), new long[]{newSlot.getProfessors().get(0).getId()});
            else if (!newSlot.getProfessors().isEmpty())
                professorDAO.update(newSlot.getId(), newSlot.getProfessors().get(0).getId());
            else if (!oldSlot.getProfessors().isEmpty())
                professorDAO.delete(newSlot.getId(), oldSlot.getProfessors().get(0).getId());

            if (groupDAO.getGroupOfSlot(newSlot.getId()).isEmpty() && !newSlot.getGroup().isEmpty())
                groupDAO.save(newSlot.getId(), new long[]{newSlot.getGroup().get(0).getId()});
            else if (!newSlot.getGroup().isEmpty())
                groupDAO.update(newSlot.getId(), newSlot.getGroup().get(0).getId());
        } catch (IdException | SQLException e) {
            log.error(e.getMessage());
            return true;
        }
        schedule.notifyChanges(SLOT_EVENT, EventChange.of(EventChange.ChangeType.MODIFY, newSlot));
        return false;
    }

    /**
     * Take one String and an Int and return a persisted Group with the right format
     * The year is comprised beteween 0 and 4 0 is L1 and 4 is M2 if the value is not
     * correct is will be ignored
     * if group is failed to be persisted it will return an empty Group
     * eg:(informAtique,5) -> M2 Informatique
     *
     * @param formation
     * @param year
     * @return
     */
    public static Group addGroupInSchedule(String formation, int year) {
        if (year < 0 || year > 4) {
            return addGroupInSchedule(formation);
        }
        String[] intToYear = {"L1", "l2", "l3", "M1", "M2"};
        Group group = Group.getInstance(" " + intToYear[year] + "-" + schedule.upperFollowedByOnlyLower(formation));
        if (Boolean.TRUE.equals(schedule.persistGroup(group))) {//this style of code is sonar...
            schedule.erroMessageForAtt(group.toString());
            return Group.getInstance("");
        }
        return group;
    }

    /**
     * Take a String and an Int and return a persisted Classroom with right format
     * if the classroom is failed to be persisted it will return an empty classroom
     *
     * @param building
     * @param roomNumber
     * @return
     */
    public static Classroom addClassroomInSchedule(String building, int roomNumber) {
        Classroom classroom = Classroom.getInstance(building.toUpperCase() + "." + roomNumber);
        if (Boolean.TRUE.equals(schedule.persistClassroom(classroom))) {
            schedule.erroMessageForAtt(classroom.toString());
            return Classroom.getInstance("");
        }
        return classroom;
    }
//Getter For persisted attribute of Slot

    /**
     * Take a String and an Int and return a persisted Subject with right format
     * if the subject is failed to be persisted it will return an empty subject
     *
     * @param moduleName
     * @param moduleNumber
     * @return
     */
    public static Subject addSubjectInSchedule(String moduleName, int moduleNumber, int numberOfHour) {
        Subject subject = Subject.getInstance(moduleName.substring(0, 1).toUpperCase() + moduleNumber + " " +
                schedule.upperFollowedByOnlyLower(moduleName), numberOfHour);
        if (Boolean.TRUE.equals(schedule.persistSubject(subject))) {
            schedule.erroMessageForAtt(subject.toString());
            return Subject.getInstance("", 0);
        }
        return subject;
    }

    /**
     * Take a String and return a persisted Classroom with right format
     * if the classroom is failed to be persisted it will return an empty classroom
     *
     * @param building
     * @return
     */
    public static Classroom addClassroomInSchedule(String building) {
        Classroom classroom = Classroom.getInstance(building.toUpperCase());
        if (Boolean.TRUE.equals(schedule.persistClassroom(classroom))) {
            schedule.erroMessageForAtt(classroom.toString());
            return Classroom.getInstance("");
        }
        return classroom;
    }

    /**
     * Take one String and return a persisted Group with the right format
     * if group is failed to be persisted it will return an empty Group
     * eg:(informAtique) -> Informatique
     *
     * @param formation
     * @return
     */
    public static Group addGroupInSchedule(String formation) {
        Group group = Group.getInstance(formation.substring(0, 1).toUpperCase() + formation.substring(1).toLowerCase());
        if (Boolean.TRUE.equals(schedule.persistGroup(group))) {
            schedule.erroMessageForAtt(group.toString());
            return Group.getInstance("");
        }
        return group;
    }

    /**
     * Take a String and an Int and return a persisted Subject with right format
     * if the subject is failed to be persisted it will return an empty subject
     *
     * @param moduleName
     * @return
     */
    public static Subject addSubjectInSchedule(String moduleName, int numberOfHour) {
        Subject subject = Subject.getInstance(schedule.upperFollowedByOnlyLower(moduleName), numberOfHour);
        if (Boolean.TRUE.equals(schedule.persistSubject(subject))) {
            schedule.erroMessageForAtt(subject.toString());
            return Subject.getInstance("", 0);
        }
        return subject;
    }

    /**
     * update a Group in the database with a new Group
     *
     * @param oldGroup
     * @param newGroup
     * @return true in case of failure false if it's a success
     */
    public static boolean updateGroupInSchedule(Group oldGroup, Group newGroup) {
        try {
            newGroup.setId(oldGroup.getId());
        } catch (IdException e) {
            log.error(ERR_MSG_DELETE_UPDATE);
            return true;
        }
        GroupDAO dao = GroupDAO.getInstance();
        try {
            dao.update(newGroup);
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

    /**
     * update a Classroom in the database with a new Classroom
     *
     * @param oldClassroom
     * @param newClassroom
     * @return true in case of failure false if it's a success
     */
    public static boolean updateClassroomInSchedule(Classroom oldClassroom, Classroom newClassroom) {
        try {
            newClassroom.setId(oldClassroom.getId());
        } catch (IdException e) {
            log.error(ERR_MSG_DELETE_UPDATE);
            return true;
        }
        ClassroomDAO dao = ClassroomDAO.getInstance();
        try {
            dao.update(newClassroom);
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

    /**
     * update a Subject in the database with a new Subject
     *
     * @param oldSubject
     * @param newSubject
     * @return true in case of failure false if it's a success
     */
    public static boolean updateSubjectInSchedule(Subject oldSubject, Subject newSubject) {
        try {
            newSubject.setId(oldSubject.getId());
        } catch (IdException e) {
            log.error(ERR_MSG_DELETE_UPDATE);
            return true;
        }
        SubjectDAO dao = SubjectDAO.getInstance();
        dao.update(newSubject);
        return false;
    }

    /**
     * Delete a group in the database
     *
     * @param group
     * @return true in case of failure false if it's a success
     */
    public static boolean deleteGroupInSchedule(Group group) {
        if (group.getId() < 0) {
            log.error(ERR_MSG_DELETE_UPDATE);
            return true;
        }
        GroupDAO dao = GroupDAO.getInstance();
        try {
            dao.delete(group);
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

    /**
     * Delete a subject in the database
     *
     * @param subject
     * @return true in case of failure false if it's a success
     */
    public static boolean deleteSubjectInSchedule(Subject subject) {
        if (subject.getId() < 0) {
            log.error(ERR_MSG_DELETE_UPDATE);
            return true;
        }
        SubjectDAO dao = SubjectDAO.getInstance();
        dao.delete(subject);
        return false;
    }

    /**
     * Delete a classroom in the database
     *
     * @param classroom
     * @return true in case of failure false if it's a success
     */
    public static boolean deleteClassroomInSchedule(Classroom classroom) {
        if (classroom.getId() < 0) {
            log.error(ERR_MSG_DELETE_UPDATE);
            return true;
        }
        ClassroomDAO dao = ClassroomDAO.getInstance();
        try {
            dao.delete(classroom);
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

    public static void subscribe(String eventName, Observer observer) {
        schedule.subscribeToEvent(eventName, observer);
    }

    public static void unsubscribe(String eventName, Observer observer) {
        schedule.unsubscribeToEvent(eventName, observer);
    }

    /**
     * return a String with an uppercase letter at the beginning followed by
     * lowercase letter
     *
     * @param str
     * @return
     */
    private String upperFollowedByOnlyLower(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * simple error mesaage to keep the code DRY
     *
     * @param str
     */
    private void erroMessageForAtt(String str) {
        log.error("failed to persist" + str + ",maybe this name is already in" +
                "the database");
    }

    /**
     * Take a group, persisted it and return false in case of
     * success True if not
     *
     * @param group
     * @return
     */
    private Boolean persistGroup(Group group) {
        GroupDAO dao = GroupDAO.getInstance();
        try {
            dao.save(group);
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

    /**
     * Take a subject, persisted it and return false in case of
     * success True if not
     *
     * @param subject
     * @return
     */
    private Boolean persistSubject(Subject subject) {
        SubjectDAO dao = SubjectDAO.getInstance();
        dao.save(subject);
        return false;
    }

    /**
     * Take a classroom, persisted it and return false in case of
     * success True if not
     *
     * @param classroom
     * @return
     */
    private Boolean persistClassroom(Classroom classroom) {
        ClassroomDAO dao = ClassroomDAO.getInstance();
        try {
            dao.save(classroom);
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

    /**
     * Go through every attribute of slot and verify if it is persisted
     * if not it add it to a list and return every
     * state
     *
     * @param slot of potentially not persisted parameter
     */
    private List<String> verifyIfPersisted(Slot slot) {
        List<String> listOfNotpersistedAttribute = new ArrayList<>();
        for (Professor professor :
                slot.getProfessors()) {
            if (professor.getId() < 0) {
                listOfNotpersistedAttribute.add(professor.toString());
            }
        }
        for (Group group :
                slot.getGroup())
            if (group.getId() < 0) {
                listOfNotpersistedAttribute.add(group.toString());
            }
        if (slot.getClassroom().getId() < 0) {
            listOfNotpersistedAttribute.add(slot.getClassroom().toString());
        }
        if (slot.getSubject().getId() < 0) {
            listOfNotpersistedAttribute.add(slot.getSubject().toString());
        }
        return listOfNotpersistedAttribute;
    }

    /**
     * Take a list of Slot and return another List of slot inside the timerange
     * each slot begin or end in the time range which mean that it can overflow
     *
     * @param slotList  list of all the slot
     * @param timerange an Intervalle of the slot to keep
     * @return all the slot of slotList that was inside the timerange
     */
    private List<Slot> filterEverySlotOutsideTimerange(List<Slot> slotList, Interval timerange) {
        List<Slot> slotIntimerange = new ArrayList<>();
        if (slotList.isEmpty()) {
            return slotIntimerange;
        }
        for (Slot slot :
                slotList) {
            if (timerange.overlaps(slot.getTimeRange())) {
                slotIntimerange.add(slot);
            }
        }
        return slotIntimerange;
    }

    /**
     * Take two LocalDate and return an Interval between the two time
     * to be noted LocalDate doesn't have a time therefor each instant
     * start at the beginning of the day 00h00
     * If the two time are on the same day, the whole day is return
     *
     * @param timeStart the starting LocalDate
     * @param timeEnd   the ending LocalDate
     * @return Interval between the
     */
    private Interval getIntervalOf(LocalDate timeStart, LocalDate timeEnd) {
        ZoneId timeZone = ZoneId.systemDefault();
        Instant start = timeStart.atStartOfDay().atZone(timeZone).toInstant();
        Instant end = timeEnd.atStartOfDay().atZone(timeZone).toInstant();
        if (start == end) {
            end = timeEnd.atTime(23, 59, 59).atZone(timeZone).toInstant();
        }
        return Interval.of(start, end);
    }

    @Override
    public void subscribeToEvent(String eventName, Observer observer) {
        observerMap.putIfAbsent(eventName, new ArrayList<>());
        observerMap.get(eventName).add(observer);
    }

    @Override
    public void unsubscribeToEvent(String eventName, Observer observer) {
        if (!observerMap.containsKey(eventName)) return;
        observerMap.get(eventName).remove(observer);
    }

    @Override
    public void notifyChanges(String eventName, EventChange<?> changes) {
        if (!observerMap.containsKey(eventName)) return;

        for (Observer obs : observerMap.get(eventName))
            obs.update(changes);
    }


}
