package com.example.currency.service;

import com.example.currency.cache.SimpleCache;
import com.example.currency.client.NbrbApiClient;
import com.example.currency.models.CurrencyInfo;
import com.example.currency.models.CurrencyRate;
import com.example.currency.repository.CurrencyInfoRepository;
import com.example.currency.repository.CurrencyRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

    @Mock
    private CurrencyInfoRepository currencyInfoRepository;

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @Mock
    private NbrbApiClient apiClient;

    @Mock
    private SimpleCache cacheService;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    public void testGetCurrencyRateByAbbreviationFoundInCache() {
        String abbreviation = "USD";
        CurrencyRate cachedRate = mock(CurrencyRate.class);
        when(cachedRate.getCurOfficialRate()).thenReturn(new BigDecimal("1.0"));
        when(cachedRate.getDate()).thenReturn(LocalDate.now());
        when(cacheService.get("rateByAbbreviation:USD:" + LocalDate.now())).thenReturn(Optional.of(cachedRate));

        CurrencyRate result = currencyService.getCurrencyRateByAbbreviation(abbreviation);

        assertEquals(cachedRate, result);
        verify(currencyInfoRepository, never()).findByCurAbbreviation(anyString());
        verify(apiClient, never()).getCurrencyRate(anyInt());
        verify(currencyRateRepository, never()).save(any(CurrencyRate.class));
    }

    @Test
    public void testGetCurrencyRateByAbbreviationFoundInDb() {
        String abbreviation = "USD";
        CurrencyInfo currency = mock(CurrencyInfo.class);
        CurrencyRate rate = mock(CurrencyRate.class);
        List<CurrencyRate> rates = mock(List.class);
        when(currency.getCurId()).thenReturn(1);
        when(currency.getCurAbbreviation()).thenReturn(abbreviation);
        when(rate.getCurOfficialRate()).thenReturn(new BigDecimal("1.0"));
        when(rate.getDate()).thenReturn(LocalDate.now());
        when(currency.getRates()).thenReturn(rates);
        when(rates.isEmpty()).thenReturn(false);
        when(rates.get(0)).thenReturn(rate);

        when(cacheService.get("rateByAbbreviation:USD:" + LocalDate.now())).thenReturn(Optional.empty());
        when(currencyInfoRepository.findByCurAbbreviation(abbreviation)).thenReturn(Optional.of(currency));

        CurrencyRate result = currencyService.getCurrencyRateByAbbreviation(abbreviation);

        assertEquals(rate, result);
        verify(cacheService).put("rateByAbbreviation:USD:" + LocalDate.now(), rate);
        verify(apiClient, never()).getCurrencyRate(anyInt());
        verify(currencyRateRepository, never()).save(any(CurrencyRate.class));
    }

    @Test
    public void testGetCurrencyRateByAbbreviationFromApi() {
        String abbreviation = "USD";
        CurrencyInfo currency = mock(CurrencyInfo.class);
        CurrencyRate rate = mock(CurrencyRate.class);
        List<CurrencyRate> rates = mock(List.class);
        when(currency.getCurId()).thenReturn(1);
        when(currency.getCurAbbreviation()).thenReturn(abbreviation);
        when(currency.getRates()).thenReturn(rates);
        when(rates.isEmpty()).thenReturn(true);
        when(rate.getCurOfficialRate()).thenReturn(new BigDecimal("1.0"));
        when(rate.getDate()).thenReturn(LocalDate.now());

        when(cacheService.get("rateByAbbreviation:USD:" + LocalDate.now())).thenReturn(Optional.empty());
        when(currencyInfoRepository.findByCurAbbreviation(abbreviation)).thenReturn(Optional.of(currency));
        when(apiClient.getCurrencyRate(1)).thenReturn(rate);
        when(currencyRateRepository.save(any(CurrencyRate.class))).thenReturn(rate);

        CurrencyRate result = currencyService.getCurrencyRateByAbbreviation(abbreviation);

        assertEquals(rate, result);
        verify(cacheService).put("rateByAbbreviation:USD:" + LocalDate.now(), rate);
        verify(currencyRateRepository).save(rate);
        verify(apiClient).getCurrencyRate(1);
    }

    @Test
    public void testGetCurrencyRateByAbbreviationNotFound() {
        String abbreviation = "XYZ";
        when(cacheService.get("rateByAbbreviation:XYZ:" + LocalDate.now())).thenReturn(Optional.empty());
        when(currencyInfoRepository.findByCurAbbreviation(abbreviation)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                currencyService.getCurrencyRateByAbbreviation(abbreviation));
        assertEquals("Currency not found for abbreviation: XYZ", exception.getMessage());
        verify(apiClient, never()).getCurrencyRate(anyInt());
        verify(currencyRateRepository, never()).save(any(CurrencyRate.class));
    }

    @Test
    public void testGetCurrencyRateByAbbreviationNullInput() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                currencyService.getCurrencyRateByAbbreviation(null));
        assertEquals("Currency abbreviation cannot be null", exception.getMessage());
        verify(cacheService, never()).get(anyString());
        verify(currencyInfoRepository, never()).findByCurAbbreviation(anyString());
        verify(apiClient, never()).getCurrencyRate(anyInt());
        verify(currencyRateRepository, never()).save(any(CurrencyRate.class));
    }

    @Test
    public void testGetCurrencyRateByAbbreviationEmptyInput() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                currencyService.getCurrencyRateByAbbreviation(""));
        assertEquals("Currency abbreviation cannot be empty", exception.getMessage());
        verify(cacheService, never()).get(anyString());
        verify(currencyInfoRepository, never()).findByCurAbbreviation(anyString());
        verify(apiClient, never()).getCurrencyRate(anyInt());
        verify(currencyRateRepository, never()).save(any(CurrencyRate.class));
    }

    @Test
    public void testGetCurrencyRateByAbbreviationApiFailure() {
        String abbreviation = "USD";
        CurrencyInfo currency = mock(CurrencyInfo.class);
        List<CurrencyRate> rates = mock(List.class);
        when(currency.getCurId()).thenReturn(1);
        when(currency.getCurAbbreviation()).thenReturn(abbreviation);
        when(currency.getRates()).thenReturn(rates);
        when(rates.isEmpty()).thenReturn(true);

        when(cacheService.get("rateByAbbreviation:USD:" + LocalDate.now())).thenReturn(Optional.empty());
        when(currencyInfoRepository.findByCurAbbreviation(abbreviation)).thenReturn(Optional.of(currency));
        when(apiClient.getCurrencyRate(1)).thenThrow(new RuntimeException("API failure"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                currencyService.getCurrencyRateByAbbreviation(abbreviation));
        assertEquals("API failure", exception.getMessage());
        verify(currencyRateRepository, never()).save(any(CurrencyRate.class));
        verify(cacheService, never()).put(anyString(), any());
    }
}
