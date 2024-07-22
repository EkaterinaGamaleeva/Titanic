package com.app.Titanic.repositories;

import com.app.Titanic.models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger,Long> {
    Optional<List<Passenger>> findByName(String name);
}
