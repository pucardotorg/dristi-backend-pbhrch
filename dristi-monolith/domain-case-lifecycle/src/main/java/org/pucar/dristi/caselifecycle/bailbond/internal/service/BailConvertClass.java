package org.pucar.dristi.caselifecycle.bailbond.internal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.pucar.dristi.caselifecycle.bailbond.internal.util.SpringContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

public class BailConvertClass {

    private BailConvertClass() {
    }

    public static <E, P> P convertTo(JsonNode jsonNode, Class<E> valueType) throws IOException {
        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
        if (jsonNode.isArray()) {
            ObjectReader reader = objectMapper.readerFor(objectMapper.getTypeFactory().constructCollectionType(List.class, valueType));
            return reader.readValue(jsonNode);
        } else {
            return (P) objectMapper.treeToValue(jsonNode, valueType);
        }
    }
}
