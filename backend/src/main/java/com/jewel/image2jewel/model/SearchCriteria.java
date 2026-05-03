package com.jewel.image2jewel.model;

/**
 * Structured search criteria extracted by the LLM
 */
public record SearchCriteria(
    String category,
    String metal,
    String stone,
    String style,
    String reasoning
) {}
