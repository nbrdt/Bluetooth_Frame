package kevin.test.bluetooth.bluetooth_frame;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothConnectionStateException;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothDataProvider;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothDataSet;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.NFK_ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.UnrecognizableBluetoothDataException;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramManager;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramSettings;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramViewSettings;

public class Connected extends AppCompatActivity implements DiagramManager.DataProvider {
    private static final String LOG_TAG = "Connected Activity";
    private static final String FRAGMENT_TAG_TEMPERATURE = "Temperature";
    private static final String FRAGMENT_TAG_HUMIDITY = "Humidity";
    private static final String FRAGMENT_TAG_SOILMOISTURE = "Soil Moisture";

    private ArduinoBluetoothClient client;
    private DiagramSettings m_globalSettings;
    private DiagramManager m_diagramManager;
    private BluetoothDataProvider m_dataProvider;
    private byte readyToShow = -1;

    private List<DiagramSettings> m_diagrams;
    private List<BluetoothDataSet> m_bluetoothData = new LinkedList<>();
    private ArrayList<Integer> m_temperatureValues = new ArrayList<>();
    private ArrayList<Integer> m_humidityValues = new ArrayList<>();
    private ArrayList<Integer> m_soilValues = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        m_dataProvider = new BluetoothDataProvider(getApplicationContext());
        String addresse = getIntent().getExtras().getString("addresse");

        Toast.makeText(getApplicationContext(), addresse, Toast.LENGTH_SHORT).show();


        if (addresse != null && !addresse.isEmpty()) {
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(Connected.this);
            try {
                client = NFK_ArduinoBluetoothClient.getClient(Integer.parseInt(preference.getString(SettingsActivity.KEY_CONNECTION_RECEIVERATE, SettingsActivity.PREF_DEFAULTVALUE_CONNECTION_RECEIVERATE)));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Could not resolve receive Rate. You may only enter Numbers in the Settings", Toast.LENGTH_LONG).show();
                try {
                    synchronized (this) {
                        this.wait(2000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                    }
                } catch (InterruptedException e1) {
                    Log.e(LOG_TAG, "Showing error was interrupted", e1);
                }
                finish();
            }
            if (client != null) {
                try {
                    client.connectBT(addresse, 1);
                    final LinearLayout l = (LinearLayout) findViewById(R.id.activity_connected);
                    l.setBackgroundColor(Color.LTGRAY);
                    final Connected host = this;
                    l.post(new Runnable() {  //the Fragments can only be created, after Layout height can be measured
                        @Override
                        public void run() {
                            int width = l.getWidth();
                            int height = (int) Math.round(width * 0.6);
                            Log.i(LOG_TAG, "using following Fragment hosting Properties:" +
                                    " width:" + width +
                                    " height: " + height);
                            DiagramViewSettings viewSettings = DiagramViewSettings.getDefaultSettings();
                            m_globalSettings = new DiagramSettings(viewSettings, null, null, height, width, Integer.MIN_VALUE, Integer.MAX_VALUE);
                            m_diagrams = new ArrayList<>(3);
                            m_diagrams.add(new DiagramSettings(
                                    m_globalSettings.getViewSettings(),
                                    FRAGMENT_TAG_TEMPERATURE,
                                    "°C",
                                    m_globalSettings.getHeight(),
                                    m_globalSettings.getWidth(),
                                    -25,
                                    100));
                            m_diagrams.add(new DiagramSettings(
                                    m_globalSettings.getViewSettings(),
                                    FRAGMENT_TAG_HUMIDITY,
                                    "%",
                                    m_globalSettings.getHeight(),
                                    m_globalSettings.getWidth(),
                                    0,
                                    100));
                            m_diagrams.add(new DiagramSettings(
                                    m_globalSettings.getViewSettings(),
                                    FRAGMENT_TAG_SOILMOISTURE,
                                    "%",
                                    m_globalSettings.getHeight(),
                                    m_globalSettings.getWidth(),
                                    0,
                                    100));
                            m_diagramManager = new DiagramManager(host, l, m_diagrams, host);
                            loadViewSettings();
                            m_diagramManager.showDiagram(FRAGMENT_TAG_TEMPERATURE);
                            Log.i(LOG_TAG, "Diagram Manager has been created");
                            readyToShow = 1;
                            client.setM_receiveListener(new ArduinoBluetoothClient.OnReceiveListener() {
                                @Override
                                public void onPreReceive() {

                                }

                                @Override
                                public void onPostReceive() {
                                    if (readyToShow == 2) {
                                        m_diagramManager.update();
                                    }
                                }
                            });
                        }
                    });
                    Toolbar usedToolbar = (Toolbar) findViewById(R.id.connected_toolbar);
                    setSupportActionBar(usedToolbar);

                } catch (BluetoothConnectionStateException e) {
                    Log.e(LOG_TAG, "connection Error", e);
                    Toast.makeText(getApplicationContext(), "could not connect Client", Toast.LENGTH_LONG);
                    try {
                        synchronized (this) {
                            this.wait(2000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                        }
                    } catch (InterruptedException e1) {
                        Log.e(LOG_TAG, "Showing error was interrupted", e1);
                    }
                    finish();
                }
            }

        } else {
            Toast.makeText(getApplicationContext(), "Could not read Target Address", Toast.LENGTH_SHORT).show();
            try {
                synchronized (this) {
                    this.wait(1000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                }
            } catch (InterruptedException e1) {
                Log.e(LOG_TAG, "Showing error was interrupted", e1);
            }
            finish();
        }
    }


    public void changeButtonClicked() {
        final String[] items = new String[m_diagrams.size()];
        int pos = 0;
        for (DiagramSettings settings :
                m_diagrams) {
            items[pos++] = settings.getName();
        }
        ListDialogFragment listDialog = ListDialogFragment.getInstance(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which] != null) {
                    switch (items[which]) {
                        case (FRAGMENT_TAG_SOILMOISTURE): {
                            m_diagramManager.showDiagram(FRAGMENT_TAG_SOILMOISTURE);
                            break;
                        }
                        case (FRAGMENT_TAG_HUMIDITY): {
                            m_diagramManager.showDiagram(FRAGMENT_TAG_HUMIDITY);
                            break;
                        }
                        default: {
                            m_diagramManager.showDiagram(FRAGMENT_TAG_TEMPERATURE);
                            break;
                        }
                    }
                } else {
                    m_diagramManager.showDiagram(FRAGMENT_TAG_TEMPERATURE);
                }
            }
        }, "Choose Diagram to be shown");
        listDialog.show(getSupportFragmentManager(), "List Dialog");
    }

    public void disconnectButtonClicked() {
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (readyToShow == 0) {
            readyToShow = 1;
        }
        try {
            m_bluetoothData = m_dataProvider.readData();
        } catch (UnrecognizableBluetoothDataException e) {
            Log.e(LOG_TAG, "Data could not be read", e);
        }
        m_temperatureValues = new ArrayList<>();
        m_humidityValues = new ArrayList<>();
        m_soilValues = new ArrayList<>();
        for (BluetoothDataSet data :
                m_bluetoothData) {
            m_temperatureValues.add(data.getTemperature().intValue());
            m_humidityValues.add(data.getHumidity().intValue());
            m_soilValues.add(data.getSoilMoisture().intValue());
        }
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
        inflater.inflate(R.menu.connected_actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.main_actionbar_menu_item_settings): {
                Intent startSettings = new Intent(this, SettingsActivity.class);
                startActivityForResult(startSettings, SettingsActivity.REQUESTCODE);
                return true;
            }
            case (R.id.connected_actionbar_menu_item_changeDiagram): {
                this.changeButtonClicked();
                return true;
            }
            case (R.id.connected_actionbar_menu_item_disconnect): {
                this.disconnectButtonClicked();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (readyToShow == 1) {
            readyToShow = 0;
        }
        m_dataProvider.writeData(m_bluetoothData);
        m_bluetoothData.clear();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingsActivity.REQUESTCODE) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.destroy();
        }
    }

    private void reloadSettings() {
        loadConnectionSettings();
        loadViewSettings();
    }

    private void loadConnectionSettings() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (client != null) {
            long timerRate;
            try {
                timerRate = Long.parseLong(preference.getString(SettingsActivity.KEY_CONNECTION_RECEIVERATE, SettingsActivity.PREF_DEFAULTVALUE_CONNECTION_RECEIVERATE));
                if (client.getTimerRate() != timerRate) {
                    client.setTimer(timerRate);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Could not resolve receive Rate. You may only enter Numbers in the Settings", Toast.LENGTH_LONG).show();
                try {
                    synchronized (this) {
                        this.wait(2000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                    }
                } catch (InterruptedException e1) {
                    Log.e(LOG_TAG, "Showing error was interrupted", e1);
                }
                finish();
            }
        }
    }

    private void loadViewSettings() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        DiagramViewSettings viewSettings = m_globalSettings.getViewSettings();
        int graphColor = Integer.parseInt(preference.getString(SettingsActivity.KEY_VIEW_GRAPHCOLOR, SettingsActivity.PREF_DEFAULTVALUE_VIEW_GRAPHCOLOR));
        boolean changed = false;
        if (graphColor != viewSettings.getGraphColor()) {
            viewSettings.setGraphColor(graphColor);
            changed = true;
        }
        if (changed) {
            m_globalSettings.setViewSettings(viewSettings);
            for (DiagramSettings settings :
                    m_diagrams) {
                settings.setViewSettings(viewSettings);
            }
            m_diagramManager.setDiagrams(m_diagrams);
            m_diagramManager.update();
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public ArrayList<Integer> onRefreshRequest(DiagramSettings fragmentDescription) {
        refreshData();
        LinkedList<BluetoothDataSet> temp = new LinkedList<>();
        Date current = Calendar.getInstance().getTime();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int maxBack = Integer.parseInt(pref.getString(SettingsActivity.KEY_DATA_SHOWVALUES, "10000"));
        if (maxBack > 0) {
            for (int i = (m_bluetoothData.size() - 1); (i >= 0); i--) {  //backward for loop...
                BluetoothDataSet data = m_bluetoothData.get(i);
                if ((data.getTimeStamp().getTime()) >= (current.getTime() - maxBack)) {  //he only does something if the current time is in the specified time Period
                    temp.addFirst(data);
                } else {
                    break;  //just to increase Performance
                }
            }
        } else {
            temp = (LinkedList<BluetoothDataSet>) m_bluetoothData;
        }
        prepareLists(temp.size());
        for (BluetoothDataSet data :  //adding everything to the Lists
                temp) {
            m_temperatureValues.add(data.getTemperature().intValue());
            m_humidityValues.add(data.getHumidity().intValue());
            m_soilValues.add(data.getSoilMoisture().intValue());
        }
        switch (fragmentDescription.getName()) {
            case (FRAGMENT_TAG_TEMPERATURE): {
                return copyValues(m_temperatureValues);
            }
            case (FRAGMENT_TAG_HUMIDITY): {
                return copyValues(m_humidityValues);
            }
            case (FRAGMENT_TAG_SOILMOISTURE): {
                return copyValues(m_soilValues);
            }
        }
        return null;
    }

    private void refreshData() {
        List<BluetoothDataSet> received = client.getReceivedData();
        if (received != null) {
            client.clearReceivedData();
            for (BluetoothDataSet data :
                    received) {
                m_bluetoothData.add(data);
                m_temperatureValues.add(data.getTemperature().intValue());
                m_humidityValues.add(data.getHumidity().intValue());
                m_soilValues.add(data.getSoilMoisture().intValue());
            }
        }
    }

    private ArrayList<Integer> copyValues(ArrayList<Integer> toCopyFrom) {
        ArrayList<Integer> copy = new ArrayList<>(toCopyFrom.size());
        for (Integer i :
                toCopyFrom) {
            copy.add(i);
        }
        return copy;
    }

    private void prepareLists(int size) {
        if (m_temperatureValues != null) {
            m_temperatureValues.clear();
        }
        if (m_humidityValues != null) {
            m_humidityValues.clear();
        }
        if (m_soilValues != null) {
            m_soilValues.clear();
        }
        m_temperatureValues = new ArrayList<>(size);
        m_humidityValues = new ArrayList<>(size);
        m_soilValues = new ArrayList<>(size);
    }
}
