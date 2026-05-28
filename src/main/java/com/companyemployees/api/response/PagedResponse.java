package com.companyemployees.api.response;

import com.companyemployees.application.common.pagination.PagedResult;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope HTTP estandar para respuestas paginadas.
 */
public record PagedResponse<T>(
        List<T> datos,
        int pagina,
        int tamano,
        long total,
        int totalPaginas
) {
    public static <D, A> PagedResponse<A> from(PagedResult<D> result, Function<D, A> mapper) {
        return new PagedResponse<>(
                result.contenido().stream().map(mapper).toList(),
                result.pagina(),
                result.tamano(),
                result.total(),
                result.totalPaginas()
        );
    }
}
