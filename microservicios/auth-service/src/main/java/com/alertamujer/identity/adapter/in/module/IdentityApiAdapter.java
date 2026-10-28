package com.alertamujer.identity.adapter.in.module;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.identity.application.port.in.QueryIdentityUseCase;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Input adapter: the IdentityApi contract for the other Auth Service modules and for Alert
 * and Evidence (through /internal/rpc). Translates the domain to the contract's UserSummary record.
 */
@Component
class IdentityApiAdapter implements IdentityApi {

    private final QueryIdentityUseCase queries;

    IdentityApiAdapter(QueryIdentityUseCase queries) {
        this.queries = queries;
    }

    @Override
    public Long requireProfileId(Long userId) {
        return findProfileId(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    @Override
    public Optional<Long> findProfileId(Long userId) {
        return queries.profileOf(userId);
    }

    @Override
    public Long requireAccountId(Long userId) {
        return queries.accountOf(userId).orElseThrow(() -> new ResourceNotFoundException("Account for user", userId));
    }

    @Override
    public Optional<UserSummary> findUser(Long userId) {
        return queries.user(userId).map(IdentityApiAdapter::summary);
    }

    @Override
    public Optional<UserSummary> findUserByEmail(String email) {
        return queries.byEmail(email).map(IdentityApiAdapter::summary);
    }

    @Override
    public Optional<UserSummary> findUserByProfileId(Long profileId) {
        return queries.byProfile(profileId).map(IdentityApiAdapter::summary);
    }

    @Override
    public Optional<UserSummary> findUserByAccountId(Long accountId) {
        return queries.byAccount(accountId).map(IdentityApiAdapter::summary);
    }

    @Override
    public Map<Long, UserSummary> usersById() {
        return summaries(queries.byId());
    }

    @Override
    public Map<Long, UserSummary> usersByProfileId() {
        return summaries(queries.byProfileId());
    }

    @Override
    public Map<Long, UserSummary> usersByAccountId() {
        return summaries(queries.byAccountId());
    }

    static UserSummary summary(User u) {
        return new UserSummary(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getTelephone(),
                u.getRoleId(), u.getCreatedAt());
    }

    private static Map<Long, UserSummary> summaries(Map<Long, User> byKey) {
        return byKey.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> summary(e.getValue())));
    }
}
