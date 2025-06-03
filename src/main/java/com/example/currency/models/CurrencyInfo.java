package com.example.currency.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "currency_info")
@Data
public class CurrencyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer curId;

    @Column(name = "cur_code")
    private String curCode;

    @Column(name = "cur_abbreviation", unique = true)
    private String curAbbreviation;

    @Column(name = "cur_name")
    private String curName;

    @Column(name = "cur_scale")
    private Integer curScale;

    @OneToMany(mappedBy = "currency", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    private List<CurrencyRate> rates = new ArrayList<>();
}