package br.com.eduardo.quarkussocial.domain.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "followers")
@Data
public class Follower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    @ManyToOne
    private User follower;

}
