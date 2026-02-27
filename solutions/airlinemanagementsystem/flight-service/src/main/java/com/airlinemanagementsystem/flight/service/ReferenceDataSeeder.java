package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.dto.AircraftSourceDto;
import com.airlinemanagementsystem.flight.dto.AirportSourceDto;
import com.airlinemanagementsystem.flight.entity.Aircraft;
import com.airlinemanagementsystem.flight.entity.Airport;
import com.airlinemanagementsystem.flight.repository.AircraftRepository;
import com.airlinemanagementsystem.flight.repository.AirportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReferenceDataSeeder {

    private final AirportRepository airportRepository;
    private final AircraftRepository aircraftRepository;
    private final ObjectMapper objectMapper;

    @Value("${flight-service.data.airports-path:data/airports.json}")
    private String airportsFilePath;

    @Value("${flight-service.data.aircrafts-path:data/aircrafts.dat}")
    private String aircraftsFilePath;

    private static final Pattern IATA_PATTERN = Pattern.compile("^[A-Z]{3}$");

    @PostConstruct
    @Transactional
    public void seedReferenceData() {
        seedAircrafts();
        seedAirports();
    }

    private void seedAircrafts() {
        if (aircraftRepository.count() > 0) {
            log.info("Aircraft data already exists. Skipping.");
            return;
        }

        try {
            log.info("Loading Aircrafts from: {}", aircraftsFilePath);
            InputStream inputStream = new ClassPathResource(aircraftsFilePath).getInputStream();

            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.builder()
                    .addColumn("name")
                    .addColumn("iata")
                    .addColumn("icao")
                    .setColumnSeparator(',')
                    .setQuoteChar('"')
                    .build();

            MappingIterator<AircraftSourceDto> it = csvMapper
                    .readerFor(AircraftSourceDto.class)
                    .with(schema)
                    .readValues(inputStream);

            List<Aircraft> aircraftsToSave = new ArrayList<>();
            Set<String> processedTailNumbers = new HashSet<>();

            while (it.hasNext()) {
                AircraftSourceDto dto = it.next();

                if (isValidAircraft(dto)) {
                    // Generate unique tail number
                    String generatedTailNumber = dto.getIata() + "-" + dto.getIcao();

                    if (!processedTailNumbers.contains(generatedTailNumber)) {
                        aircraftsToSave.add(Aircraft.builder()
                                .model(dto.getName())
                                .tailNumber(generatedTailNumber)
                                .totalCapacity(estimateCapacity(dto.getName()))
                                .build());
                        processedTailNumbers.add(generatedTailNumber);
                    }
                }
            }

            if (!aircraftsToSave.isEmpty()) {
                aircraftRepository.saveAll(aircraftsToSave);
                log.info("Seeded {} aircraft types.", aircraftsToSave.size());
            }

        } catch (Exception e) {
            log.error("Failed to seed aircrafts: {}", e.getMessage());
        }
    }

    private void seedAirports() {
        if (airportRepository.count() > 0) {
            log.info("Airport data already exists. Skipping.");
            return;
        }

        try {
            log.info("Loading Airports from: {}", airportsFilePath);
            InputStream inputStream = new ClassPathResource(airportsFilePath).getInputStream();

            Map<String, AirportSourceDto> sourceMap = objectMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, AirportSourceDto>>() {}
            );

            List<Airport> airportsToSave = new ArrayList<>();
            int skippedCount = 0;

            for (AirportSourceDto dto : sourceMap.values()) {
                if (dto.getIataCode() != null) {
                    dto.setIataCode(dto.getIataCode().toUpperCase());
                }

                if (isValidAirport(dto)) {
                    airportsToSave.add(Airport.builder()
                            .code(dto.getIataCode())
                            .name(dto.getName())
                            .city(dto.getCity())
                            .country(dto.getCountry())
                            .timezoneId(dto.getTimezone())
                            .build());
                } else {
                    skippedCount++;
                }
            }

            if (!airportsToSave.isEmpty()) {
                airportRepository.saveAll(airportsToSave);
                log.info("Seeded {} valid airports. (Skipped {} invalid/incomplete entries)",
                        airportsToSave.size(), skippedCount);
            } else {
                log.warn("No valid airports found to seed!");
            }

        } catch (Exception e) {
            log.error("Failed to seed airports: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidAircraft(AircraftSourceDto dto) {
        return dto.getIata() != null
                && !dto.getIata().isEmpty()
                && !dto.getIata().equals("\\N");
    }

    private boolean isValidAirport(AirportSourceDto dto) {
        if (dto.getIataCode() == null || !IATA_PATTERN.matcher(dto.getIataCode()).matches()) {
            return false;
        }

        return hasText(dto.getName())
                && hasText(dto.getCity())
                && hasText(dto.getCountry());
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private int estimateCapacity(String modelName) {
        if (modelName == null) return 100;
        String name = modelName.toLowerCase();
        if (name.contains("747") || name.contains("a380")) return 450;
        if (name.contains("777") || name.contains("a350") || name.contains("787")) return 300;
        if (name.contains("767") || name.contains("a330")) return 250;
        if (name.contains("737") || name.contains("a320") || name.contains("321")) return 180;
        if (name.contains("erj") || name.contains("crj") || name.contains("atr")) return 70;
        return 150;
    }
}