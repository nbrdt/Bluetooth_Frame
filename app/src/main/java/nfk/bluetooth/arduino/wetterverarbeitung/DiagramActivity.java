package nfk.bluetooth.arduino.wetterverarbeitung;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.ArduinoBluetoothClient;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.BluetoothConnectionStateException;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.BluetoothDataSet;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.NFK_ArduinoBluetoothClient;
import nfk.bluetooth.arduino.wetterverarbeitung.Views.DiagramFragment;
import nfk.bluetooth.arduino.wetterverarbeitung.Views.DiagramViewSettings;
import nfk.bluetooth.arduino.wetterverarbeitung.Views.NonSwipableViewPager;

import static nfk.bluetooth.arduino.wetterverarbeitung.Views.DiagramFragment.positionToSectionNumber;

/**
 * @author NB
 * @version 1.4
 */

public class DiagramActivity extends AppCompatActivity implements DiagramHandler.DiagramCallbacks, ViewPager.OnPageChangeListener, ActivityResults {

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
    private ArduinoBluetoothClient m_client;
    private DiagramViewSettings m_viewSettings;
    private int m_maxBack;
    private int m_viewedPosition;
    private DiagramHandler m_handler;
    private int m_messageBufferSize;



    private NonSwipableViewPager mViewPager;

    /**
     * The ViewPager that will host the section contents.
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

        String addresse = getIntent().getExtras().getString("addresse");

        if (addresse != null && !addresse.isEmpty()) {
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(DiagramActivity.this);
            int receiveRate = 1000;
            try {
                receiveRate = Integer.parseInt(preference.getString(SettingsActivity.KEY_CONNECTION_RECEIVERATE, SettingsActivity.PREF_DEFAULTVALUE_CONNECTION_RECEIVERATE));
            } catch (NumberFormatException e) {
                finishWithError("Could not resolve receive Rate. You may only enter Numbers in the Settings");
            }
            m_client = NFK_ArduinoBluetoothClient.getClient(receiveRate, 3000);
            m_client.setLogEnabled(false);
            reloadSettings();
            m_client.setMessageBufferSize(m_messageBufferSize);
            HandlerThread handlerThread = new HandlerThread("Diagram Refresher");
            handlerThread.start();
            m_handler = new DiagramHandler(handlerThread, this, m_maxBack);
            m_handler.setLogEnabled(false);
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
        m_client.setReceiveListener(m_handler);
        m_handler.notifyOnStart();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
        m_handler.notifyOnStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(SettingsActivity.KEY_CONNECTION_BUFFERSIZE, "" + m_client.getMessageBufferSize());
        editor.apply();
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
    public int getCurrentFragmentPosition() {
        return m_viewedPosition;
    }

    @Override
    public List<BluetoothDataSet> getReceivedData() {
        List<BluetoothDataSet> received = m_client.getReceivedData();
        m_client.clearReceivedData();
        return received;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {  //This is called, whenever a new Page is shown-> Fragment needs to load data
        m_viewedPosition = position;
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

    public SectionsPagerAdapter getSectionsPagerAdapter() {
        return mSectionsPagerAdapter;
    }

    private void updateFragment() {
        Message msg = m_handler.obtainMessage(DiagramHandler.MESSAGE_UPDATE_FRAGMENT);
        m_handler.sendMessageAtFrontOfQueue(msg);  //it's high Priority to update the Fragment
    }

    private void reloadSettings() {
        loadConnectionSettings();
        loadDataSettings();
        loadViewSettings();
        if (m_handler != null) m_handler.notifySettingsChanged(m_maxBack);
    }

    private void loadConnectionSettings() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        m_messageBufferSize = Integer.parseInt(preference.getString(SettingsActivity.KEY_CONNECTION_BUFFERSIZE, SettingsActivity.PREF_DEFAULTVALUE_CONNECTION_BUFFERSIZE));
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

    private void loadDataSettings() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            m_maxBack = Integer.parseInt(preference.getString(SettingsActivity.KEY_DATA_SHOWVALUES, SettingsActivity.PREF_DEFAULTVALUE_DATA_SHOWVALUES));
        } catch (NumberFormatException e) {
            m_maxBack = Integer.parseInt(SettingsActivity.PREF_DEFAULTVALUE_DATA_SHOWVALUES);
            finishWithError("Could not Read Data Settings. Try resetting Data Settings or contact Developers.");
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
    class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final int PAGE_NUMBER = 4;
        private ArrayList<DiagramFragment> fragments = new ArrayList<>(PAGE_NUMBER);

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            DiagramFragment fragment = DiagramFragment.newInstance(positionToSectionNumber(position), m_viewSettings);
            fragment.setRefresher(m_handler);
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
                case DiagramFragment.DIAGRAM_SECTIONNUMBER_LIGHT:
                    return DiagramFragment.DIAGRAM_NAME_LIGHT;
                case DiagramFragment.DIAGRAM_SECTIONNUMBER_SOIL:
                    return DiagramFragment.DIAGRAM_NAME_SOIL;
                default:
                    Log.w(LOG_TAG, "Could not identify Page title");
                    return DiagramFragment.DIAGRAM_NAME_TEMP;
            }
        }

        /**
         * Returns the fragment used at the given position.
         * Will return null if the position does not poin to an given Fragment
         *
         * @param position the position (Tab) from which the Fragment should be retrieved
         * @return The Fragment at the given position. Null, if there is no Fragment at the given position.
         */
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
