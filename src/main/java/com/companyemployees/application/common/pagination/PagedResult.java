package com.companyemployees.application.common.pagination;

import java.util.List;

/**
 * Resultado paginado interno a la capa Application.
 * Se separa del envelope HTTP (PagedResponse) para no contaminar Application con DTOs de API.
 */
public record PagedResult<T>(List<T> contenido, int pagina, int tamano, long total) {

    public int totalPaginas() {
        if (tamano <= 0) return 0;
        return (int) Math.ceil((double) total / (double) tamano);
    }

    public <R> PagedResult<R> map(java.util.function.Function<T, R> mapper) {
        return new PagedResult<>(contenido.stream().map(mapper).toList(), pagina, tamano, total);
    }
}
