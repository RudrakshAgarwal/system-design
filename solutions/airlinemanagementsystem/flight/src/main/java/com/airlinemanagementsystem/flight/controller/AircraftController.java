package com.airlinemanagementsystem.flight.controller;

import com.airlinemanagementsystem.flight.entity.Aircraft;
import com.airlinemanagementsystem.flight.repository.AircraftRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aircrafts")
@RequiredArgsConstructor
@Tag(name = "Aircraft Controller", description = "Manage physical aircraft inventory")
public class AircraftController {

    private final AircraftRepository aircraftRepository;

    @Operation(summary = "Add a new aircraft", description = "Registers a new aircraft with a unique tail number and total seat capacity")
    @PostMapping
    public ResponseEntity<Aircraft> addAircraft(@Valid @RequestBody Aircraft aircraft) {
        return new ResponseEntity<>(aircraftRepository.save(aircraft), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all aircrafts", description = "Returns a list of all registered aircraft in the fleet")
    @GetMapping
    public ResponseEntity<List<Aircraft>> getAllAircrafts() {
        return ResponseEntity.ok(aircraftRepository.findAll());
    }

    @Operation(summary = "Get aircraft by tail number")
    @GetMapping("/{tailNumber}")
    public ResponseEntity<Aircraft> getByTailNumber(@PathVariable String tailNumber) {
        return aircraftRepository.findByTailNumber(tailNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
