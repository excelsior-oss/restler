package org.restler.integration.springdata;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity(name = "persons")
public class Person implements Serializable {

    @Id private Long id;

    @Column private String name;

    @OneToMany(mappedBy = "person"/*, cascade = CascadeType.ALL*/)
    private List<Pet> pets = new ArrayList<>();

    @OneToMany(mappedBy = "person"/*, cascade = CascadeType.ALL*/)
    private List<Address> addresses;

    public Person(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Pet> getPets() {return pets;}

    public List<Address> getAddresses() {return addresses;}

    // for JPA
    Person() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
