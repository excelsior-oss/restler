package org.restler.integration.springdata;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "persons")
public class Person implements Serializable {

    @Id private Long id;

    @Column private String name;

    @OneToMany(mappedBy = "person"/*, cascade = CascadeType.ALL*/)
    private List<Pet> pets = new ArrayList<>();

    @OneToMany(mappedBy = "person"/*, cascade = CascadeType.ALL*/)
    private List<Address> addresses;

    @ManyToMany
    @JoinTable(name = "person_post",
            joinColumns = @JoinColumn(name = "person_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id"))
    private List<Post> posts;

    public Person(Long id, String name, List<Post> posts) {
        this.id = id;
        this.name = name;
        this.posts = posts;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Pet> getPets() {return pets;}

    public List<Address> getAddresses() {return addresses;}

    public List<Post> getPosts() {
        return posts;
    }

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
