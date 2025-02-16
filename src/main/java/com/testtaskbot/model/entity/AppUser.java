package com.testtaskbot.model.entity;

import com.testtaskbot.model.enums.Sex;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUser {
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private int id;
    private String name;
    private String surname;
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private Sex sex;


    private String UTMUrl;
}
