package fr.univtln.lhd.model.entities.slots;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class defining a Classroom
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Classroom {

    private long id;

    private final String name;
    //private final String buildingName; //? might not be a string in database ?

    /**
     * Factory for a classroom given an id
     * @param id long identifier for a Classroom (default -1)
     * @param name name of the classroom
     * //@param buildingName name of the building for that classroom
     * @return an instance of Classroom class
     */
    public static Classroom getInstance(long id, String name) { return new Classroom(id, name); }

    /**
     * Factory for a classroom
     * @param name name of the classroom
     * //@param buildingName name of the building for that classroom
     * @return an instance of Classroom class
     */
    public static Classroom getInstance(String name) {
        return getInstance(-1, name);
    }
}
