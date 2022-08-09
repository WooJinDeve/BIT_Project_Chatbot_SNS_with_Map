package com.example.test.repository;

import java.util.List;
import com.example.test.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlaceRepository extends JpaRepository<Place, Integer> {

    //일반 sql 쿼리
    @Query(value = "select * from place", nativeQuery = true)
    public List<Place> selectAll();

}