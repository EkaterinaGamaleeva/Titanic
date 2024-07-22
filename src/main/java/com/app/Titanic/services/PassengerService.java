package com.app.Titanic.services;

import com.app.Titanic.models.Passenger;
import com.app.Titanic.repositories.PassengerRepository;
import com.app.Titanic.response.PassengerNotFoundException;
import com.app.Titanic.util.TypeOfSorting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private List<Passenger> noPassengerHadRelativesOnBoardList;
    private List<Passenger> passengersSurvivors;

    @Autowired
    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }
    //выдача с лимитом
    @Cacheable(value = "passengers", key = "#limit")
    public List<Passenger> limit(Integer limit){
        return findAll().stream().limit(limit).collect(Collectors.toList());
    }
    //сортировка с лимитом
    @Cacheable(value = "passengers", key = "{#sortBy,#limit,#limit}")
    public List<Passenger> sortAndLimit(String sortBy ,TypeOfSorting type,Integer limit){
        if (TypeOfSorting.ASCENDING == type) {
            return passengerRepository.findAll(Sort.by(sortBy)).stream().limit(limit).collect(Collectors.toList());
        } else {
            return passengerRepository.findAll(Sort.by(sortBy).descending()).stream().limit(limit).collect(Collectors.toList());
        }
    }

    //сортировка по возрастанию и убыванию
    @Cacheable(value = "passengers", key = "{#sortBy,#type}")
    public List<Passenger> sort(String sortBy, TypeOfSorting type) {
        if (TypeOfSorting.ASCENDING == type) {
            return passengerRepository.findAll(Sort.by(sortBy));
        } else {
            return passengerRepository.findAll(Sort.by(sortBy).descending());
        }

    }

    //пагинация по трем параметрам Page and Size Sort(по возрастанию и убыванию)
    @Cacheable(value = "passengers", key = "{#page,#size,#sortBy,#type}")
    public List<Passenger> pagination(Integer page, Integer size, String sortBy, TypeOfSorting type) {
        if (TypeOfSorting.ASCENDING == type) {
            return passengerRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy))).getContent();
        } else {
            return passengerRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy).descending())).getContent();
        }
    }

    //пагинация по двум параметрам Page and Size
    @Cacheable(value = "passengers", key = "{#page,#size}")
    public Page<Passenger> pageAndSize(Integer page, Integer size) {
        return passengerRepository.findAll(PageRequest.of(page, size));
    }

    //поиск пассажира по имени
    @Cacheable(value = "passengers", key = "#name")
    public List<Passenger> search(String name) {
        return passengerRepository.findByName(name).orElseThrow(PassengerNotFoundException::new);
    }
    @Cacheable("fareSum")
    public Double getFareSum() {
        return passengerRepository.findAll().stream().map(e -> e.getFare()).reduce((a, e) -> a + e).get().doubleValue();
    }

    //сортировка всех пассажиров у кого есть родственники на борту(Количество людей имеющих родственников на борту)4
    @Cacheable("sizePassengerHadRelativesOnBoard()")
    public long passengerHadRelativesOnBoard() {
        List<Passenger> passengerHadRelativesOnBoardList = new ArrayList<>();
        noPassengerHadRelativesOnBoardList = new ArrayList<>();
        passengerRepository.findAll().stream().map(e -> {
                    if (e.getCountParentsOrChildrenOnBoard() != 0 || e.getCountSiblingsOrSpousesOnBoard() != 0) {
                        return passengerHadRelativesOnBoardList.add(e);
                    }
                    return noPassengerHadRelativesOnBoardList.add(e);
                }
        ).collect(Collectors.toList());
        return passengerHadRelativesOnBoardList.size();
    }

    //подсчет количества выживших пассажиров
    @Cacheable("counterOfSurvivors")
    public long getCounterOfSurvivors() {
       passengersSurvivors  = new ArrayList<>();
        passengerRepository.findAll().stream().map(e -> {
                    if (e.isSurvived() == true) {
                        return passengersSurvivors.add(e);
                    }
                    return null;
                }
        ).collect(Collectors.toList());
        return passengersSurvivors.size();
    }

    //возвращение всех выживших
    @Cacheable("survivors")
    public List<Passenger> getPassengersSurvivors() {

        return passengersSurvivors;
    }
    //возвращает совершенолетних пассажиров
    @Cacheable(value = "anAdult")
    public List<Passenger> getAnAdultPassenger() {
        return passengerRepository.findAll().stream().filter(e -> e.getAge() > 16).collect(Collectors.toList());
    }
    //возвращает  пассажиров мужского пола
    @Cacheable(value = "male")
    public List<Passenger> getPassengersSexMale() {
        return passengerRepository.findAll().stream().filter(e -> e.getSex().equals("male")).collect(Collectors.toList());
    }

    //возвращение всех пассажиров у кого нет родственников на борту
    @Cacheable(value = "noPassengerHadRelativesOnBoard")
    public List<Passenger> getNoPassengerHadRelativesOnBoardList() {

        return noPassengerHadRelativesOnBoardList;
    }




    //фильтр с одним параметром
    public List<Passenger> filter(String type) {
        return filter(
                passengerRepository.findAll(), type);
    }
    //фильтр с двумя параметрами
    public List<Passenger> filter(String type1, String type2) {
        List<Passenger> passengers = new ArrayList<>();
        List<Passenger> passengerSort = new ArrayList<>();
        passengers = filter(type1);
        passengerSort = filter(passengers, type2);
        return passengerSort;
    }
    //Фильтр по 3 параметрам
    public List<Passenger> filter(String type1, String type2, String type3) {
        List<Passenger> passengers = new ArrayList<>();

        passengers = filter(type1);
        passengers = filter(passengers, type2);
        passengers = filter(passengers, type3);
        return passengers;
    }
    //фильтр по 4 параметрам
    public List<Passenger> filter(String type1, String type2, String type3, String type4) {
        List<Passenger> passengers = new ArrayList<>();
        passengers = filter(type1);
        passengers = filter(passengers, type2);
        passengers = filter(passengers, type3);
        passengers = filter(passengers, type4);

        return passengers;
    }


    //фильтр который позволяет отсортировать лист пассажиров по определенному типу
    public List<Passenger> filter(List<Passenger> passengers, String type) {
        List<Passenger> passengerSort = new ArrayList<>();
        if (type.equals(parametersFilter.get(0))) {
            passengerSort = passengers.stream().filter(e -> e.isSurvived() == true).collect(Collectors.toList());
        }
        //сортировка Совершеннолетих пассажирова (страше 16 лет)
        if (type.equals(parametersFilter.get(1))) {
            passengerSort = passengers.stream().filter(e -> e.getAge() > 16).collect(Collectors.toList());
        }
        //сортировка Пассажиры мкжского пола
        if (type.equals(parametersFilter.get(2))) {
            passengerSort = passengers.stream().filter(e -> e.getSex().equals("male")).collect(Collectors.toList());
        }
        //сортировка Пассажиры кто не имеет родственников на борту
        if (type.equals(parametersFilter.get(3))) {
            List<Passenger> finalPassengerSort = new ArrayList<>();
            passengers.stream().map(e -> {
                        if (e.getCountParentsOrChildrenOnBoard() != 0 || e.getCountSiblingsOrSpousesOnBoard() != 0) {
                            return null;
                        }
                        return finalPassengerSort.add(e);
                    }
            ).collect(Collectors.toList());
            passengerSort = finalPassengerSort;
        }
        return passengerSort;
    }



    //возвращение всех пассажиров
    @Cacheable("passengers")
    public List<Passenger> findAll() {

        return passengerRepository.findAll();
    }


    //возвращает пассажира по id
    @CachePut(value = "passengers", key = "#id")
    public Passenger findById(Long id) {
        Optional<Passenger> foundPerson = passengerRepository.findById(id);
        return foundPerson.orElseThrow(PassengerNotFoundException::new);
    }

    //изменение пассажира
    @Transactional
    @CachePut(value = "passengers", key = "#id")
    public void update(Long id, Passenger updatedPassenger) {
        updatedPassenger.setId(id);
        passengerRepository.save(updatedPassenger);
    }


    //сохраниние пассажира
    @Transactional
    @CachePut(value = "passengers", key = "#passenger")
    public void save(Passenger passenger) {
        passengerRepository.save(passenger);
    }

    //удаление пассажира
    @Transactional
    @CacheEvict(value = "passengers", key = "#id")
    public void delete(Long id) {
        passengerRepository.deleteById(id);
    }

    private static List<String> parametersFilter;


    static {
        parametersFilter = new ArrayList<>();
        parametersFilter.add("Выжившие пассажиры");
        parametersFilter.add("Совершеннолетих пассажирова (страше 16 лет)");
        parametersFilter.add("Пассажиры мужского пола");
        parametersFilter.add("Пассажиры кто не имеет родственников на борту");
    }

    public List<String> getParametersFilter() {
        return parametersFilter;
    }

    private static List<String> sortingParameters;

    static {
        sortingParameters = new ArrayList<>();
        sortingParameters.add("id");
        sortingParameters.add("name");
        sortingParameters.add("age");
//        sortingParameters.add("sex");
//        sortingParameters.add("survived");
//        sortingParameters.add("grade");
//        sortingParameters.add("countSiblingsOrSpousesOnBoard");
//        sortingParameters.add("countParentsOrChildrenOnBoard");
        sortingParameters.add("fare");
    }

    public List<String> getSortingParameters() {
        return sortingParameters;
    }

    private static List<TypeOfSorting> typeOfSortingsList;

    static {
        typeOfSortingsList = new ArrayList<>();
        typeOfSortingsList.add(TypeOfSorting.ASCENDING);
        typeOfSortingsList.add(TypeOfSorting.DESCENDING);
    }

    public List<TypeOfSorting> getTypeOfSortingsList() {
        return typeOfSortingsList;
    }
}
