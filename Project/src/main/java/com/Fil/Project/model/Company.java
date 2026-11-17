package com.Fil.Project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@DiscriminatorValue("COMPANY")
public class Company extends User {
    @NotBlank
    private String companyName;
    private String location;
    private String description;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String n) { companyName = n; }
    public String getLocation() { return location; }
    public void setLocation(String l) { location = l; }
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
}