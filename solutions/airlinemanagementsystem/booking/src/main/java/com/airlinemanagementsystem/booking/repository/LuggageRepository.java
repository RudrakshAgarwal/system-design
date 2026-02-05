package com.airlinemanagementsystem.booking.repository;

import com.airlinemanagementsystem.booking.entity.Luggage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LuggageRepository extends JpaRepository<Luggage, Long> {
    Optional<Luggage> findByTagNumber(String tagNumber);
}