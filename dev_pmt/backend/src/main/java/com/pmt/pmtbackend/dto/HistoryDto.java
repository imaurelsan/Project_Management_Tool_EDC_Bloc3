package com.pmt.pmtbackend.dto;

/** Ligne d'historique renvoyée par l'API. */
public record HistoryDto(Long id, String modifiedField, String oldValue,
                         String newValue, String modifiedDate, String modifiedBy) {}