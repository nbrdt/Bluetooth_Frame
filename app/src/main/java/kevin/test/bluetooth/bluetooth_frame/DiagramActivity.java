package kevin.test.bluetooth.bluetooth_frame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothConnectionStateException;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothDataProvider;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothDataSet;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.NFK_ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.UnrecognizableBluetoothDataException;
import kevin.test.bluetooth.bluetooth_frame.Views.DiagramFragment;
import kevin.test.bluetooth.bluetooth_frame.Views.DiagramViewSettings;
import kevin.test.bluetooth.bluetooth_frame.Views.NonSwipableViewPager;

import static kevin.test.bluetooth.bluetooth_frame.Views.DiagramFragment.positionToSectionNumber;
import static kevin.test.bluetooth.bluetooth_frame.Views.DiagramFragment.sectionNumberToPosition;

/**
 * @author NB & KI
 * @version 1.4
 */

public class DiagramActivity extends AppCompatActivity implements ArduinoBluetoothClient.OnReceiveListener, DiagramFragment.RefreshListener, ViewPager.OnPageChangeListener, ActivityResults {

    private static final String LOG_TAG = "Diagram Activity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private BluetoothDataProvider m_dataProvider;
    private ArduinoBluetoothClient m_client;
    private DiagramViewSettings m_viewSettings;
    private int m_viewedPosition;

    private Vector<BluetoothDataSet> m_bluetoothData = new Vector<>();
    private LinkedList<Entry> m_temperatureValues = new LinkedList<>();
    private LinkedList<Entry> m_soilValues = new LinkedList<>();
    private LinkedList<Entry> m_rainValues = new LinkedList<>();

    private NonSwipableViewPager mViewPager;

    /**
     * The {@link ViewPager} that will host the section contents.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagram);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (NonSwipableViewPager) findViewById(R.id.diagramactivity_diagramcontainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setSwipeable(false);

        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        mViewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        toolbar.setBackgroundColor(Color.DKGRAY);
        tabLayout.setBackgroundColor(Color.DKGRAY);
        mViewPager.setBackgroundColor(Color.LTGRAY);
        m_viewedPosition = 0;

        m_dataProvider = new BluetoothDataProvider(getApplicationContext());
        String addresse = getIntent().getExtras().getString("addresse");

        if (addresse != null && !addresse.isEmpty()) {
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(DiagramActivity.this);
            try {
                m_client = NFK_ArduinoBluetoothClient.getClient(Integer.parseInt(preference.getString(SettingsActivity.KEY_CONNECTION_RECEIVERATE, "1000")), 2000);
            } catch (NumberFormatException e) {
                finishWithError("Could not resolve receive Rate. You may only enter Numbers in the Settings");
            }
            reloadSettings();
            if (m_client != null) {
                try {
                    m_client.connectBT(addresse, 1);
                } catch (BluetoothConnectionStateException e) {
                    finishWithError("Could not connect to Target Address");
                }
            }

        } else {
            finishWithError("Invalid Bluetooth-Address");
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        try {
            m_bluetoothData = (Vector<BluetoothDataSet>) m_dataProvider.readData();
        } catch (UnrecognizableBluetoothDataException e) {
            Log.e(LOG_TAG, "Data could not be read", e);
        }
        prepareLists(m_bluetoothData.size());
        m_client.setReceiveListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actionbar_menu, menu);
        inflater.inflate(R.menu.menu_diagram, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case (R.id.main_actionbar_menu_item_settings): {
                Intent startSettings = new Intent(this, SettingsActivity.class);
                startActivityForResult(startSettings, SETTINGSACTIVITY_REQUEST_CODE);
                return true;
            }
            case (R.id.action_refresh): {
                updateFragment();
                return true;
            }
            case (R.id.action_disconnect): {
                finish();
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
        m_dataProvider.writeData(m_bluetoothData);
        m_bluetoothData.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_client != null) {
            m_client.destroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGSACTIVITY_REQUEST_CODE) {
            if (resultCode != SettingsActivity.RESULT_ERROR) {
                reloadSettings();
            } else {
                showActivityError(data);
            }
        }
    }

    //Called when the back-button on the phone is pressed
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPreReceive() {

    }

    @Override
    public void onPostReceive() {
        updateFragment();
    }

    @Override
    public void onRefreshRequest(DiagramFragment requester) {  //This method provides the Fragment with Data as needed
        Log.i(LOG_TAG, "Refreshing Diagram");
        processData();
        int sectionNumber = requester.getSectionNumber();
        String title = mSectionsPagerAdapter.getPageTitle(sectionNumberToPosition(sectionNumber)).toString();
        switch (title) {
            case (DiagramFragment.DIAGRAM_NAME_RAIN): {
                Log.d(LOG_TAG, "Setting Rain Strength values on position: " + m_viewedPosition);
                requester.resetValues(m_rainValues);
                break;
            }
            case (DiagramFragment.DIAGRAM_NAME_SOIL): {
                Log.d(LOG_TAG, "Setting Soil Moisture values on position: " + m_viewedPosition);
                requester.resetValues(m_soilValues);
                break;
            }
            default: {
                Log.d(LOG_TAG, "Setting Temperature values on position: " + m_viewedPosition);
                requester.resetValues(m_temperatureValues);
                break;
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {  //This is called, whenever a new Page is shown-> Fragment needs to load data
        m_viewedPosition = position;
        DiagramFragment fragment = mSectionsPagerAdapter.getSavedFragmentFromPosition(m_viewedPosition);
        if (fragment != null && sectionNumberToPosition(fragment.getSectionNumber()) != position) {
            Log.w(LOG_TAG, "Fragment and Position don't match");  //just for debugging purposes
        }
        updateFragment();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
        Toast.makeText(this, errorMessage.getStringExtra(RESULTKEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
    }

    private void updateFragment() {
        final DiagramFragment fragment = mSectionsPagerAdapter.getSavedFragmentFromPosition(m_viewedPosition);
        if (fragment != null) {
            /*Handler refresher = new Handler(Looper.getMainLooper());  //makes this Thread safe, by posting it on the main Thread
            refresher.post(new Runnable() {
                @Override
                public void run() {
                }
            });*/
            fragment.updateDiagram();
        } else {
            Log.w(LOG_TAG, "Could not update Diagram Fragment, because Fragment is not available");
        }
    }

    private void processData() {
        refreshData();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int maxBack = Integer.parseInt(pref.getString(SettingsActivity.KEY_DATA_SHOWVALUES, "10000"));
        if (maxBack > 0) {
            Date current = Calendar.getInstance().getTime();
            LinkedList<BluetoothDataSet> temp = new LinkedList<>();
            for (int i = (m_bluetoothData.size() - 1); (i >= 0); i--) {  //backward for loop...
                BluetoothDataSet data = m_bluetoothData.get(i);
                if ((data.getTimeStamp().getTime()) >= (current.getTime() - maxBack)) {  //he only does something if the current time is in the specified time Period
                    temp.addFirst(data);
                } else {
                    break;  //just to increase Performance
                }
            }
            addEntries(temp);
        } else {
            addEntries(m_bluetoothData);
        }
    }

    private void addEntries(@NonNull List<BluetoothDataSet> dataSets) {
        prepareLists(dataSets.size());
        if (!dataSets.isEmpty()) {
            BigDecimal xEntryMin = new BigDecimal(dataSets.get(0).getTimeStamp().getTime()); //gets The Minimum XValue
            BigDecimal xEntryMax = new BigDecimal(dataSets.get(dataSets.size() - 1).getTimeStamp().getTime());  //gets The Maximum XValue
            BigDecimal xEntryRange = xEntryMax.subtract(xEntryMin);  //gets The Range in between which is needed to invert The Axis
            DiagramFragment fragment = mSectionsPagerAdapter.getSavedFragmentFromPosition(m_viewedPosition);
            if (fragment != null) {
                fragment.resetFormat(xEntryRange);  //xEntryRange supplies the MaxValue...
            }
            for (BluetoothDataSet data :  //adding everything to the Lists
                    dataSets) {
                BigDecimal xValue = (new BigDecimal(data.getTimeStamp().getTime())).subtract(xEntryMin);
                xValue = xValue.multiply(new BigDecimal(-1)); //versetzt die Werte unter das minimum
                xValue = xValue.add(xEntryRange);   //versetzt sie wieder um den maximal Abstand nach oben
                m_temperatureValues.addFirst(new Entry(xValue.floatValue(), data.getTemperature().floatValue()));
                m_soilValues.addFirst(new Entry(xValue.floatValue(), data.getRainStrength().floatValue()));
                m_rainValues.addFirst(new Entry(xValue.floatValue(), data.getSoilMoisture().floatValue()));
            }
        }
    }

    private void refreshData() {
        List<BluetoothDataSet> received = m_client.getReceivedData();
        if (received != null) {
            m_client.clearReceivedData();
            for (BluetoothDataSet data :
                    received) {
                m_bluetoothData.add(data);
            }
        }
    }

    private void prepareLists(int size) {
        if (m_temperatureValues != null) {
            m_temperatureValues.clear();
        }
        if (m_soilValues != null) {
            m_soilValues.clear();
        }
        if (m_rainValues != null) {
            m_rainValues.clear();
        }
        m_temperatureValues = new LinkedList<>();
        m_soilValues = new LinkedList<>();
        m_rainValues = new LinkedList<>();
    }

    private void reloadSettings() {
        loadConnectionSettings();
        loadViewSettings();
    }

    private void loadConnectionSettings() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (m_client != null) {
            long timerRate;
            try {
                timerRate = Long.parseLong(preference.getString(SettingsActivity.KEY_CONNECTION_RECEIVERATE, SettingsActivity.PREF_DEFAULTVALUE_CONNECTION_RECEIVERATE));
                if (m_client.getTimerRate() != timerRate) {
                    m_client.setTimer(timerRate);
                }
            } catch (NumberFormatException e) {
                finishWithError("Could not resolve receive Rate. Enter only Numbers in the Settings");
            }
        }
    }

    private void loadViewSettings() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        DiagramFragment fragment = (DiagramFragment) getSupportFragmentManager().findFragmentById(R.id.diagramactivity_diagramcontainer);
        if (m_viewSettings == null) {
            m_viewSettings = DiagramViewSettings.getDefaultSettings();
        }
        int graphColor;
        try {
            graphColor = Integer.parseInt(preference.getString(SettingsActivity.KEY_VIEW_GRAPHCOLOR, SettingsActivity.PREF_DEFAULTVALUE_VIEW_GRAPHCOLOR));
        } catch (NumberFormatException e) {
            graphColor = Integer.parseInt(SettingsActivity.PREF_DEFAULTVALUE_VIEW_GRAPHCOLOR);
            finishWithError("Could not Read Graph Settings. Try resetting Graph Settings or contact Developers.");
        }
        if (graphColor != m_viewSettings.getGraphColor()) {
            m_viewSettings.setGraphColor(graphColor);
            if (fragment != null) fragment.setViewSettings(m_viewSettings);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final int PAGE_NUMBER = 3;
        private ArrayList<DiagramFragment> fragments = new ArrayList<>(PAGE_NUMBER);

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            DiagramFragment fragment = DiagramFragment.newInstance(positionToSectionNumber(position), m_viewSettings);
            fragment.setRefresher(DiagramActivity.this);
            if (position < fragments.size() &&
                    PAGE_NUMBER <= fragments.size()) {
                if (fragments.get(position) != null) {
                    fragments.set(position, fragment);
                } else {
                    fragments.add(position, fragment);
                }
            } else {
                fragments.add(position, fragment);
            }
            return fragment;
        }

        //Amount of tabs
        @Override
        public int getCount() {
            return PAGE_NUMBER;
        }

        //Defines the titles of the Tabs
        @Override
        public CharSequence getPageTitle(int position) {
            int sectionNumber = positionToSectionNumber(position);
            switch (sectionNumber) {
                case DiagramFragment.DIAGRAM_SECTIONNUMBER_TEMP:
                    return DiagramFragment.DIAGRAM_NAME_TEMP;
                case DiagramFragment.DIAGRAM_SECTIONNUMBER_RAIN:
                    return DiagramFragment.DIAGRAM_NAME_RAIN;
                case DiagramFragment.DIAGRAM_SECTIONNUMBER_SOIL:
                    return DiagramFragment.DIAGRAM_NAME_SOIL;
            }
            return null;
        }

        /**
         * Returns the fragment used at the given position.
         * Will return null if the position does not poin to an given Fragment
         *
         * @param position the position (Tab) from which the Fragment should be retrieved
         * @return The Fragment at the given position. Null, if there is no Fragment at the given position.
         */
        public
        @Nullable
        DiagramFragment getSavedFragmentFromPosition(int position) {
            if (position >= 0 && position < fragments.size()) {
                return fragments.get(position);
            } else {
                return null;
            }
        }
    }
}
