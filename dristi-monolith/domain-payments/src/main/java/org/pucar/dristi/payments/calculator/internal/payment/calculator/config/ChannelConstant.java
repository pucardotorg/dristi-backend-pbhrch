package org.pucar.dristi.payments.calculator.internal.payment.calculator.config;


import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor
@Configuration
public class ChannelConstant {

    private static final Map<String, String> CONSTANTS = new HashMap<>();

    public static String getConstant(String key) {
        return CONSTANTS.get(key);
    }

    @PostConstruct
    public void init() {
        CONSTANTS.put("EPOST", "org.pucar.dristi.payments.calculator.internal.payment.calculator.service.channels.EPostFeeService");
        CONSTANTS.put("POLICE", "org.pucar.dristi.payments.calculator.internal.payment.calculator.service.channels.PoliceFeeService");
        CONSTANTS.put("EMAIL", "org.pucar.dristi.payments.calculator.internal.payment.calculator.service.channels.EmailFeeService");
        CONSTANTS.put("SMS", "org.pucar.dristi.payments.calculator.internal.payment.calculator.service.channels.SmsFeeService");
        CONSTANTS.put("RPAD", "org.pucar.dristi.payments.calculator.internal.payment.calculator.service.channels.RPADFeeService");
    }

}
