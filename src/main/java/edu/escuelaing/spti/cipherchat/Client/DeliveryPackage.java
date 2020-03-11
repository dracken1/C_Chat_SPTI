package edu.escuelaing.spti.cipherchat.Client;

public class DeliveryPackage {
    String from;
    String to;
    String message;

    public DeliveryPackage(String from, String to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }
}
