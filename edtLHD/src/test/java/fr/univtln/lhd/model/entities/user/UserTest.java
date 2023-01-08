package fr.univtln.lhd.model.entities.user;

import fr.univtln.lhd.exceptions.IdException;
import fr.univtln.lhd.model.entities.slots.Group;
import fr.univtln.lhd.model.entities.users.Admin;
import fr.univtln.lhd.model.entities.users.Student;
import fr.univtln.lhd.model.entities.users.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


class UserTest {

    public User getInstanceOfUser(){
        return Admin.of("Name","FirstName","Name.Firstname@emal.com","St");
    }

    public List<Student> getListOfStudents(){
        List<Student> students = new ArrayList<>();
        return students;
    }


    public  User getAnotherInstanceOfUser(){
        return Admin.of("Name2","FirstName2","Name2.First2name@emal.com","St");
    }

    @Test
    void IdShouldBeNegative(){
        User user1 = this.getInstanceOfUser();
        Assertions.assertEquals(-1,user1.getId());
    }

    @Test
    void IdShouldBe42() throws IdException {
        User user1 = this.getInstanceOfUser();
        user1.setId(42);
        Assertions.assertEquals(42,user1.getId());
    }

    @Test
    void ShouldThrowAnIdException(){
        User user1 = this.getInstanceOfUser();
        IdException thrown = Assertions.assertThrows(
                IdException.class,() ->user1.setId(-3),"should throw id error"
        );
        Assertions.assertFalse(thrown.getMessage().contains("should throw id error"));
    }

    @Test
    void EqualIsReflexive(){
        User user1 = this.getInstanceOfUser();
        Assertions.assertEquals(user1,user1);
    }

    @Test
    void HashCodeIsReflexive(){
        User user1 = this.getInstanceOfUser();
        Assertions.assertEquals(user1.hashCode(),user1.hashCode());
    }

    @Test
    void ShouldGetName(){
        User user1 = getInstanceOfUser();
        Assertions.assertEquals("Name",user1.getName());
    }

    @Test
    void ShouldGetFname(){
        User user1 = getInstanceOfUser();
        Assertions.assertEquals("FirstName",user1.getFname());
    }

    @Test
    void ShouldGetEmail(){
        User user1 = getInstanceOfUser();
        Assertions.assertEquals("Name.Firstname@emal.com",user1.getEmail());
    }

    @Test
    void HashCodeIsReflexive2(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getAnotherInstanceOfUser();
        Assertions.assertNotEquals(user1.hashCode(),user2.hashCode());
    }

    @Test
    void differentClassShouldNotBeEqual(){
        Group different = Group.getInstance("Name");
        User user = this.getAnotherInstanceOfUser();
        Assertions.assertNotEquals(user,different);

    }

    @Test
    void EqualIsReflexive2(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getAnotherInstanceOfUser();
        Assertions.assertNotEquals(user1,user2);
    }

    @Test
    void EqualIsSymmetric(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getInstanceOfUser();
        Assertions.assertTrue(user1.equals(user2)&&user2.equals(user1));
    }

    @Test
    void HashCodeIsSymmetric(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getInstanceOfUser();
        Assertions.assertTrue(user1.hashCode()==user2.hashCode()&&user2.hashCode()==user1.hashCode());
    }

    @Test
    void HashCodeIsSymmetric2(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getAnotherInstanceOfUser();
        Assertions.assertFalse(user1.hashCode()==user2.hashCode()&&user2.hashCode()==user1.hashCode());
    }

    @Test
    void EqualIsSymmetric2(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getAnotherInstanceOfUser();
        Assertions.assertFalse(user1.equals(user2)&&user2.equals(user1));
    }

    @Test
  void EqualIsTransitive(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getInstanceOfUser();
        User user3 = this.getInstanceOfUser();
        Assertions.assertTrue(user1.equals(user2)&&user2.equals(user3)&&user1.equals(user3));
    }
    @Test
    void EqualIsTransitive2(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getAnotherInstanceOfUser();
        User user3 = this.getInstanceOfUser();
        Assertions.assertFalse(user1.equals(user2)&&user2.equals(user3)&&user1.equals(user3));
    }

    @Test
    void HashCodeIsTransitive(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getInstanceOfUser();
        User user3 = this.getInstanceOfUser();
        Assertions.assertTrue(user1.hashCode()==user2.hashCode()&&
                user2.hashCode()==user3.hashCode()&&
                user1.hashCode()==user3.hashCode());
    }

    @Test
    void HashCodeIsTransitive2(){
        User user1 = this.getInstanceOfUser();
        User user2 = this.getAnotherInstanceOfUser();
        User user3 = this.getInstanceOfUser();
        Assertions.assertFalse(user1.hashCode()==user2.hashCode()&&
                user2.hashCode()==user3.hashCode()&&
                user1.hashCode()==user3.hashCode());
    }

    @Test
    void NullEquality(){
        User user1 = this.getInstanceOfUser();
        Assertions.assertNotEquals(null,user1);
    }
@Test
    void ShouldBeValidException() {
        try {
            throw new IdException("test");
        } catch (IdException e) {
            Assertions.assertEquals("test", e.getMessage());
        }
}

}