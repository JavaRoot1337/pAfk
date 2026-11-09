package org.flaudz122.pafk.reward;

@FunctionalInterface
public interface Sink {
    void dispatch(String command);
}
