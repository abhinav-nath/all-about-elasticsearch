package com.codecafe.search.controller;

import com.codecafe.search.model.SearchResponse;
import com.codecafe.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchResponse> textSearch(@RequestParam("query") final String query,
                                                     @RequestParam(value = "from", defaultValue = "0") final int from,
                                                     @RequestParam(value = "size", defaultValue = "5") final int size,
                                                     @RequestParam(value = "sortBy", required = false, defaultValue = "RELEVANCE") final String sortBy,
                                                     @RequestParam(value = "facets", required = false) final String facets) {

        SearchResponse searchResponse = searchService.performTextSearch(query, from, size, sortBy, facets);

        return ResponseEntity.ok(searchResponse);
    }

}