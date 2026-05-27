package org.pucar.dristi.caselifecycle.casemanagement.internal.util.jsonmapper;

import org.egov.common.contract.models.AuditDetails;
import org.json.JSONObject;
import org.pucar.dristi.common.contract.casemanagement.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

	private final JsonMapperUtil jsonMapperUtil;

	@Autowired
	public OrderMapper(JsonMapperUtil jsonMapperUtil) {
		this.jsonMapperUtil = jsonMapperUtil;
	}

	public Order getOrder(JSONObject jsonObject) {
		JSONObject dataObject = jsonObject.optJSONObject("Data");
		if (dataObject == null) {
			return null;
		}

		Order order = jsonMapperUtil.map(dataObject.optJSONObject("orderDetails"), Order.class);

		if (order != null) {
			order.setAuditDetails(jsonMapperUtil.map(dataObject.optJSONObject("orderDetails"), AuditDetails.class));
		}
		return order;
	}
}
