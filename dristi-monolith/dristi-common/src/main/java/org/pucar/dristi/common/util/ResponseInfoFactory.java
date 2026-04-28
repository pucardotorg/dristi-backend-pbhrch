// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.stereotype.Component;

import static org.pucar.dristi.common.config.CommonConstants.FAILED;
import static org.pucar.dristi.common.config.CommonConstants.RES_MSG_ID;
import static org.pucar.dristi.common.config.CommonConstants.SUCCESSFUL;

/**
 * Builds {@link ResponseInfo} from a {@link RequestInfo}, mirroring the
 * tiny helper that 30 DRISTI services duplicated locally.
 *
 * <p>Both call shapes that exist across the codebase are supported:
 * <ul>
 *   <li>{@link #createResponseInfoFromRequestInfo(RequestInfo, Boolean)} —
 *       used by case, hearing, lock-svc, etc.</li>
 *   <li>{@link #createResponseInfo(RequestInfo, Boolean)} — used by order
 *       and bail-bond.</li>
 * </ul>
 *
 * <p>Both static and instance variants exist because some services call
 * {@code ResponseInfoFactory.createResponseInfo(...)} (static) while
 * others {@code @Autowired} the bean and use the instance method. Either
 * works.
 */
@Component("commonResponseInfoFactory")
public class ResponseInfoFactory {

    /** Static variant — preferred for new code. */
    public static ResponseInfo createResponseInfo(final RequestInfo requestInfo, final Boolean success) {
        final String apiId = requestInfo != null ? requestInfo.getApiId() : "";
        final String ver = requestInfo != null ? requestInfo.getVer() : "";
        final Long ts = requestInfo != null ? requestInfo.getTs() : null;
        final String msgId = requestInfo != null ? requestInfo.getMsgId() : "";
        final String responseStatus = Boolean.TRUE.equals(success) ? SUCCESSFUL : FAILED;
        return ResponseInfo.builder()
                .apiId(apiId)
                .ver(ver)
                .ts(ts)
                .resMsgId(RES_MSG_ID)
                .msgId(msgId)
                .status(responseStatus)
                .build();
    }

    /** Alias kept for legacy callers that imported the verbose name. */
    public static ResponseInfo createResponseInfoFromRequestInfo(
            final RequestInfo requestInfo, final Boolean success) {
        return createResponseInfo(requestInfo, success);
    }

    // Instance-method overloads for callers that @Autowire the bean and
    // call it through the instance (e.g. case-svc's existing controllers).
    public ResponseInfo buildResponseInfo(final RequestInfo requestInfo, final Boolean success) {
        return createResponseInfo(requestInfo, success);
    }
}
