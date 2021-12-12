package com.codecafe.search.repository;

import com.codecafe.search.document.ProductDocument;
import com.codecafe.search.model.SearchResult;
import com.codecafe.search.config.OpenSearchConfig.OpenSearchProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.search.sort.SortBuilders.fieldSort;
import static org.elasticsearch.search.sort.SortBuilders.scoreSort;
import static org.elasticsearch.search.sort.SortOrder.DESC;
import static org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.of;
import static org.springframework.util.CollectionUtils.isEmpty;

@Repository
public class SearchRepository {

    @Value("${app.search.index-name}")
    private String indexName;

    private final OpenSearchProperties openSearchProperties;

    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    public SearchRepository(ElasticsearchRestTemplate elasticsearchTemplate, OpenSearchProperties openSearchProperties) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.openSearchProperties = openSearchProperties;
    }

    public SearchResult searchProducts(String query, int from, int size, String sortBy, String facets) {

        List<ProductDocument> matchedProducts = new ArrayList<>(1);
        SearchResult searchResult = new SearchResult();

        Map<String, Float> queryFields = new HashMap<>();
        queryFields.put("name", 3.0f);
        queryFields.put("description", 1.0f);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withSort(scoreSort())
                .withSort(fieldSort("dateModified").order(DESC))
                .withSort(fieldSort("dateAdded").order(DESC))
                .withQuery(multiMatchQuery(query)
                        .fields(queryFields))
                .withPageable(PageRequest.of(from, size))
                .build();

        SearchHits<ProductDocument> searchHits = elasticsearchTemplate.search(searchQuery, ProductDocument.class, of(indexName));

        if (!isEmpty(searchHits.getSearchHits())) {
            searchHits.getSearchHits()
                    .forEach(hit -> matchedProducts.add(hit.getContent()));

            searchResult = SearchResult.builder()
                    .productDocuments(matchedProducts)
                    .totalResults(searchHits.getTotalHits())
                    .build();
        }

        return searchResult;
    }

}