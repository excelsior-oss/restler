package org.restler.integration.springdata;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "pets")
public class Pet implements Serializable {
    @Id
    private Long id;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Person person;

    public Pet(Long id, String name, Person person) {
        this.id = id;
        this.name = name;
        this.person = person;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Person getPerson() {
        return person;
    }

    // for JPA
    Pet() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
