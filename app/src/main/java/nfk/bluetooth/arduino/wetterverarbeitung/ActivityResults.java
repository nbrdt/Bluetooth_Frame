package nfk.bluetooth.arduino.wetterverarbeitung;

import android.content.Intent;

/**
 * @author KI & NB
 * @version 1.1
 **/

public interface ActivityResults {
    public static final int ACTIVATEBLUETOOTH_REQUEST_CODE = 1;
    public static final int DEVICELIST_REQUEST_CODE = 2;
    public static final int SETTINGSACTIVITY_REQUEST_CODE = 3;
    public static final int DIAGRAMACTIVITY_REQUEST_CODE = 4;
    public static final int RESULT_ERROR = -42; //42 is best -> -42 is worst ;)
    public static final String RESULTKEY_ERROR_MESSAGE = "Error Message";

    /**
     * Sets The Activity Result to RESULT_ERROR with an Intent containing message with the Key RESULTKEY_ERROR_MESSAGE.
     *
     * @param message The Result Error message to be passed to the calling Activity
     */
    public void setErrorMessage(String message);

    /**
     * Sets the Activity Result as defined by setErrorMessage and finishes the Activity afterwards.
     * @param message The Result Error message to be passed to the calling Activity
     */
    public void finishWithError(String message);

    /**
     * Shows an Activity Error, received in an Intent defined by setErrorMessage.
     * @param errorMessage An Intent containing the Error Message to display.
     */
    public void showActivityError(Intent errorMessage);
}
