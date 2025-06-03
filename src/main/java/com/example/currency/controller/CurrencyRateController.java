package com.example.currency.controller;

import com.example.currency.models.CurrencyRate;
import com.example.currency.service.CurrencyConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/currency/rates")
@Tag(name = "Currency Rates", description = "API for managing currency rates and conversions")
public class CurrencyRateController {

    private final CurrencyConversionService conversionService;

    @Autowired
    public CurrencyRateController(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/convert")
    @Operation(summary = "Convert currency", description = "Converts an amount from one currency to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversion successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input or amount less than or equal to zero"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> convert(
            @RequestParam Integer from,
            @RequestParam Integer to,
            @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        try {
            BigDecimal result = conversionService.convertCurrency(from, to, amount);
            return ResponseEntity.ok(Map.of(
                    "amount", amount,
                    "from", from,
                    "to", to,
                    "result", result
            ));
        } catch (Exception e) {
            throw new IllegalArgumentException("Conversion error: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all currency rates", description = "Returns a list of all currency rates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rates"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyRate>> getAllRates() {
        return ResponseEntity.ok(conversionService.getAllRates());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get currency rate by ID", description = "Returns a currency rate by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rate"),
            @ApiResponse(responseCode = "404", description = "Rate not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyRate> getRateById(@PathVariable Long id) {
        Optional<CurrencyRate> rate = conversionService.getRateById(id);
        if (rate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate not found with ID: " + id);
        }
        return ResponseEntity.ok(rate.get());
    }

    @PostMapping
    @Operation(summary = "Create a new currency rate", description = "Creates a new currency rate entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rate created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyRate> createRate(@Valid @RequestBody CurrencyRate rate) {
        CurrencyRate created = conversionService.createRate(rate);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a currency rate", description = "Updates an existing currency rate by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rate updated successfully"),
            @ApiResponse(responseCode = "404", description = "Rate not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyRate> updateRate(@PathVariable Long id, @Valid @RequestBody CurrencyRate rate) {
        CurrencyRate updated = conversionService.updateRate(id, rate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a currency rate", description = "Deletes a currency rate by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rate deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rate not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteRate(@PathVariable Long id) {
        conversionService.deleteRate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-abbreviation")
    @Operation(summary = "Get rates by abbreviation and date", description = "Returns currency rates for a specific abbreviation and date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rates"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyRate>> getRatesByAbbreviationAndDate(
            @RequestParam String abbreviation,
            @RequestParam LocalDate date) {
        List<CurrencyRate> rates = conversionService.getRatesByAbbreviationAndDate(abbreviation, date);
        return ResponseEntity.ok(rates);
    }

    @PostMapping("/bulk-rates")
    @Operation(summary = "Get rates for multiple currencies", description = "Returns rates for a list of currency abbreviations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rates"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyRate>> getBulkRates(@RequestBody List<String> abbreviations) {
        List<CurrencyRate> rates = conversionService.getBulkRates(abbreviations);
        return ResponseEntity.ok(rates);
    }
}