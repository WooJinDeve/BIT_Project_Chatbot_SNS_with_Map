package com.example.test;

import com.example.test.entity.Place;
import com.example.test.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

@SpringBootTest
class PlaceRepositoryTest {
    @Autowired
    PlaceRepository placeRepository;

    @Test
    public void InsertDummies() {
            System.out.println(placeRepository.selectAll());
    }
}

