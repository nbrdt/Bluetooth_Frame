package nfk.bluetooth.arduino.wetterverarbeitung.Views;

import android.content.Context;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import nfk.bluetooth.arduino.wetterverarbeitung.R;

/**
 * @author NB
 * @version 1.4
 * A fragment containing the Diagram. When clicking on one tab, the correct Diagram is put into the fragment
 */
public class DiagramFragment extends Fragment {
    public static final String DIAGRAM_NAME_TEMP = "Temperature";
    public static final int DIAGRAM_SECTIONNUMBER_TEMP = 1;
    public static final String DIAGRAM_NAME_SOIL = "Soil Moisture";
    public static final int DIAGRAM_SECTIONNUMBER_SOIL = 2;
    public static final String DIAGRAM_NAME_RAIN = "Rain Strength";
    public static final int DIAGRAM_SECTIONNUMBER_RAIN = 3;
    public static final String DIAGRAM_NAME_LIGHT = "Brightness";
    public static final int DIAGRAM_SECTIONNUMBER_LIGHT = 4;
    private static final String LOG_TAG = "Diagram Fragment";

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_VIEWSETTINGS = "view_settings";
    //attributes
    private int sectionNumber;
    private DiagramViewSettings viewSettings;
    private RefreshListener refresher;
    private ArrayList<Entry> values;
    private LinkedList<LimitLine> limitLines;
    private TimeValueFormatter xValueFormatter;
    private TimeFormat timeFormat = TimeFormat.SECONDS;
    //views
    private LineChart shownDiagram;
    LinearLayout layout;

    private enum TimeFormat {
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


    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        xValueFormatter = new TimeValueFormatter();
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (limitLines != null) {
            limitLines.clear();
        }
        limitLines = new LinkedList<>();
        Bundle args = getArguments();
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
            case (DIAGRAM_SECTIONNUMBER_LIGHT): {
                rootView = inflater.inflate(R.layout.brightness_tab_layout, container, false);
                layout = (LinearLayout) rootView.findViewById(R.id.brightness_layout);
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
                        LimitLine feucht = new LimitLine(45);
                        feucht.setLabel("Kaum Regen");
                        feucht.setLineColor(Color.CYAN);
                        LimitLine nass = new LimitLine(65);
                        nass.setLabel("Stark Regen");
                        nass.setLineColor(Color.BLUE);
                        limitLines.add(feucht);
                        limitLines.add(nass);
                        break;
                    }
                    case (DIAGRAM_SECTIONNUMBER_LIGHT): {
                        shownDiagram = (LineChart) rootView.findViewById(R.id.brightness_line_chart);
                        LimitLine wachstumsgrenze = new LimitLine(400);  //vgl. http://www.green24.de/Gartenbedarf-Technik/Anzucht-Vermehrung/Messgeraete/LuxMesser-LuxMessgeraet-fuer-Pflanzen.html
                        wachstumsgrenze.setLabel("Vollschatten Pflanzen");
                        wachstumsgrenze.setLineColor(Color.DKGRAY);
                        LimitLine halbschattenGrenze = new LimitLine(600);
                        halbschattenGrenze.setLabel("Schatten bis Halb Schatten");
                        halbschattenGrenze.setLineColor(Color.GRAY);
                        LimitLine normaleAnforderungen = new LimitLine(800);
                        normaleAnforderungen.setLabel("Halbschatten bis sonnig");
                        normaleAnforderungen.setLineColor(Color.LTGRAY);
                        LimitLine sonnigerStandort = new LimitLine(1000);
                        sonnigerStandort.setLabel("Vollsonniger Standort");
                        sonnigerStandort.setLineColor(Color.YELLOW);
                        limitLines.add(wachstumsgrenze);
                        limitLines.add(halbschattenGrenze);
                        limitLines.add(normaleAnforderungen);
                        limitLines.add(sonnigerStandort);
                        break;
                    }
                    case (DIAGRAM_SECTIONNUMBER_TEMP): {
                        shownDiagram = (LineChart) rootView.findViewById(R.id.temperature_line_chart);
                        LimitLine wachstumsgrenze = new LimitLine(5); //vgl. http://www.ecotronics.ch/blumen/keimverhalten.aspx?AspxAutoDetectCookieSupport=1#Suche
                        wachstumsgrenze.setLabel("Kaum eine Pflanze keimt bei dieser Temperatur");
                        wachstumsgrenze.setLineColor(Color.BLUE);
                        LimitLine untererKeimbereich = new LimitLine(12);
                        untererKeimbereich.setLabel("Kaltkeimer");
                        untererKeimbereich.setLineColor(Color.CYAN);
                        LimitLine gemaeßigterKeimbereich = new LimitLine(20);
                        gemaeßigterKeimbereich.setLabel("Keimbereich für Pflanzen aus gemäßigten Breiten");
                        gemaeßigterKeimbereich.setLineColor(Color.YELLOW);
                        LimitLine warmKeimer = new LimitLine(25);
                        warmKeimer.setLabel("Kaum Pflanzen brauchen oder wollen derartig hohe Keimtemperaturen");
                        warmKeimer.setLineColor(Color.RED);
                        limitLines.add(wachstumsgrenze);
                        limitLines.add(untererKeimbereich);
                        limitLines.add(gemaeßigterKeimbereich);
                        limitLines.add(warmKeimer);
                        break;
                    }
                    default: {
                        Log.w(LOG_TAG, "Could not identify Section number. Using Temperature Line chart.");
                        shownDiagram = (LineChart) rootView.findViewById(R.id.temperature_line_chart);  //this one might return null
                        LimitLine wachstumsgrenze = new LimitLine(5);
                        wachstumsgrenze.setLabel("Kaum eine Pflanze keimt bei dieser Temperatur");
                        wachstumsgrenze.setLineColor(Color.BLUE);
                        LimitLine untererKeimbereich = new LimitLine(12);
                        untererKeimbereich.setLabel("Kaltkeimer");
                        untererKeimbereich.setLineColor(Color.CYAN);
                        LimitLine gemaeßigterKeimbereich = new LimitLine(20);
                        gemaeßigterKeimbereich.setLabel("Keimbereich für Pflanzen aus gemäßigten Breiten");
                        gemaeßigterKeimbereich.setLineColor(Color.YELLOW);
                        LimitLine warmKeimer = new LimitLine(25);
                        warmKeimer.setLabel("Kaum Pflanzen brauchen oder wollen derartig hohe Keimtemperaturen");
                        warmKeimer.setLineColor(Color.RED);
                        limitLines.add(wachstumsgrenze);
                        limitLines.add(untererKeimbereich);
                        limitLines.add(gemaeßigterKeimbereich);
                        limitLines.add(warmKeimer);
                        break;
                    }
                }
                if (shownDiagram != null) {  //so we have to look for null values
                    Description description = new Description();
                    description.setEnabled(false);
                    shownDiagram.setDescription(description);
                    shownDiagram.setBackgroundColor(Color.WHITE);
                    shownDiagram.setDragDecelerationEnabled(false);
                    shownDiagram.setLogEnabled(false);
                    shownDiagram.getXAxis().setValueFormatter(xValueFormatter);
                    shownDiagram.setNoDataText("No Received Data available yet");
                    setLimitLines();
                }
                updateDiagram();
            }
        });


        return rootView;


    }


    public void resetValues(ArrayList<Entry> newValues) {
        values = newValues;
    }

    public void resetFormat(BigDecimal maxValue) {
        this.timeFormat = xValueFormatter.chooseFormat(maxValue.floatValue());
    }

    public void updateDiagram() {  //updates the Diagram view
        if (shownDiagram != null) {
            refresher.onRefreshRequest(this);
            shownDiagram.setData(createDataFromValues());
            Handler refresher = new Handler(Looper.getMainLooper());
            refresher.post(new Runnable() {
                @Override
                public void run() {
                    shownDiagram.notifyDataSetChanged();
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
            LineDataSet dataSet = new LineDataSet(values, getDataName());
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
            case DIAGRAM_SECTIONNUMBER_LIGHT: {
                return DIAGRAM_NAME_LIGHT + " in LUX";
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

    private void setLimitLines() {
        YAxis axis = shownDiagram.getAxisLeft();
        for (LimitLine l :
                limitLines) {
            l.setTextSize(9);
            axis.addLimitLine(l);
        }
    }

    public static int sectionNumberToPosition(int sectionNumber) {
        return sectionNumber - 1;
    }

    public static int positionToSectionNumber(int position) {
        return position + 1;
    }

    /**
     * Formats YAxis in another manner than the predefined way...
     *
     * @author KI&FW&NB
     */
    private class ValueFormatter implements IValueFormatter {
        ValueFormatter() {
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
            return formatter.format(value);
        }
    }

    /** Formats XAxis values so that it displays the chosen TimeFormat
     *  @author KI
     */
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