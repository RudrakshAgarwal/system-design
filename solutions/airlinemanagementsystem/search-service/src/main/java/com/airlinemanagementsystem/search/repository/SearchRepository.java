package com.airlinemanagementsystem.search.repository;

import com.airlinemanagementsystem.search.document.FlightDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository extends ElasticsearchRepository<FlightDocument, String> {
}
