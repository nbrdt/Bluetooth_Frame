package kevin.test.bluetooth.bluetooth_frame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramViewSettings;
import kevin.test.bluetooth.bluetooth_frame.Views.*;

//author: NB, KI

public class DiagramActivity extends AppCompatActivity implements ArduinoBluetoothClient.OnReceiveListener {
    private static final String DIAGRAM_NAME_TEMP = "Temperature";
    private static final String DIAGRAM_NAME_HUMID = "Humidity";
    private static final String DIAGRAM_NAME_SOIL = "Soil Moisture";
    ;

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
    DiagramViewSettings m_viewSettings;

    private List<BluetoothDataSet> m_bluetoothData = new LinkedList<>();
    private ArrayList<Integer> m_temperatureValues = new ArrayList<>();
    private ArrayList<Integer> m_humidityValues = new ArrayList<>();
    private ArrayList<Integer> m_soilValues = new ArrayList<>();

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

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        toolbar.setBackgroundColor(Color.DKGRAY);
        tabLayout.setBackgroundColor(Color.DKGRAY);
        mViewPager.setBackgroundColor(Color.LTGRAY);



        m_dataProvider = new BluetoothDataProvider(getApplicationContext());
        String addresse = getIntent().getExtras().getString("addresse");

        if (addresse != null && !addresse.isEmpty()) {
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(DiagramActivity.this);
            try {
                m_client = NFK_ArduinoBluetoothClient.getClient(Integer.parseInt(preference.getString(SettingsActivity.KEY_CONNECTION_RECEIVERATE, "1000")));
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
            reloadSettings();
            if (m_client != null) {
                try {
                    m_client.connectBT(addresse, 1);
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


    @Override
    public void onStart() {
        super.onStart();
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
                startActivityForResult(startSettings, SettingsActivity.REQUESTCODE);
                return true;
            }
            case (R.id.action_refresh): {
                Log.e(LOG_TAG, "Refreshing is not supported yet");
                return super.onOptionsItemSelected(item);
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
        if (requestCode == SettingsActivity.REQUESTCODE) {
            reloadSettings();
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
        DiagramActivity.DiagramFragment fragment = (DiagramActivity.DiagramFragment) getSupportFragmentManager().findFragmentById(R.id.diagramactivity_diagramcontainer);
        updateFragment(fragment);
    }

    private void updateFragment(DiagramActivity.DiagramFragment fragment) {
        processData();
        String title = mSectionsPagerAdapter.getPageTitle(fragment.getSectionNumber() - 1).toString();
        switch (title) {
            case (DIAGRAM_NAME_SOIL): {
                fragment.updateDiagram(m_soilValues);
                break;
            }
            case (DIAGRAM_NAME_HUMID): {
                fragment.updateDiagram(m_humidityValues);
                break;
            }
            default: {
                fragment.updateDiagram(m_temperatureValues);
                break;
            }
        }
    }

    private void processData() {
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
    }

    private void refreshData() {
        List<BluetoothDataSet> received = m_client.getReceivedData();
        if (received != null) {
            m_client.clearReceivedData();
            for (BluetoothDataSet data :
                    received) {
                m_bluetoothData.add(data);
                m_temperatureValues.add(data.getTemperature().intValue());
                m_humidityValues.add(data.getHumidity().intValue());
                m_soilValues.add(data.getSoilMoisture().intValue());
            }
        }
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

    private ArrayList<Integer> copyValues(ArrayList<Integer> toCopyFrom) {
        ArrayList<Integer> copy = new ArrayList<>(toCopyFrom.size());
        for (Integer i :
                toCopyFrom) {
            copy.add(i);
        }
        return copy;
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
        DiagramActivity.DiagramFragment fragment = (DiagramActivity.DiagramFragment) getSupportFragmentManager().findFragmentById(R.id.diagramactivity_diagramcontainer);
        if (m_viewSettings == null) {
            m_viewSettings = DiagramViewSettings.getDefaultSettings();
        }
        int graphColor = Integer.parseInt(preference.getString(SettingsActivity.KEY_VIEW_GRAPHCOLOR, SettingsActivity.PREF_DEFAULTVALUE_VIEW_GRAPHCOLOR));
        if (graphColor != m_viewSettings.getGraphColor()) {
            m_viewSettings.setGraphColor(graphColor);
            if (fragment != null) fragment.setViewSettings(m_viewSettings);
        }
    }




    /**-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     * A fragment containing the Diagram. When clicking on one tab, the right Diagram is put into the fragment
     */
    public static class DiagramFragment extends Fragment {

        private DiagramViewSettings viewSettings;
        private int sectionNumber;

        private DiagrammAllgemein shownDiagram;
        LinearLayout layout;

        private ArrayList<Integer> values;

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_VIEWSETTINGS = "section_number";

        public DiagramFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DiagramFragment newInstance(int sectionNumber, DiagramViewSettings viewSettings) {
            DiagramFragment fragment = new DiagramFragment();
            fragment.viewSettings = viewSettings; // Bundles don't work anymore (latest STABLE SDK update), they only save the last given value (every other is registered as null)
            fragment.sectionNumber = sectionNumber;
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.tab_layout, container, false);
            layout = (LinearLayout)rootView.findViewById(R.id.layout);
            Bundle args = getArguments();
            final int diagramToShow = sectionNumber - 1;

            //Is necessary to get the height and width of the layout
            layout.post(new Runnable() {
                @Override
                public void run() {

                    final float touchWidth = layout.getWidth();
                    final int layoutWidth = (int)touchWidth;

                    int width = layoutWidth;
                    int height = (int) Math.round(layoutWidth*0.6);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);

                    shownDiagram = null;
                    Bundle args = getArguments();

                    switch (diagramToShow) {
                        case 0: {
                            shownDiagram = new DiagrammAllgemein(getContext(), height, width, new ArrayList<Integer>(), -25, 100, "°C", viewSettings);
                            break;
                        }
                        case 1: {
                            shownDiagram = new DiagrammAllgemein(getContext(), height, width, new ArrayList<Integer>(), 0, 100, "%", viewSettings);
                            break;
                        }
                        case 2: {
                            shownDiagram = new DiagrammAllgemein(getContext(), height, width, new ArrayList<Integer>(), 20, 80, "%", viewSettings);
                            break;
                        }
                        default: {
                            shownDiagram = new DiagrammAllgemein(getContext(), height, width, new ArrayList<Integer>(), -25, 100, "°C", viewSettings);
                        }
                    }

                    shownDiagram.setDiagramFragment(DiagramFragment.this);
                    shownDiagram.setBackgroundColor(Color.WHITE);
                    shownDiagram.setLayoutParams(params);
                    layout.addView(shownDiagram);
                }
            });


            return rootView;


        }


        public void updateDiagram(ArrayList<Integer> newValues) {
            values = newValues;
            updateDiagram();
        }

        public void updateDiagram() {
            if (shownDiagram != null) {
                shownDiagram.updateList(values);
                Handler refresher = new Handler(Looper.getMainLooper());
                refresher.post(new Runnable() {
                    @Override
                    public void run() {
                        shownDiagram.invalidate();
                    }
                });
            }
        }

        public void setViewSettings(DiagramViewSettings settings) {
            this.viewSettings = settings;
            if (shownDiagram != null) {
                shownDiagram.setViewSettings(viewSettings);
                updateDiagram();
            }
        }

        public int getSectionNumber() {
            return sectionNumber;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final int PAGE_NUMBER = 3;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return DiagramFragment.newInstance(position + 1, m_viewSettings);
        }

        //Amount of tabs
        @Override
        public int getCount() {
            return PAGE_NUMBER;
        }

        //Defines the titles of the Tabs
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return DIAGRAM_NAME_TEMP;
                case 1:
                    return DIAGRAM_NAME_SOIL;
                case 2:
                    return DIAGRAM_NAME_HUMID;
            }
            return null;
        }

        public void resetViewSettings() {

        }
    }
}
