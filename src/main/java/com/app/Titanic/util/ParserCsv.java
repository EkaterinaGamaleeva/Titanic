package com.app.Titanic.util;

import com.app.Titanic.models.GradeEnum;
import com.app.Titanic.models.Passenger;
import com.app.Titanic.services.PassengerService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
public class ParserCsv {
    private final PassengerService service;
    private final String fileName = "C:/Users/gamal/IdeaProjects/Titanic/src/main/resources/titanic.csv";

    @Autowired
    public ParserCsv(PassengerService service) throws IOException {
        this.service = service;
    }

  @PostConstruct
  private void mappingCsvToPassenger() throws IOException {
            BufferedReader lineReader = null;
            try {
                lineReader = new BufferedReader(new FileReader(fileName));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                lineReader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String lineText = null;
            while ((lineText = lineReader.readLine()) != null) {
                Passenger passenger = new Passenger();
                String[] data = lineText.split(",");
                //преобразование в boolean
                Integer survived = Integer.valueOf(data[0]);
                if (survived == 0) {
                    passenger.setSurvived(false);
                } else if (survived == 1) {
                    passenger.setSurvived(true);
                }
                Integer grade = Integer.valueOf(data[1]);
                // преобразование(1,2,3)=> в Enum(FIRST_GRADE,SECOND_GRADE,THIRD_GRADE)
                if (grade == 1) {
                    passenger.setGrade(GradeEnum.FIRST_GRADE);
                }
                if (grade == 2) {
                    passenger.setGrade(GradeEnum.SECOND_GRADE);
                }
                if (grade == 3) {
                    passenger.setGrade(GradeEnum.THIRD_GRADE);
                }
                //
                String name = data[2];
                passenger.setName(name);

                String sex = data[3];
                passenger.setSex(sex);
                //убираем залетевший double
                Double d = Double.valueOf((data[4]));
                //
                Integer age = d.intValue();
                passenger.setAge(age);
//
                Integer countSiblingsOrSpousesOnBoard = Integer.valueOf(data[5]);
                passenger.setCountSiblingsOrSpousesOnBoard(countSiblingsOrSpousesOnBoard);

                Integer countParentsOrChildrenOnBoard = Integer.valueOf(data[6]);
                passenger.setCountParentsOrChildrenOnBoard(countParentsOrChildrenOnBoard);

                BigDecimal decimal = new BigDecimal(data[7]);
                passenger.setFare(decimal.doubleValue());
                service.save(passenger);
            }

            lineReader.close();
            System.out.println("Данные добавлены в базу данных ,будьте остороны при перезапуске даныые будут дублированнны , после инициализации закоментируйте аннотацию @PostConstruct");

    }
}
