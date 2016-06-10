package org.restler.integration.springdata;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity(name = "posts")
public class Post implements Serializable {
    @Id
    private Long id;

    @Column
    private String message;

    @ManyToMany
    @JoinTable(name = "person_post",
            joinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "person_id", referencedColumnName = "id"))
    private List<Person> authors;

    public Post(Long id, String message, List<Person> authors) {
        this.id = id;
        this.message = message;
        this.authors = authors;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public List<Person> getAuthors() {
        return authors;
    }

    // for JPA
    Post() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
