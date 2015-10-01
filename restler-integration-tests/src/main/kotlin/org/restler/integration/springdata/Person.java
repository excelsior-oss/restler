package org.restler.integration.springdata;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "persons")
public class Person implements Serializable {

    @Id private String id;

    @Column private String name;

    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // for JPA
    Person() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
