package com.companyemployees.api.response;

/**
 * Respuesta para poblar selects en el front: label visible + value (id) a enviar al guardar.
 */
public record OptionApiResponse(String label, String value) {}
