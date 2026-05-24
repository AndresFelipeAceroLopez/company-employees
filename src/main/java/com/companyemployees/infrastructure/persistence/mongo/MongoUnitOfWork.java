package com.companyemployees.infrastructure.persistence.mongo;

import com.companyemployees.application.ports.transaction.UnitOfWork;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Implementación del Unit of Work para MongoDB.
 * Agrupa operaciones en una sola transacción usando TransactionTemplate.
 *
 * Nota: MongoDB soporta transacciones multi-documento desde la versión 4.0
 * con replica sets. Si usas MongoDB standalone, las transacciones no funcionarán
 * pero el código seguirá funcionando sin atomicidad entre documentos.
 */
@Component
public class MongoUnitOfWork implements UnitOfWork {

    private final TransactionTemplate transactionTemplate;

    public MongoUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> action) {
        return transactionTemplate.execute(status -> action.get());
    }

    @Override
    public void execute(Runnable action) {
        transactionTemplate.executeWithoutResult(status -> action.run());
    }
}
