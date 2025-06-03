package com.example.currency.service;

import com.example.currency.cache.SimpleCache;
import com.example.currency.models.CurrencyRate;
import com.example.currency.repository.CurrencyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CurrencyConversionService {

    private final CurrencyService currencyService;
    private final CurrencyRateRepository currencyRateRepository;
    private final SimpleCache cacheService;

    @Autowired
    public CurrencyConversionService(
            CurrencyService currencyService,
            CurrencyRateRepository currencyRateRepository,
            SimpleCache cacheService
    ) {
        this.currencyService = currencyService;
        this.currencyRateRepository = currencyRateRepository;
        this.cacheService = cacheService;
    }

    public BigDecimal convertCurrency(Integer fromCurId, Integer toCurId, BigDecimal amount) {
        String cacheKey = "convert:" + fromCurId + ":" + toCurId + ":" + amount.toString();
        Optional<Object> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return (BigDecimal) cached.get();
        }

        CurrencyRate fromRate = currencyService.getCurrencyRate(fromCurId);
        CurrencyRate toRate = currencyService.getCurrencyRate(toCurId);

        BigDecimal fromRatePerUnit = fromRate.getCurOfficialRate()
                .divide(BigDecimal.valueOf(fromRate.getCurScale()), 6, RoundingMode.HALF_UP);

        BigDecimal toRatePerUnit = toRate.getCurOfficialRate()
                .divide(BigDecimal.valueOf(toRate.getCurScale()), 6, RoundingMode.HALF_UP);

        BigDecimal result = amount.multiply(fromRatePerUnit)
                .divide(toRatePerUnit, 2, RoundingMode.HALF_UP);

        cacheService.put(cacheKey, result);
        return result;
    }

    public CurrencyRate createRate(CurrencyRate rate) {
        CurrencyRate savedRate = currencyRateRepository.save(rate);
        String cacheKey = generateCacheKey(rate.getCurrency().getCurAbbreviation(), rate.getDate());
        cacheService.put(cacheKey, List.of(savedRate));
        return savedRate;
    }

    public List<CurrencyRate> getAllRates() {
        String cacheKey = "allRates";
        Optional<Object> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return (List<CurrencyRate>) cached.get();
        }

        List<CurrencyRate> rates = currencyRateRepository.findAll();
        cacheService.put(cacheKey, rates);
        return rates;
    }

    public Optional<CurrencyRate> getRateById(Long id) {
        String cacheKey = "rateById:" + id;
        Optional<Object> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return Optional.of((CurrencyRate) cached.get());
        }

        Optional<CurrencyRate> rate = currencyRateRepository.findById(id);
        rate.ifPresent(r -> cacheService.put(cacheKey, r));
        return rate;
    }

    public CurrencyRate updateRate(Long id, CurrencyRate updatedRate) {
        Optional<CurrencyRate> existingRate = currencyRateRepository.findById(id);
        if (existingRate.isPresent()) {
            CurrencyRate rate = existingRate.get();
            rate.setCurOfficialRate(updatedRate.getCurOfficialRate());
            rate.setCurScale(updatedRate.getCurScale());
            rate.setDate(updatedRate.getDate());
            rate.setCurrency(updatedRate.getCurrency());
            CurrencyRate savedRate = currencyRateRepository.save(rate);
            String cacheKey = generateCacheKey(rate.getCurrency().getCurAbbreviation(), rate.getDate());
            cacheService.put(cacheKey, List.of(savedRate));
            cacheService.remove("rateById:" + id);
            return savedRate;
        }
        throw new RuntimeException("Rate not found with id: " + id);
    }

    public void deleteRate(Long id) {
        Optional<CurrencyRate> rate = currencyRateRepository.findById(id);
        if (rate.isPresent()) {
            String cacheKey = generateCacheKey(rate.get().getCurrency().getCurAbbreviation(), rate.get().getDate());
            currencyRateRepository.deleteById(id);
            cacheService.remove(cacheKey);
            cacheService.remove("rateById:" + id);
        } else {
            throw new RuntimeException("Rate not found with id: " + id);
        }
    }

    public List<CurrencyRate> getRatesByAbbreviationAndDate(String abbreviation, LocalDate date) {
        String cacheKey = generateCacheKey(abbreviation, date);
        Optional<Object> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return (List<CurrencyRate>) cached.get();
        }

        List<CurrencyRate> rates = currencyRateRepository.findByCurrencyAbbreviationAndDate(abbreviation, date);
        cacheService.put(cacheKey, rates);
        return rates;
    }

    private String generateCacheKey(String abbreviation, LocalDate date) {
        return "rates:" + abbreviation + ":" + date.toString();
    }

    public List<CurrencyRate> getBulkRates(List<String> abbreviations) {
        return abbreviations.stream()
                .map(abbreviation -> {
                    String cacheKey = "rateByAbbreviation:" + abbreviation + ":" + LocalDate.now();
                    Optional<Object> cached = cacheService.get(cacheKey);
                    if (cached.isPresent()) {
                        return (CurrencyRate) cached.get();
                    } else {
                        CurrencyRate rate = currencyService.getCurrencyRateByAbbreviation(abbreviation);
                        cacheService.put(cacheKey, rate);
                        return rate;
                    }
                })
                .collect(Collectors.toList());
    }
}