package org.pucar.dristi.payments.calculator.internal.payment.calculator.service;

import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.Calculation;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.SummonCalculationCriteria;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.TaskPaymentCriteria;
import org.egov.common.contract.request.RequestInfo;

public interface Payment {

    Calculation calculatePayment(RequestInfo requestInfo, SummonCalculationCriteria criteria);

    Calculation calculatePayment(RequestInfo requestInfo, TaskPaymentCriteria criteria);

}
