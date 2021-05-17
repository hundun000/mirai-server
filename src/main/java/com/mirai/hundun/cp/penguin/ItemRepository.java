package com.mirai.hundun.cp.penguin;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mirai.hundun.cp.penguin.domain.Item;

/**
 * @author hundun
 * Created on 2021/04/26
 */
@Repository
public interface ItemRepository extends MongoRepository<Item, String> {
    Item findTopByName(String name);
    //List<Item> findAllByNameLike(String name);
}
