package com.companyemployees.application.common.pagination;

import java.util.Locale;
import java.util.Set;

/**
 * Criterios de paginacion validados (Application layer).
 * Garantiza limites razonables y campos permitidos.
 * <p>
 * pagina:  base 1
 * tamano:  1..100
 * orden:   campo permitido (whitelist)
 * dir:     asc | desc
 * buscar:  texto opcional, null-safe
 */
public record PageCriteria(int pagina, int tamano, String orden, String dir, String buscar) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;
    public static final String DEFAULT_SORT = "apellido";
    public static final String DEFAULT_DIR = "asc";

    public static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("nombre", "apellido", "correo", "salario", "cargo", "status");

    public PageCriteria {
        if (pagina < 1) {
            throw new IllegalArgumentException("El parametro 'pagina' debe ser >= 1");
        }
        if (tamano < 1) {
            throw new IllegalArgumentException("El parametro 'tamano' debe ser >= 1");
        }
        if (tamano > MAX_SIZE) {
            throw new IllegalArgumentException("El parametro 'tamano' no puede superar " + MAX_SIZE);
        }
        orden = (orden == null || orden.isBlank()) ? DEFAULT_SORT : orden.trim().toLowerCase(Locale.ROOT);
        dir = (dir == null || dir.isBlank()) ? DEFAULT_DIR : dir.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_SORT_FIELDS.contains(orden)) {
            throw new IllegalArgumentException(
                    "El parametro 'orden' debe ser uno de: " + ALLOWED_SORT_FIELDS);
        }
        if (!dir.equals("asc") && !dir.equals("desc")) {
            throw new IllegalArgumentException("El parametro 'dir' debe ser 'asc' o 'desc'");
        }
        buscar = (buscar == null || buscar.isBlank()) ? null : buscar.trim();
    }

    public static PageCriteria of(Integer pagina, Integer tamano, String orden, String dir, String buscar) {
        return new PageCriteria(
                pagina == null ? DEFAULT_PAGE : pagina,
                tamano == null ? DEFAULT_SIZE : tamano,
                orden,
                dir,
                buscar
        );
    }

    public boolean isAsc() {
        return "asc".equals(dir);
    }

    public int offset() {
        return (pagina - 1) * tamano;
    }
}
