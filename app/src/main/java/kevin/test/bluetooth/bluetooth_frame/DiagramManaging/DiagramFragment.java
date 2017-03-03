package kevin.test.bluetooth.bluetooth_frame.DiagramManaging;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kevin.test.bluetooth.bluetooth_frame.R;


/**
 * @author KI
 * @version 1.0b
 */
public class DiagramFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String INSTANCESTATE_DIAGRAM_NAME = "Diagram m_name";
    private static final String INSTANCESTATE_DIAGRAM_HEIGHT = "Diagram m_height";
    private static final String INSTANCESTATE_DIAGRAM_WIDTH = "Diagram m_width";
    private static final String INSTANCESTATE_DIAGRAM_VALUES = "Diagram m_values";
    private static final String INSTANCESTATE_DIAGRAM_MINVALUE = "Diagram minimum value";
    private static final String INSTANCESTATE_DIAGRAM_MAXVALUE = "Diagram maximum value";
    private static final String INSTANCESTATE_DIAGRAM_UNIT = "Diagram m_unit";
    private static final String INSTANCESTATE_DIAGRAMVIEWSETTINGS = "DIagram viewSettings";
    //Default m_values
    private static final int DEFAULT_COLOR_NAMETEXT = Color.BLACK;
    private static final int DEFAULT_COLOR_DIAGRAM_BACKGROUND = Color.WHITE;


    private TextView m_nameView;
    private LinearLayout m_rootView;
    private Diagram m_diagram;
    private RelativeLayout m_extraContainer;
    private RefreshListener m_refreshListener;
    private DiagramFragment thisFragment = this;

    private Context m_attachContext;
    private ArrayList<Integer> m_values;
    private DiagramSettings m_settings;


    public DiagramFragment() {
        //Android developer guides say you should leave an empty constructor and set everything necessary in an static factory
    }

    /**
     *
     * @param m_name the Name to be shown
     * @param m_diagram the Diagram to show information
     * @return a newly created DiagramFragment
     */
    /**
     * public static DiagramFragment newInstance(String m_name, Diagram m_diagram) {
     * DiagramFragment fragment = new DiagramFragment();
     * Bundle args = new Bundle();
     * args.putString(INSTANCESTATE_DIAGRAM_NAME, m_name);
     * fragment.setArguments(args);
     * fragment.setDiagram(m_diagram);
     * return fragment;
     * }
     **/


    public static DiagramFragment newInstance(String name, int height, int width, ArrayList<Integer> values, int min, int max, String unit, DiagramViewSettings viewSettings) {
        DiagramFragment fragment = new DiagramFragment();
        Bundle args = new Bundle();
        args.putString(INSTANCESTATE_DIAGRAM_NAME, name);
        args.putInt(INSTANCESTATE_DIAGRAM_HEIGHT, height);
        args.putInt(INSTANCESTATE_DIAGRAM_WIDTH, width);
        args.putIntegerArrayList(INSTANCESTATE_DIAGRAM_VALUES, values);
        args.putInt(INSTANCESTATE_DIAGRAM_MINVALUE, min);
        args.putInt(INSTANCESTATE_DIAGRAM_MAXVALUE, max);
        args.putString(INSTANCESTATE_DIAGRAM_UNIT, unit);
        args.putBundle(INSTANCESTATE_DIAGRAMVIEWSETTINGS, viewSettings.createToBundle());
        fragment.setArguments(args);
        fragment.setDiagram(null);

        return fragment;
    }

    public static DiagramFragment newInstance(String name, int height, int width, ArrayList<Integer> values, int min, int max, String unit) {
        return newInstance(name, height, width, values, min, max, unit, DiagramViewSettings.getDefaultSettings());
    }

    public static DiagramFragment newInstance(DiagramSettings settings, ArrayList<Integer> values) {
        return newInstance(settings.getName(), settings.getHeight(), settings.getWidth(), values, settings.getMin(), settings.getMax(), settings.getUnit(), settings.getViewSettings());
    }

    private void setDiagram(Diagram diagram) {
        this.m_diagram = diagram;
    }

    public void setRefreshListener(RefreshListener refreshListener) {
        m_refreshListener = refreshListener;
    }

    public DiagramSettings getSettings() {
        return m_settings;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            String name = args.getString(INSTANCESTATE_DIAGRAM_NAME);
            int height = args.getInt(INSTANCESTATE_DIAGRAM_HEIGHT);
            int width = args.getInt(INSTANCESTATE_DIAGRAM_WIDTH);
            int min = args.getInt(INSTANCESTATE_DIAGRAM_MINVALUE);
            int max = args.getInt(INSTANCESTATE_DIAGRAM_MAXVALUE);
            m_values = args.getIntegerArrayList(INSTANCESTATE_DIAGRAM_VALUES);

            String unit = args.getString(INSTANCESTATE_DIAGRAM_UNIT);
            DiagramViewSettings viewSettings = DiagramViewSettings.createFromBundle(args.getBundle(INSTANCESTATE_DIAGRAMVIEWSETTINGS));
            m_settings = new DiagramSettings(viewSettings, name, unit, height, width, min, max);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            m_rootView = new LinearLayout(container.getContext()); //TODO save Container Context, so that it can be reused
        } else {
            Log.w("Diagram Fragment", "Had to use context known from previous attach, context might not be the same");
            m_rootView = new LinearLayout(m_attachContext);
        }
        m_rootView.setOrientation(LinearLayout.VERTICAL);
        m_rootView.setId(View.NO_ID);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        m_rootView.setLayoutParams(params);
        return m_rootView;
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * Activity.onCreateOptionsMenu
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.diagramfragment_actionbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        m_attachContext = context;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.diagramfragment_actionbar_menu_item_refresh): {
                if (m_refreshListener != null) {
                    m_refreshListener.onRefreshRequest(thisFragment);
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) { //Activity view has been created, m_diagram may be added
        super.onActivityCreated(savedInstanceState);
        createLayout();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        m_attachContext = null;
        setDiagram(null);
        m_settings.setViewSettings(null);
        m_settings = null;
        m_nameView = null;
        m_rootView = null;
        m_values.clear();
        m_values = null;
    }

    public void updateDiagram(ArrayList<Integer> newValues) {
        m_values = newValues;
        updateDiagram();
    }

    public void updateDiagram() {
        this.m_rootView.removeView(m_diagram);
        drawDiagram();
    }

    private void removeFromExtraContainer() {
        m_extraContainer.removeView(m_nameView);
    }

    private void createLayout() {
        drawExtraContainer();
        drawTextName();
        drawDiagram();
    }

    private void drawExtraContainer() {
        this.m_extraContainer = new RelativeLayout(m_rootView.getContext());
        m_extraContainer.setId(View.NO_ID);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        m_extraContainer.setLayoutParams(params);
        m_extraContainer.setId(View.NO_ID);
        this.m_rootView.addView(m_extraContainer);
    }

    private void drawDiagram() {
        setDiagram(new Diagram(m_rootView.getContext(), m_values, m_settings));
        m_diagram.setBackgroundColor(DEFAULT_COLOR_DIAGRAM_BACKGROUND);
        m_diagram.setLayoutParams(new LinearLayout.LayoutParams(m_settings.getWidth(), m_settings.getHeight()));
        m_diagram.setSettings(m_settings);
        m_diagram.setId(View.NO_ID);
        this.m_rootView.addView(m_diagram);
    }

    private void drawTextName() {
        this.m_nameView = new TextView(m_extraContainer.getContext());
        m_nameView.setText(m_settings.getName());
        m_nameView.setTextColor(DEFAULT_COLOR_NAMETEXT);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        m_nameView.setLayoutParams(params);
        m_nameView.setId(View.NO_ID);
        this.m_extraContainer.addView(m_nameView);
    }

    public interface RefreshListener {
        public void onRefreshRequest(DiagramFragment requester);
    }

}
