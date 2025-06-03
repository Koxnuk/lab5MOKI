package com.example.currency.repository;

import com.example.currency.models.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    @Query("SELECT cr FROM CurrencyRate cr WHERE cr.currency.curAbbreviation = :abbreviation AND cr.date = :date")
    List<CurrencyRate> findByCurrencyAbbreviationAndDate(@Param("abbreviation") String abbreviation, @Param("date") LocalDate date);
}