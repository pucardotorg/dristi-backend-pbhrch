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
 * <p>Methods are instance-level so existing Mockito-based tests
 * (`when(responseInfoFactory.createResponseInfoFromRequestInfo(...))`)
 * keep working unchanged.
 *
 * <p>Two call shapes that exist across the codebase are both supported:
 * <ul>
 *   <li>{@link #createResponseInfoFromRequestInfo} — used by case, hearing, lock-svc, etc.</li>
 *   <li>{@link #createResponseInfo} — used by order and bail-bond.</li>
 * </ul>
 */
@Component("commonResponseInfoFactory")
public class ResponseInfoFactory {

    public ResponseInfo createResponseInfoFromRequestInfo(final RequestInfo requestInfo, final Boolean success) {
        return createResponseInfoFromRequestInfo(requestInfo, success, RES_MSG_ID);
    }

    /** Overload that lets the caller set {@code resMsgId} (used by order). */
    public ResponseInfo createResponseInfoFromRequestInfo(final RequestInfo requestInfo, final Boolean success, final String msg) {
        final String apiId = requestInfo != null ? requestInfo.getApiId() : "";
        final String ver = requestInfo != null ? requestInfo.getVer() : "";
        final Long ts = requestInfo != null ? requestInfo.getTs() : null;
        final String msgId = requestInfo != null ? requestInfo.getMsgId() : "";
        final String responseStatus = Boolean.TRUE.equals(success) ? SUCCESSFUL : FAILED;
        return ResponseInfo.builder()
                .apiId(apiId)
                .ver(ver)
                .ts(ts)
                .resMsgId(msg)
                .msgId(msgId)
                .status(responseStatus)
                .build();
    }

    /** Alias used by services that prefer the shorter name. */
    public ResponseInfo createResponseInfo(final RequestInfo requestInfo, final Boolean success) {
        return createResponseInfoFromRequestInfo(requestInfo, success);
    }
}
