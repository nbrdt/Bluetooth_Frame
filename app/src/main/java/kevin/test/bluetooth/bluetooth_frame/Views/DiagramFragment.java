package kevin.test.bluetooth.bluetooth_frame.Views;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import kevin.test.bluetooth.bluetooth_frame.R;

/**
 * @author NB & KI
 * @version 1.3
 * A fragment containing the Diagram. When clicking on one tab, the correct Diagram is put into the fragment
 */
public class DiagramFragment extends Fragment {

    private DiagramViewSettings viewSettings;
    private int sectionNumber;
    private RefreshListener refresher;

    private DiagrammAllgemein shownDiagram;
    LinearLayout layout;

    private ArrayList<Integer> values;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_VIEWSETTINGS = "view_settings";

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

        final View rootView = inflater.inflate(R.layout.tab_layout, container, false);
        layout = (LinearLayout) rootView.findViewById(R.id.layout);
        Bundle args = getArguments();
        sectionNumber = args.getInt(ARG_SECTION_NUMBER);
        viewSettings = DiagramViewSettings.createFromBundle(args.getBundle(ARG_VIEWSETTINGS));
        final int diagramToShow = sectionNumber - 1;

        //Is necessary to get the height and width of the layout
        layout.post(new Runnable() {
            @Override
            public void run() {

                final float touchWidth = layout.getWidth();
                final int layoutWidth = (int) touchWidth;

                int width = layoutWidth;
                int height = (int) Math.round(layoutWidth * 0.6);

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


    public void resetValues(ArrayList<Integer> newValues) {
        values = newValues;
    }

    public void updateDiagram() {
        if (shownDiagram != null) {
            refresher.onRefreshRequest(this);
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

    public interface RefreshListener {
        public void onRefreshRequest(DiagramFragment requester);
    }
}