package com.example.test.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "place")
@Data
@Builder
@AllArgsConstructor // @Builder 를 이용하기 위해서 항상 같이 처리해야 컴파일 에러가 발생하지 않는다
@NoArgsConstructor // @Builder 를 이용하기 위해서 항상 같이 처리해야 컴파일 에러가 발생하지 않는다
public class Place {
    @Id // Primary Key를 뜻함
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idPlace;

    @Column
    private Double lat;
    @Column
    private Double lon;
    @Column
    private String placeName;
    @Column
    private String adminArea;
    @Column
    private String locality;
    @Column
    private String subLocality;
    @Column
    private String thoroughfare;
    @Column
    private int age;
    @Column
    private String hashTags;
    @Column
    private String image;



    public Place(Place place, int count) {
        this.idPlace = place.getIdPlace();
        this.lat = place.getLat();
        this.lon = place.getLon();
        this.placeName = place.getPlaceName();
        this.adminArea = place.getAdminArea();
        this.locality = place.getLocality();
        this.subLocality = place.getSubLocality();
        this.thoroughfare = place.getThoroughfare();
        this.age = place.getAge();
        this.hashTags = place.getHashTags();
        this.image = place.getImage();
    }
}