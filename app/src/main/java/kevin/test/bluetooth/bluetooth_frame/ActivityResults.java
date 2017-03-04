package kevin.test.bluetooth.bluetooth_frame;

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

    public void setErrorMessage(String message);

    public void finishWithError(String message);

    public void showActivityError(Intent errorMessage);
}
