package com.example.currency.controller;

import com.example.currency.models.CurrencyInfo;
import com.example.currency.service.CurrencyService;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/currency/info")
@Tag(name = "Currency Info", description = "API for managing currency information")
public class CurrencyInfoController {

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyInfoController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    @Operation(summary = "Get all currencies", description = "Returns a list of all available currencies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved currencies"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyInfo>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/db")
    @Operation(summary = "Get all currencies from database", description = "Returns a list of all currencies stored in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved currencies"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyInfo>> getAllCurrenciesFromDb() {
        return ResponseEntity.ok(currencyService.getAllCurrenciesFromDb());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get currency by ID", description = "Returns a currency by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved currency"),
            @ApiResponse(responseCode = "404", description = "Currency not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyInfo> getCurrencyById(@PathVariable Integer id) {
        Optional<CurrencyInfo> currency = currencyService.getCurrencyById(id);
        if (currency.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Currency not found with ID: " + id);
        }
        return ResponseEntity.ok(currency.get());
    }

    @PostMapping
    @Operation(summary = "Create a new currency", description = "Creates a new currency entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyInfo> createCurrency(@Valid @RequestBody CurrencyInfo currencyInfo) {
        CurrencyInfo created = currencyService.createCurrency(currencyInfo);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a currency", description = "Updates an existing currency by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency updated successfully"),
            @ApiResponse(responseCode = "404", description = "Currency not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyInfo> updateCurrency(@PathVariable Integer id, @Valid @RequestBody CurrencyInfo currencyInfo) {
        CurrencyInfo updated = currencyService.updateCurrency(id, currencyInfo);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a currency", description = "Deletes a currency by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Currency deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Currency not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteCurrency(@PathVariable Integer id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }
}
