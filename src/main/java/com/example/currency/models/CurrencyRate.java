package com.example.currency.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "currency_rate")
@Data
public class CurrencyRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cur_official_rate")
    private BigDecimal curOfficialRate;

    @Column(name = "cur_scale")
    private Integer curScale;

    @Column(name = "date")
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    @JsonBackReference
    @ToString.Exclude
    private CurrencyInfo currency;
}