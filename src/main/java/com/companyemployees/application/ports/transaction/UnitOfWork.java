package com.companyemployees.application.ports.transaction;

import java.util.function.Supplier;

/**
 * Puerto Unit of Work — vive en Application.
 * Agrupa operaciones en una sola transacción.
 * Infrastructure implementa esto con MongoTransactionManager.
 *
 * Equivalente a:
 *   EF Core: using var tx = context.Database.BeginTransaction()
 *   JPA:     @Transactional
 *   MongoDB: TransactionTemplate
 */
public interface UnitOfWork {
    /** Ejecuta una operación transaccional que retorna un valor */
    <T> T execute(Supplier<T> action);

    /** Ejecuta una operación transaccional sin valor de retorno */
    void execute(Runnable action);
}
