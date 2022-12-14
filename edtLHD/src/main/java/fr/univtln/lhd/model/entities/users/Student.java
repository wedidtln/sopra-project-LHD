package fr.univtln.lhd.model.entities.users;

import fr.univtln.lhd.model.entities.slots.Group;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class of User that is Student
 */
@Getter
public class Student extends User {

    private List<Group> studentGroup;
    /**
     * Private constructor use by the factory should not be used
     * ref <code>of</code> methode for parameter meaning
     */
    private Student(String name, String fname, String email,List<Group> studentGroup) {
        super(name, fname, email);
        this.studentGroup = studentGroup;
    }

    /**
     * factory of Student
     * @param name name of a student
     * @param fname first name of a student
     * @param email email of a student
     * @return an instance of Student
     */
    public static Student of(String name, String fname, String email) {
        List<Group> studentGroup = null;
        return new Student(name, fname, email,studentGroup);
    }

    /**
     * Take a group and add it to the list of Group of this student
     * @param group a group to add to this student
     */
    public void add(Group group){
        if (studentGroup != null){
            this.studentGroup.add(group);
        }
        else{
            this.studentGroup = new ArrayList<>();
            this.studentGroup.add(group);
        }
    }

    /**
     * factory of Student
     * @param name name of a student
     * @param fname first name of a student
     * @param email email of a student
     * @param studentGroup Groupe attached to a student
     * @return an instance of Student
     */
    public static Student of(String name, String fname, String email, List<Group> studentGroup) {
        return new Student(name, fname, email,studentGroup);
    }

    /**
     * Take an object and return if the student is equal to it
     * the only important parameter for the test of equality are those of User
     * @param o an object
     * @return a boolean
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * HashCode of student, like the equality the only important parameter are those of User
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString()+"from "+this.getStudentGroup();
    }
}
