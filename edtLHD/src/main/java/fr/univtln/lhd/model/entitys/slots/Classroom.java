package fr.univtln.lhd.model.entitys.slots;

import lombok.Getter;

/**
 * Class defining a Classroom
 */
@Getter
public class Classroom {

    private final String name;
    private final String buildingName; //? might not be a string in database ?

    public Classroom(String name, String buildingName) {
        this.name = name;
        this.buildingName = buildingName;
    }

    /**
     * Factory for a classroom
     * @param name name of the classroom
     * @param buildingName name of the building for that classroom
     * @return an instance of Classroom class
     */
    public static Classroom getInstance(String name, String buildingName) {
        return new Classroom(name, buildingName);
    }
}