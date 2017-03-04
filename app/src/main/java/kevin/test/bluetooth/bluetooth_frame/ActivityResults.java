package kevin.test.bluetooth.bluetooth_frame;

import android.content.Intent;

/**
 * @author kevin
 * @version 0.0 from 04.03.2017 in Bluetooth_Frame
 **/

public interface ActivityResults {
    public static final int RESULT_ERROR = -42; //42 is best -> -42 is worst ;)
    public static final String RESULTKEY_ERROR_MESSAGE = "Error Message";

    public void setErrorMessage(String message);

    public void finishWithError(String message);

    public void showActivityError(Intent errorMessage);
}
