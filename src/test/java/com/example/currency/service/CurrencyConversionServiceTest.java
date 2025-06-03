package com.example.currency.service;

import com.example.currency.cache.SimpleCache;
import com.example.currency.models.CurrencyRate;
import com.example.currency.repository.CurrencyRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyConversionServiceTest {

    @Mock
    private CurrencyService currencyService;

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @Mock
    private SimpleCache cacheService;

    @InjectMocks
    private CurrencyConversionService conversionService;

    @Test
    public void testGetBulkRates() {
        List<String> abbreviations = Arrays.asList("USD", "EUR");
        CurrencyRate usdRate = mock(CurrencyRate.class);
        CurrencyRate eurRate = mock(CurrencyRate.class);
        when(usdRate.getCurOfficialRate()).thenReturn(new BigDecimal("1.0"));
        when(eurRate.getCurOfficialRate()).thenReturn(new BigDecimal("0.85"));

        when(currencyService.getCurrencyRateByAbbreviation("USD")).thenReturn(usdRate);
        when(currencyService.getCurrencyRateByAbbreviation("EUR")).thenReturn(eurRate);

        List<CurrencyRate> rates = conversionService.getBulkRates(abbreviations);

        assertEquals(2, rates.size());
        assertEquals(usdRate, rates.get(0));
        assertEquals(eurRate, rates.get(1));
    }

    @Test
    public void testGetBulkRatesWithEmptyList() {
        List<String> abbreviations = Collections.emptyList();

        List<CurrencyRate> rates = conversionService.getBulkRates(abbreviations);

        assertTrue(rates.isEmpty());
    }

    @Test
    public void testGetBulkRatesWithInvalidAbbreviation() {
        List<String> abbreviations = Arrays.asList("USD", "XYZ");
        CurrencyRate usdRate = mock(CurrencyRate.class);
        when(usdRate.getCurOfficialRate()).thenReturn(new BigDecimal("1.0"));
        when(currencyService.getCurrencyRateByAbbreviation("USD")).thenReturn(usdRate);
        when(currencyService.getCurrencyRateByAbbreviation("XYZ")).thenThrow(new IllegalArgumentException("Currency not found for abbreviation: XYZ"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                conversionService.getBulkRates(abbreviations));
        assertEquals("Currency not found for abbreviation: XYZ", exception.getMessage());
    }

    @Test
    public void testGetBulkRatesWithCache() {
        List<String> abbreviations = Arrays.asList("USD", "EUR");
        CurrencyRate usdRate = mock(CurrencyRate.class);
        CurrencyRate eurRate = mock(CurrencyRate.class);
        when(usdRate.getCurOfficialRate()).thenReturn(new BigDecimal("1.0"));
        when(eurRate.getCurOfficialRate()).thenReturn(new BigDecimal("0.85"));

        when(cacheService.get("rateByAbbreviation:USD:" + java.time.LocalDate.now())).thenReturn(Optional.of(usdRate));
        when(cacheService.get("rateByAbbreviation:EUR:" + java.time.LocalDate.now())).thenReturn(Optional.of(eurRate));

        List<CurrencyRate> rates = conversionService.getBulkRates(abbreviations);

        assertEquals(2, rates.size());
        assertEquals(usdRate, rates.get(0));
        assertEquals(eurRate, rates.get(1));
        verify(currencyService, never()).getCurrencyRateByAbbreviation(anyString());
    }

    @Test
    public void testGetBulkRatesWithPartialCache() {
        List<String> abbreviations = Arrays.asList("USD", "EUR");
        CurrencyRate usdRate = mock(CurrencyRate.class);
        CurrencyRate eurRate = mock(CurrencyRate.class);
        when(usdRate.getCurOfficialRate()).thenReturn(new BigDecimal("1.0"));
        when(eurRate.getCurOfficialRate()).thenReturn(new BigDecimal("0.85"));

        when(cacheService.get("rateByAbbreviation:USD:" + java.time.LocalDate.now())).thenReturn(Optional.of(usdRate));
        when(cacheService.get("rateByAbbreviation:EUR:" + java.time.LocalDate.now())).thenReturn(Optional.empty());
        when(currencyService.getCurrencyRateByAbbreviation("EUR")).thenReturn(eurRate);

        List<CurrencyRate> rates = conversionService.getBulkRates(abbreviations);

        assertEquals(2, rates.size());
        assertEquals(usdRate, rates.get(0));
        assertEquals(eurRate, rates.get(1));
        verify(currencyService, times(1)).getCurrencyRateByAbbreviation("EUR");
        verify(cacheService, times(1)).put("rateByAbbreviation:EUR:" + java.time.LocalDate.now(), eurRate);
    }
}