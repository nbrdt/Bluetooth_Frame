package kevin.test.bluetooth.bluetooth_frame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.ViewTreeObserver;
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
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramManager;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramSettings;
import kevin.test.bluetooth.bluetooth_frame.DiagramManaging.DiagramViewSettings;
import kevin.test.bluetooth.bluetooth_frame.Views.*;

//author: NB, KI

public class DiagramActivity extends AppCompatActivity implements DiagramManager.DataProvider {


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
    private ArduinoBluetoothClient client;

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
        mViewPager = (NonSwipableViewPager) findViewById(R.id.container);
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
                client = NFK_ArduinoBluetoothClient.getClient(Integer.parseInt(preference.getString(SettingsActivity.KEY_CONNECTION_RECEIVERATE, "1000")));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Could not resolve receive Rate. You may only enter Numbers in the Settings", Toast.LENGTH_LONG).show();
                try {
                    synchronized (this) {
                        this.wait(2000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                    }
                } catch (InterruptedException e1) {
                    Log.e("ERROR", "Showing error was interrupted", e1);
                }
                finish();
            }
            if (client != null) {
                try {
                    client.connectBT(addresse, 1);
                }
             catch (BluetoothConnectionStateException e) {
                Log.e("ERROR", "connection Error", e);
                Toast.makeText(getApplicationContext(), "could not connect Client", Toast.LENGTH_LONG);
                try {
                    synchronized (this) {
                        this.wait(2000);  //somehow it doesn't show the toast, if it doesn't get some time for it
                    }
                } catch (InterruptedException e1) {
                    Log.e("ERROR", "Showing error was interrupted", e1);
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
            Log.e("ERROR", "Showing error was interrupted", e1);
        }
        finish();
    }

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_diagram, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent startSettings = new Intent(this, SettingsActivity.class);
            startActivity(startSettings);
            return true;
        }
        if(id == R.id.action_refresh) {
            Log.e("Toolbar", "Refreshing");
        }
        if(id == R.id.action_disconnect) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (client != null) {
            client.destroy();
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
    public ArrayList<Integer> onRefreshRequest(DiagramSettings fragmentDescription) {
        return null;
    }

    /*
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
    }*/






    /**-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     * A fragment containing the Diagram. When clicking on one tab, the right Diagram is put into the fragment
     */
    public static class DiagramFragment extends Fragment {

        private DiagrammAllgemein shownDiagram;
        LinearLayout layout;

        private ArrayList<Integer> values;

        private static final String ARG_SECTION_NUMBER = "section_number";

        public DiagramFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DiagramFragment newInstance(int sectionNumber) {
            DiagramFragment fragment = new DiagramFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.tab_layout, container, false);
            layout = (LinearLayout)rootView.findViewById(R.id.layout);


            //Is nescssary to get the height and width of the layout
            layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    else {
                        layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }

                    final float touchWidth = layout.getWidth();
                    final int layoutWidth = (int)touchWidth;

                    int width = layoutWidth;
                    int height = (int) Math.round(layoutWidth*0.6);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);

                    shownDiagram = null;
                    if(1==1) {

                    }

                    DiagramViewSettings vs = DiagramViewSettings.getDefaultSettings();
                    switch (getArguments().getInt(ARG_SECTION_NUMBER)-1) {
                        case 0:
                            //settings = new DiagramSettings("Temperature", "°C", width, height, -25, 100);
                            //viewSettings = DiagramViewSettings.getDefaultSettings();
                            //settings.setViewSettings(viewSettings);
                            shownDiagram = new DiagrammAllgemein(getContext(), height, width, values, -25, 100, "°C", vs);
                            break;
                        case 1:
                            //settings = new DiagramSettings("Humidity", "%", width, height, 0, 100);
                            //viewSettings = DiagramViewSettings.getDefaultSettings();
                            //settings.setViewSettings(viewSettings);

                            shownDiagram = new DiagrammAllgemein(getContext(), height, width, values, 0, 100, "%", vs);
                            break;
                        case 2:
                            //settings = new DiagramSettings("Soil Moisture", "%", width, height, 0, 100);
                            //viewSettings = DiagramViewSettings.getDefaultSettings();
                            //settings.setViewSettings(viewSettings);
                            shownDiagram = new DiagrammAllgemein(getContext(), height, width, values, 0, 100, "%", vs);
                            break;
                    }
                    shownDiagram.setBackgroundColor(Color.WHITE);
                    shownDiagram.setLayoutParams(params);
                    layout.addView(shownDiagram);



                }
            });
            return rootView;


        }


        public void updateDiagram(ArrayList<Integer> newValues) {
            if(values.size() > 10) {
                for(int i = 0; i < newValues.size(); i++) {
                    values.remove(i);
                    values.add(newValues.get(i));
                }
            }
            shownDiagram.invalidate();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return DiagramFragment.newInstance(position + 1);
        }

        //Amount of tabs
        @Override
        public int getCount() {
            return 3;
        }

        //Defines the titles of the Tabs
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Temperature";
                case 1:
                    return "Humidity";
                case 2:
                    return "Soil Moisture";
            }
            return null;
        }
    }
}
