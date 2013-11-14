package com.android.sampler.yaySerialization.messages;

/**
 * Indicates that the payload has begun or ended for the given request type
 */
public class PayloadTerminalMessage {
    private final RequestType type;
    private final Terminals terminal;

    public PayloadTerminalMessage(RequestType type, Terminals terminal) {
        this.type = type;
        this.terminal = terminal;
    }

    public RequestType getType() {
        return type;
    }

    public Terminals getTerminal() {
        return terminal;
    }
}
