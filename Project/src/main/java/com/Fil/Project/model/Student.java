package com.Fil.Project.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {
    @Column(length = 30, nullable = false)
    private String universityId;

    @ElementCollection
    @CollectionTable(name = "student_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest", nullable = false)
    private List<String> interests;

    @ElementCollection
    @CollectionTable(name = "student_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill", nullable = false)
    private List<String> skills;

    private String preferredLocation;

    /* getters / setters */
    public String getUniversityId() { return universityId; }
    public void setUniversityId(String universityId) { this.universityId = universityId; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }
}