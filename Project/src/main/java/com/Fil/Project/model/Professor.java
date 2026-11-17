package com.Fil.Project.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("PROFESSOR")
public class Professor extends User {
    @ElementCollection
    @CollectionTable(name = "professor_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest", nullable = false)
    private List<String> interests;

    /* getters / setters */
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
}