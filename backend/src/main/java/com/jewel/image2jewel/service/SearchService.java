package com.jewel.image2jewel.service;

import com.jewel.image2jewel.model.JewelryItem;
import com.jewel.image2jewel.model.SearchCriteria;
import com.jewel.image2jewel.repository.JewelryRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final JewelryRepository repository;
    private final ElasticsearchClient esClient;

    public SearchService(JewelryRepository repository, ElasticsearchClient esClient) {
        this.repository = repository;
        this.esClient = esClient;
    }

    public void save(JewelryItem item) {
        repository.save(item);
    }

    public List<JewelryItem> findSimilar(float[] vector, int k) {
        List<Float> queryVector = new ArrayList<>();
        for (float f : vector) queryVector.add(f);

        try {
            SearchResponse<JewelryItem> response = esClient.search(s -> s
                    .index("jewelry_items")
                    .knn(knn -> knn
                            .field("imageVector")
                            .queryVector(queryVector)
                            .k(k)
                            .numCandidates(100)
                    ),
                    JewelryItem.class
            );

            return mapResults(response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to search Elasticsearch", e);
        }
    }

    public List<JewelryItem> findSimilarHybrid(float[] vector, SearchCriteria criteria, int k) {
        List<Float> queryVector = new ArrayList<>();
        for (float f : vector) queryVector.add(f);

        try {
            SearchResponse<JewelryItem> response = esClient.search(s -> s
                    .index("jewelry_items")
                    .knn(knn -> knn
                            .field("imageVector")
                            .queryVector(queryVector)
                            .k(k)
                            .numCandidates(100)
                            .filter(f -> f
                                .bool(b -> b
                                    .must(m -> m.term(t -> t.field("category").value(criteria.category().toLowerCase())))
                                    .should(sh -> sh.match(m -> m.field("description").query(criteria.metal())))
                                )
                            )
                    ),
                    JewelryItem.class
            );

            return mapResults(response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to search Elasticsearch", e);
        }
    }

    private List<JewelryItem> mapResults(SearchResponse<JewelryItem> response) {
        List<JewelryItem> results = new ArrayList<>();
        for (Hit<JewelryItem> hit : response.hits().hits()) {
            if (hit.source() != null) {
                results.add(hit.source());
            }
        }
        return results;
    }
}
