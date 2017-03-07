package nfk.bluetooth.arduino.wetterverarbeitung.Views;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import nfk.bluetooth.arduino.wetterverarbeitung.R;

/**
 * @author NB & KI
 * @version 1.3
 * A fragment containing the Diagram. When clicking on one tab, the correct Diagram is put into the fragment
 */
public class DiagramFragment extends Fragment {
    public static final String DIAGRAM_NAME_TEMP = "Temperature";
    public static final int DIAGRAM_SECTIONNUMBER_TEMP = 1;
    public static final String DIAGRAM_NAME_SOIL = "Soil Moisture";
    public static final int DIAGRAM_SECTIONNUMBER_SOIL = 2;
    public static final String DIAGRAM_NAME_RAIN = "Rain Strength";
    public static final int DIAGRAM_SECTIONNUMBER_RAIN = 3;
    private static final String LOG_TAG = "Diagram Fragment";

    private DiagramViewSettings viewSettings;
    private int sectionNumber;
    private RefreshListener refresher;

    private LineChart shownDiagram;
    LinearLayout layout;

    private LinkedList<Entry> values;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_VIEWSETTINGS = "view_settings";
    private TimeFormat timeFormat = TimeFormat.SECONDS;

    public enum TimeFormat {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS
    }

    public DiagramFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DiagramFragment newInstance(int sectionNumber, DiagramViewSettings viewSettings, RefreshListener listener) {
        DiagramFragment fragment = new DiagramFragment();
        if (listener != null) {
            fragment.refresher = listener;
        } else {
            fragment.refresher = (RefreshListener) fragment.getActivity();
        }
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putBundle(ARG_VIEWSETTINGS, viewSettings.createToBundle());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DiagramFragment newInstance(int sectionNumber, DiagramViewSettings viewSettings) {
        try {
            return newInstance(sectionNumber, viewSettings, null);
        } catch (ClassCastException e) {
            throw new ClassCastException("Host Activity must implement interface RefreshListener");
        }
    }

    public void setRefresher(RefreshListener listener) {
        refresher = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Bundle args = getArguments();
        sectionNumber = args.getInt(ARG_SECTION_NUMBER);
        viewSettings = DiagramViewSettings.createFromBundle(args.getBundle(ARG_VIEWSETTINGS));
        final View rootView;
        switch (getSectionNumber()) {
            case (DIAGRAM_SECTIONNUMBER_SOIL): {
                rootView = inflater.inflate(R.layout.humidity_tab_layout, container, false);
                layout = (LinearLayout) rootView.findViewById(R.id.humidity_layout);
                break;
            }
            case (DIAGRAM_SECTIONNUMBER_RAIN): {
                rootView = inflater.inflate(R.layout.soilmoisture_tab_layout, container, false);
                layout = (LinearLayout) rootView.findViewById(R.id.soilmoisture_layout);
                break;
            }
            case (DIAGRAM_SECTIONNUMBER_TEMP): {
                rootView = inflater.inflate(R.layout.temperature_tab_layout, container, false);
                layout = (LinearLayout) rootView.findViewById(R.id.temperature_layout);
                break;
            }
            default: {
                Log.w(LOG_TAG, "Could not identify Section number. Using Default Temperature Layout");
                rootView = inflater.inflate(R.layout.temperature_tab_layout, container, false);
                layout = (LinearLayout) rootView.findViewById(R.id.temperature_layout);
                break;
            }
        }

        //Is necessary to get the height and width of the layout
        layout.post(new Runnable() {
            @Override
            public void run() {

                final float touchWidth = layout.getWidth();
                final int layoutWidth = (int) touchWidth;

                int width = layoutWidth;
                int height = (int) Math.round(layoutWidth * 0.75);
                switch (getSectionNumber()) {
                    case (DIAGRAM_SECTIONNUMBER_SOIL): {
                        shownDiagram = (LineChart) rootView.findViewById(R.id.humidity_line_chart);
                        break;
                    }
                    case (DIAGRAM_SECTIONNUMBER_RAIN): {
                        shownDiagram = (LineChart) rootView.findViewById(R.id.soilmoisture_line_chart);
                        break;
                    }
                    case (DIAGRAM_SECTIONNUMBER_TEMP): {
                        shownDiagram = (LineChart) rootView.findViewById(R.id.temperature_line_chart);
                        break;
                    }
                    default: {
                        Log.w(LOG_TAG, "Could not identify Section number. Using Temperature Line chart.");
                        shownDiagram = (LineChart) rootView.findViewById(R.id.temperature_line_chart);
                        break;
                    }
                }
                shownDiagram.setBackgroundColor(Color.WHITE);
                shownDiagram.setDragDecelerationEnabled(false);
                shownDiagram.setLogEnabled(false);
                shownDiagram.getXAxis().setValueFormatter(new TimeValueFormatter());
                updateDiagram();
            }
        });


        return rootView;


    }


    public void resetValues(LinkedList<Entry> newValues) {
        values = newValues;
    }

    public void resetFormat(BigDecimal maxValue) {
        TimeValueFormatter formatter = new TimeValueFormatter();
        this.timeFormat = formatter.chooseFormat(maxValue.floatValue());
    }

    public void updateDiagram() {  //updates the Diagram view
        if (shownDiagram != null) {
            refresher.onRefreshRequest(this);
            shownDiagram.setData(createDataFromValues());
            //shownDiagram.notifyDataSetChanged();
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
            updateDiagram();
        }
    }

    public int getSectionNumber() {
        if (getArguments() != null) {
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);  //somehow the ebugger shows us, that the class variables showed in tabs are mixed sometimes...
            return sectionNumber;
        } else {
            Log.w(LOG_TAG, "Arguments could not be read, using class variable");
            return sectionNumber;
        }
    }

    public interface RefreshListener {
        public void onRefreshRequest(DiagramFragment requester);
    }

    private LineData createDataFromValues() {
        if (values.isEmpty()) {
            return null;
        } else {
            LineDataSet dataSet = new LineDataSet(copyValues(), getDataName());
            dataSet.setColor(viewSettings.getGraphColor());
            LineData data = new LineData(dataSet);
            data.setValueFormatter(new ValueFormatter());
            return data;
        }
    }

    private String getDataName() {
        switch (getSectionNumber()) {
            case DIAGRAM_SECTIONNUMBER_RAIN: {
                return DIAGRAM_NAME_RAIN + " in %";
            }
            case DIAGRAM_SECTIONNUMBER_SOIL: {
                return DIAGRAM_NAME_SOIL + " in %";
            }
            case DIAGRAM_SECTIONNUMBER_TEMP: {
                return DIAGRAM_NAME_TEMP + " in °C";
            }
            default: {
                Log.w(LOG_TAG, "Could not identify Section number. Using Temperature Data Name");
                return DIAGRAM_NAME_TEMP + " in °C";
            }
        }
    }

    private List<Entry> copyValues() {
        ArrayList<Entry> copy = new ArrayList<>(values.size());
        for (Entry e :
                values) {
            copy.add(e);
        }
        return copy;
    }

    public static int sectionNumberToPosition(int sectionNumber) {
        return sectionNumber - 1;
    }

    public static int positionToSectionNumber(int position) {
        return position + 1;
    }

    private class ValueFormatter implements IValueFormatter {
        ValueFormatter() {
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
            return formatter.format(value);
        }
    }

    private class TimeValueFormatter implements IAxisValueFormatter {
        private static final int SECONDS_TO_MINUTES = 60;
        private static final int SECONDS_TO_HOURS = 3600;
        private static final int SECONDS_TO_DAYS = 86400;
        private DecimalFormat formatter;

        TimeValueFormatter() {
            formatter = (DecimalFormat) DecimalFormat.getInstance();
        }

        TimeFormat chooseFormat(float valueInMs) {
            float value = valueInMs / 1000;  //formats into seconds
            if (value < SECONDS_TO_MINUTES * 2) {
                return TimeFormat.SECONDS;
            } else if (value < SECONDS_TO_HOURS * 2) {
                return TimeFormat.MINUTES;
            } else if (value < SECONDS_TO_DAYS * 2) {
                return TimeFormat.HOURS;
            } else {
                return TimeFormat.DAYS;
            }
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            axis.setAxisMinimum(0f);
            axis.setGridColor(viewSettings.getFrameColor());
            formatter.setMaximumFractionDigits(1);
            value = value / 1000; //formats into Seconds
            StringBuilder formatted = new StringBuilder();
            switch (timeFormat) {
                case SECONDS: {
                    formatted.append(formatter.format(value));
                    formatted.append("s");
                    break;
                }
                case MINUTES: {
                    formatted.append(formatter.format(value / SECONDS_TO_MINUTES));
                    formatted.append("min");
                    break;
                }
                case HOURS: {
                    formatted.append(formatter.format(value / SECONDS_TO_HOURS));
                    formatted.append("h");
                    break;
                }
                case DAYS: {
                    formatter.setMaximumFractionDigits(2);
                    formatted.append(formatter.format(value / SECONDS_TO_DAYS));
                    formatted.append("d");
                    break;
                }
                default: {  //the default formats Seconds
                    Log.w(LOG_TAG, "Unknown timeFormat is used to Format an Axis. Using Seconds");
                    formatted.append(formatter.format(value / 60));
                    formatted.append("s");
                }
            }
            return formatted.toString();
        }


    }
}