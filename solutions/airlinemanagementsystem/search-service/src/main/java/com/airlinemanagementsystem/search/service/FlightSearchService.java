package com.airlinemanagementsystem.search.service;

import com.airlinemanagementsystem.search.document.FlightDocument;
import com.airlinemanagementsystem.search.dto.FlightSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<FlightDocument> searchFlights(FlightSearchRequest request) {
        log.info("Executing Elasticsearch query for Source: {}, Dest: {}", request.getSource(), request.getDestination());

        Criteria criteria = new Criteria("status").not().is("CANCELLED");

        if (request.getSource() != null && !request.getSource().isEmpty()) {
            criteria = criteria.and(new Criteria("sourceAirport").is(request.getSource()));
        }

        if (request.getDestination() != null && !request.getDestination().isEmpty()) {
            criteria = criteria.and(new Criteria("destinationAirport").is(request.getDestination()));
        }

        if (request.getDate() != null) {
            long startOfDay = request.getDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            long endOfDay = request.getDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

            criteria = criteria.and(new Criteria("departureTime").between(startOfDay, endOfDay));
        }

        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<FlightDocument> searchHits = elasticsearchOperations.search(query, FlightDocument.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}