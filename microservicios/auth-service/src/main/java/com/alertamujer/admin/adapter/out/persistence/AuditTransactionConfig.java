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
 * The audit triggers (admin.log_row_change) store as actor the account given by
 * app.current_account_id and the IP from app.client_ip. This transaction manager sets them when
 * each transaction starts with SET LOCAL (set_config(..., true)), so they only apply to that
 * transaction and do not leak to other requests reusing the pooled connection.
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
            Long userId = currentUser();
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

        /** The JWT filter sets the users id (Long) as principal. */
        private static Long currentUser() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getPrincipal() instanceof Long id ? id : null;
        }
    }
}
