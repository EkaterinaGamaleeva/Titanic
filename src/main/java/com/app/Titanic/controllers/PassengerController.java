package com.app.Titanic.controllers;

import com.app.Titanic.models.Passenger;
import com.app.Titanic.services.PassengerService;
import com.app.Titanic.util.ParserCsv;
import com.app.Titanic.util.TypeOfSorting;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.stream.Collectors;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/passengers")
public class PassengerController {
    private final PassengerService service;


    @Autowired
    public PassengerController(PassengerService service) {
        this.service = service;
    }

    //стартовая страница
    @GetMapping("")
    private String startingPage(Model model) {
        model.addAttribute("passengers", service.findAll().stream().limit(50));
        model.addAttribute("designations", service.getSortingParameters());
        model.addAttribute("types", service.getTypeOfSortingsList());
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "start";
    }

    //выдача выживших пасаажиров
    @GetMapping("/survivors")
    public String survivors(Model model) {
        model.addAttribute("passengers", service.getPassengersSurvivors());
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "survivors";
    }

    //выдача пассажиров у которых нет родственников на борту
    @GetMapping("/NoParentsToBoard")
    public String survivorsNo(Model model) {
        model.addAttribute("passengers", service.getNoPassengerHadRelativesOnBoardList());
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "NoParentsToBoard";
    }

    //фильтр по возрасту
    @GetMapping("/age")
    public String ageFilter(Model model) {
        model.addAttribute("passengers", service.getAnAdultPassenger());
//        model.addAttribute("fareSum", service.getFareSum());
//        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
//        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "age";
    }

    //Фильтр по полу
    @GetMapping("/sex")
    public String sexFilter(Model model) {
        model.addAttribute("passengers", service.getPassengersSexMale());
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "sex";
    }

    //Поиск пассажира  по имении(только полное совпадение)
    @GetMapping("/search")
    public String search(Model model, @RequestParam("name") String name) {
        if (service.search(name).isEmpty()) {
            return "errorSearch";
        }
        model.addAttribute("passengers", service.search(name));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "search";
    }

    ///пагинация по двум параметрам(page и size и sort(c указанием по возрастанию или по убыванию))
    @GetMapping("/pagination")
    public String pagination(Model model,
                             @RequestParam(value = "page", required = false) String page,
                             @RequestParam(value = "size", required = false) String size,
                             @RequestParam(value = "type", required = false) TypeOfSorting typeOfsorting,
                             @RequestParam(value = "sort", required = false) String sort) {
        model.addAttribute("passengers", service.pagination(Integer.valueOf(page), Integer.valueOf(size), sort, typeOfsorting));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "pagination";
    }

    //пагинация по двум параметрам(page и size)
    @GetMapping("/page")
    public String page(Model model,
                       @RequestParam(value = "page", required = false) String page,
                       @RequestParam(value = "size", required = false) String size) {
        model.addAttribute("passengers", service.pageAndSize(Integer.valueOf(page), Integer.valueOf(size)));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "pagination";
    }

    //сортировка(сортирует пассажиров по переданному полю по возростанию и убыванию)
    @GetMapping("/sort")
    public String sort(Model model,
                       @RequestParam(value = "sort", required = false) String sort,
                       @RequestParam(value = "type", required = false) TypeOfSorting typeOfsorting) {
        model.addAttribute("passengers", service.sort(sort, typeOfsorting));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "pagination";
    }

    //сортировка с лимитом выдачи(сортирует пассажиров по переданному полю по возростанию и убыванию и выдает нужное количество пассажиров)
    @GetMapping("/sortAndLimit")
    public String sort(Model model,
                       @RequestParam(value = "sort", required = false) String sort,
                       @RequestParam(value = "type", required = false) TypeOfSorting typeOfsorting,
                       @RequestParam("limit") String limit) {
        Integer l = Integer.valueOf(limit);
        if (l <= 0) {
            return "errorLimit";
        }
        if (l > 888) {
            return "errorLimit";
        }
        model.addAttribute("passengers", service.sortAndLimit(sort, typeOfsorting, l));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "pagination";
    }

    //создание собственного фильтра
    @GetMapping("/filter1")
    private String newFilter(Model model, @RequestParam("type") String type) {
        //фильтр по одному параметру
        model.addAttribute("passengers", service.filter(type));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "newFilter";
    }

    @GetMapping("/filter2")
    private String newFilter(Model model, @RequestParam("type1") String type1,
                             @RequestParam("type2") String type2) {
        //фильтр двум параметрам
        model.addAttribute("passengers", service.filter(type1, type2));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "newFilter";
    }

    @GetMapping("/filter3")
    private String newFilter(Model model, @RequestParam("type1") String type1,
                             @RequestParam("type2") String type2,
                             @RequestParam("type3") String type3) {
        //фильтр 3 параметрам
        model.addAttribute("passengers", service.filter(type1, type2, type3));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "newFilter";
    }

    @GetMapping("/filter4")
    private String newFilter(Model model, @RequestParam("type1") String type1,
                             @RequestParam("type2") String type2,
                             @RequestParam("type3") String type3,
                             @RequestParam("type4") String type4) {
        //фильтр 4
        model.addAttribute("passengers", service.filter(type1, type2, type3, type4));
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());
        return "newFilter";
    }


    //форма для создания фильтра
    @GetMapping("/filterForms")
    private String filterForms(Model model) {
        model.addAttribute("designations", service.getParametersFilter());
        model.addAttribute("fareSum", service.getFareSum());
        model.addAttribute("counter", service.passengerHadRelativesOnBoard());
        model.addAttribute("survivors", service.getCounterOfSurvivors());

        return "filterForms";
    }

    //лимит выдачи пассажиров(млжно указать сколько пассажиров будет на странице)
    @GetMapping("/limit")
    private String getPassengerAll(Model model, @RequestParam("limit") String limit) {
        Integer l = Integer.valueOf(limit);
        if (l <= 0) {
            return "errorLimit";
        }
        if (l > 888) {
            return "errorLimit";
        } else {
            model.addAttribute("passengers", service.limit(l));
            model.addAttribute("fareSum", service.getFareSum());
            model.addAttribute("counter", service.passengerHadRelativesOnBoard());
            model.addAttribute("survivors", service.getCounterOfSurvivors());
        }
        return "limit";
    }

    //создание пассажира
    @ResponseBody
    @PostMapping()
    private ResponseEntity create(@RequestBody @Valid Passenger passenger) {
        service.save(passenger);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //удаление пассажира
    @ResponseBody
    @DeleteMapping("/{id}")
    private ResponseEntity delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.LOOP_DETECTED);
    }

    //Возвращает пассажира по id
    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity getPassengerById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(service.findById(id), HttpStatus.OK);
    }

    //    метод для тестирования работы кэша
    @ResponseBody
    @GetMapping("/findAll")
    public ResponseEntity getFindAll() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    //изменение пассажира
    @PatchMapping("/{id}")
    @ResponseBody
    public ResponseEntity update(@RequestBody @Valid Passenger passenger,
                                 @PathVariable("id") Long id) {
        service.update(id, passenger);
        return new ResponseEntity<>(HttpStatus.OK);
    }


//    //парсинг файла csv метод использовался для тестирования парсинга (на данном этапе он не нужен)
//    @ResponseBody
//    @GetMapping("/csv")
//    public ResponseEntity parserCSVFile() throws IOException, CsvValidationException {
//        System.out.println("я не пользовательский метод");
//        if (counter == 0) {
//            ParserCsv parserCsv = new ParserCsv(service);
//            parserCsv.getPassengersList().stream().forEach(e -> service.save(e));
//            counter++;
//        } else if (counter > 0) {
//            System.out.println("Метод уже был вызван, сервер остановлен что бы предотвратить дублирование данных перейдите в util ParserCsv измените ссылку и сбросте счетчик counter ");
//            try {
//                throw new Exception();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
//    }
//


}