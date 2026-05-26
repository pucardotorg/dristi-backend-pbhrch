package org.pucar.dristi.caselifecycle.transformer.internal.event;

public interface EventListener<T,U> {
    void process(T event,U requestInfo);
}
