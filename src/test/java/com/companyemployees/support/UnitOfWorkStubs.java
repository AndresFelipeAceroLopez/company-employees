package com.companyemployees.support;

import com.companyemployees.application.ports.transaction.UnitOfWork;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * Utilidad para pruebas unitarias: hace que un UnitOfWork mockeado ejecute la
 * accion recibida de forma sincrona (sin transaccion real), de modo que la
 * logica del use case corra normalmente bajo Mockito.
 */
public final class UnitOfWorkStubs {

    private UnitOfWorkStubs() {
    }

    @SuppressWarnings("unchecked")
    public static void runInline(UnitOfWork unitOfWork) {
        lenient().when(unitOfWork.execute(any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<Object>) inv.getArgument(0)).get());
        lenient().doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(unitOfWork).execute(any(Runnable.class));
    }
}
