package br.com.eduardo.quarkussocial.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "posts")
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_text")
    private String postText;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @ManyToOne()
    private User user;

    @PrePersist
    public void prePersist(){
        this.dateTime = LocalDateTime.now();
    }

}
