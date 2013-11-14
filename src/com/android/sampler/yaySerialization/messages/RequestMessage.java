package com.android.sampler.yaySerialization.messages;

/**
 * Used when asking for some data to be received
 */
public class RequestMessage {
    private final RequestType type;

    public RequestMessage(RequestType type) {
        this.type = type;
    }

    public RequestType getType() {
        return type;
    }
}
