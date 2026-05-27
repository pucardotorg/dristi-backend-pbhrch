package org.pucar.dristi.caselifecycle.scheduler.internal.web.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pair<T, P> {

    private T key;
    private P value;


}
