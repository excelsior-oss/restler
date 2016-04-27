package org.restler.integration.springdata;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "posts")
public class Post implements Serializable {
    @Id
    private Long id;

    @Column
    private String message;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Person author;

    public Post(Long id, String message, Person author) {
        this.id = id;
        this.message = message;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Person getAuthor() {
        return author;
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
