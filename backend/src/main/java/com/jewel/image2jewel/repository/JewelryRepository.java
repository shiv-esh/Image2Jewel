package com.jewel.image2jewel.repository;

import com.jewel.image2jewel.model.JewelryItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JewelryRepository extends ElasticsearchRepository<JewelryItem, String> {
}
