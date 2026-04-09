package org.otis.model.entity;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
}
