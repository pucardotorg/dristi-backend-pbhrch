package org.pucar.dristi.payments.calculator.internal.payment.calculator.factory;

import org.pucar.dristi.payments.calculator.internal.payment.calculator.service.Payment;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.Calculation;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.SummonCalculationCriteria;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.TaskPaymentCriteria;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.request.RequestInfo;


@Setter
@AllArgsConstructor
public class PaymentContext {

    private Payment payment;

    @Deprecated
    public Calculation calculatePayment(RequestInfo requestInfo, SummonCalculationCriteria criteria) {
        return payment.calculatePayment(requestInfo, criteria);
    }

    public Calculation calculatePayment(RequestInfo requestInfo, TaskPaymentCriteria criteria) {
        return payment.calculatePayment(requestInfo, criteria);
    }
}
