package com.app.Titanic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "passengers")
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "survived")
    private boolean survived;

    @Enumerated(EnumType.STRING)
    private GradeEnum grade;

    @NotEmpty(message = "Name should not be empty")
//    @Size(min = 2, max = 100, message = "Name should be between 2 and 100 characters")
    @Column(name = "name")
    private String name;

    @NotEmpty(message = " Sex should not be empty")
    @Column(name = "sex")
    private String sex;

//    @Min(value = 0, message = "Значение не может быть меньше 0")
    @NotNull(message = " Age should not be empty")
    @Column(name = "age")
    private int age;

//    @Min(value = 0, message = "Значение не может быть меньше 0")
    @Column(name = "countSiblingsOrSpousesOnBoard")
    private int countSiblingsOrSpousesOnBoard;

//    @Min(value = 0, message = "Значение не может быть меньше 0")
    @Column(name = "countParentsOrChildrenOnBoard")
    private int countParentsOrChildrenOnBoard;

    @NotNull
//    @Min(value = 0, message = "Значение не может быть меньше 0")
    @Column(name = "fare")
    private double fare;

}
