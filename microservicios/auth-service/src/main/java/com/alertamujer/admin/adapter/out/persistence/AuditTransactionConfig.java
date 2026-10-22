package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.shared.web.AuditSource;
import com.alertamujer.shared.web.RequestInfo;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Los triggers de auditoría (admin.log_row_change) guardan como actor la cuenta que indique
 * app.current_account_id y la IP de app.client_ip. Este gestor de transacciones los fija al
 * iniciar cada transacción con SET LOCAL (set_config(..., true)), así valen solo para esa
 * transacción y no se filtran a otras peticiones que reutilicen la conexión del pool.
 */
@Configuration
public class AuditTransactionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new AuditingJpaTransactionManager(entityManagerFactory);
    }

    static class AuditingJpaTransactionManager extends JpaTransactionManager {

        AuditingJpaTransactionManager(EntityManagerFactory emf) {
            super(emf);
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            super.doBegin(transaction, definition);
            Long userId = usuarioActual();
            String ip = RequestInfo.ip();
            String source = AuditSource.current() != null ? AuditSource.current() : RequestInfo.source();
            if (userId == null && ip == null && source == null) return;
            EntityManagerHolder holder = (EntityManagerHolder)
                    TransactionSynchronizationManager.getResource(obtainEntityManagerFactory());
            if (holder == null) return;
            holder.getEntityManager().createNativeQuery(
                    "SELECT set_config('app.current_account_id', "
                    + "COALESCE((SELECT CAST(id AS text) FROM identity.account WHERE user_id = :userId), ''), true), "
                    + "set_config('app.client_ip', :ip, true), "
                    + "set_config('app.source', :origen, true)")
                    .setParameter("userId", userId == null ? -1L : userId)
                    .setParameter("ip", ip == null ? "" : ip)
                    .setParameter("origen", source == null ? "" : source.length() > 120 ? source.substring(0, 120) : source)
                    .getSingleResult();
        }

        /** El filtro JWT deja como principal el id de users (Long). */
        private static Long usuarioActual() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getPrincipal() instanceof Long id ? id : null;
        }
    }
}
