package kevin.test.bluetooth.bluetooth_frame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothDataProvider;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.NFK_ArduinoBluetoothClient;

public class Main extends AppCompatActivity implements ActivityResults {
    //Class Attributes
    private Button m_buttonShowPaired;
    private ArduinoBluetoothClient m_client;
    private ActionBar m_actionBar;

    //end of Class Attributes


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_client = NFK_ArduinoBluetoothClient.getClient();
        setContentView(R.layout.activity_main);
        Toolbar usedToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(usedToolbar);
        m_actionBar = getSupportActionBar();
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_data, false);
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_connection, false);
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_view, false);
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_headers, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPostResume() {
        m_actionBar.show();
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        if (preference.getBoolean(SettingsActivity.KEY_DATA_DELETEONFINISH, true)) {
            (new BluetoothDataProvider(getApplicationContext())).deleteAll();
        }
        if (m_client != null) {
            m_client.destroy();
            m_client = null;
        }
        if (m_buttonShowPaired != null) {
            m_buttonShowPaired = null;
        }
    }

    @Override
    public void setErrorMessage(String message) {
        Intent data = new Intent("Closed on Error");
        data.putExtra(RESULTKEY_ERROR_MESSAGE, message);
        setResult(RESULT_ERROR, data);
    }

    @Override
    public void finishWithError(String message) {
        setErrorMessage(message);
        finish();
    }

    @Override
    public void showActivityError(Intent errorMessage) {
        Toast.makeText(this, errorMessage.getStringExtra(RESULTKEY_ERROR_MESSAGE), Toast.LENGTH_LONG).show();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.main_actionbar_menu_item_settings): {
                Intent startSettings = new Intent(this, SettingsActivity.class);
                startActivityForResult(startSettings, SETTINGSACTIVITY_REQUEST_CODE);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (DEVICELIST_REQUEST_CODE): {
                if (resultCode == DeviceList.RESULT_ERROR) {
                    showActivityError(data);
                }
                break;
            }
            case (SETTINGSACTIVITY_REQUEST_CODE): {
                if (resultCode == SettingsActivity.RESULT_ERROR) {
                    showActivityError(data);
                }
                break;
            }
        }
    }

    public void showDeviceListClicked(View v) {
        Intent showAddresses = new Intent(Main.this, DeviceList.class);
        startActivityForResult(showAddresses, DEVICELIST_REQUEST_CODE);
    }
}
