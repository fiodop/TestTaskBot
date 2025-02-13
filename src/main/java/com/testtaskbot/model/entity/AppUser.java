package com.testtaskbot.model.entity;

import com.testtaskbot.model.entity.Enums.Sex;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUser {
    @Id
    private int id;
    private String name;
    private String surname;
    private Date birthday;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    private byte[] photo;
}
