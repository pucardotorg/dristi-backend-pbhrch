package org.pucar.dristi.common.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defensive-copy helpers for {@link RequestInfo} (Rule 40). When code reachable
 * from a direct method call (a {@code *Api} impl, internal service, or any
 * helper that may be invoked across subdomains) needs to enrich a
 * {@code RequestInfo}, it must build a copy — not mutate the caller's
 * instance, since direct calls share the reference.
 */
public final class RequestInfoUtil {

    private RequestInfoUtil() {}

    /** Defensive copy of {@code source} with {@code role} appended to the user's roles list. */
    public static RequestInfo withExtraRole(RequestInfo source, Role role) {
        User src = source.getUserInfo();
        List<Role> roles = new ArrayList<>(
                src.getRoles() != null ? src.getRoles() : Collections.emptyList());
        roles.add(role);
        User newUser = User.builder()
                .id(src.getId()).userName(src.getUserName()).name(src.getName())
                .type(src.getType()).mobileNumber(src.getMobileNumber()).emailId(src.getEmailId())
                .roles(roles).tenantId(src.getTenantId()).uuid(src.getUuid())
                .build();
        return withUser(source, newUser);
    }

    /** Defensive copy of {@code source} with {@code userInfo} replaced. */
    public static RequestInfo withUser(RequestInfo source, User userInfo) {
        return RequestInfo.builder()
                .apiId(source.getApiId()).ver(source.getVer())
                .ts(source.getTs()).action(source.getAction())
                .did(source.getDid()).key(source.getKey())
                .msgId(source.getMsgId()).authToken(source.getAuthToken())
                .correlationId(source.getCorrelationId())
                .userInfo(userInfo)
                .build();
    }
}
