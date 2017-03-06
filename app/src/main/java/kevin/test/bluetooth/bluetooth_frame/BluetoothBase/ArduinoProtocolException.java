package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

/**
 * @author KI
 * @version 1.0
 **/
public class ArduinoProtocolException extends RuntimeException {
    double falseMessages;
    double receivedMessages;
    public ArduinoProtocolException(String message) {
        super(message);
    }
    public ArduinoProtocolException(String message, double falseMessages, double receivedMessages) {
        super(message + " with:" + falseMessages + " false Messages and:" + receivedMessages + " received Messages");
        this.falseMessages = falseMessages;
        this.receivedMessages = receivedMessages;
    }
    public ArduinoProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public double getFalseMessages() {
        return falseMessages;
    }

    public void setFalseMessages(double falseMessages) {
        this.falseMessages = falseMessages;
    }

    public double getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(double receivedMessages) {
        this.receivedMessages = receivedMessages;
    }
}
