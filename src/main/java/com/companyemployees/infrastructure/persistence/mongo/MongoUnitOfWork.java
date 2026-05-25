package com.companyemployees.infrastructure.persistence.mongo;

import com.companyemployees.application.ports.transaction.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MongoUnitOfWork.class);
    private final TransactionTemplate transactionTemplate;

    public MongoUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> action) {
        log.info("Inicio de una transacción");
        try {
            T result = transactionTemplate.execute(status -> action.get());
            log.info("Confirmación de una transacción");
            return result;
        } catch (Exception e) {
            log.error("Rollback de una transacción. Motivo: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void execute(Runnable action) {
        log.info("Inicio de una transacción");
        try {
            transactionTemplate.executeWithoutResult(status -> action.run());
            log.info("Confirmación de una transacción");
        } catch (Exception e) {
            log.error("Rollback de una transacción. Motivo: {}", e.getMessage());
            throw e;
        }
    }
}
