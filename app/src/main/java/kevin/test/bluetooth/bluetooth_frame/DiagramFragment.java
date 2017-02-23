package kevin.test.bluetooth.bluetooth_frame;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


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
    //Default m_values
    private static final int DEFAULT_COLOR_NAMETEXT = Color.BLACK;
    private static final int DEFAULT_COLOR_DIAGRAM_BACKGROUND = Color.WHITE;


    private String m_name, m_unit;
    private TextView m_nameView;
    private LinearLayout m_rootView;
    private Context m_attachContext;
    private Diagramm m_diagram;
    private ArrayList<Integer> m_values;
    private int m_height, m_width, m_min, m_max;


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
     * public static DiagramFragment newInstance(String m_name, Diagramm m_diagram) {
     * DiagramFragment fragment = new DiagramFragment();
     * Bundle args = new Bundle();
     * args.putString(INSTANCESTATE_DIAGRAM_NAME, m_name);
     * fragment.setArguments(args);
     * fragment.setDiagram(m_diagram);
     * return fragment;
     * }
     **/

    public static DiagramFragment newInstance(String name, int height, int width, ArrayList<Integer> values, int min, int max, String unit) {
        DiagramFragment fragment = new DiagramFragment();
        Bundle args = new Bundle();
        args.putString(INSTANCESTATE_DIAGRAM_NAME, name);
        args.putInt(INSTANCESTATE_DIAGRAM_HEIGHT, height);
        args.putInt(INSTANCESTATE_DIAGRAM_WIDTH, width);
        args.putIntegerArrayList(INSTANCESTATE_DIAGRAM_VALUES, values);
        args.putInt(INSTANCESTATE_DIAGRAM_MINVALUE, min);
        args.putInt(INSTANCESTATE_DIAGRAM_MAXVALUE, max);
        args.putString(INSTANCESTATE_DIAGRAM_UNIT, unit);
        fragment.setArguments(args);
        fragment.setDiagram(null);
        return fragment;
    }

    private void setDiagram(Diagramm diagram) {
        this.m_diagram = diagram;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            m_name = args.getString(INSTANCESTATE_DIAGRAM_NAME);
            m_height = args.getInt(INSTANCESTATE_DIAGRAM_HEIGHT);
            m_width = args.getInt(INSTANCESTATE_DIAGRAM_WIDTH);
            m_min = args.getInt(INSTANCESTATE_DIAGRAM_MINVALUE);
            m_max = args.getInt(INSTANCESTATE_DIAGRAM_MAXVALUE);
            m_values = args.getIntegerArrayList(INSTANCESTATE_DIAGRAM_VALUES);
            m_unit = args.getString(INSTANCESTATE_DIAGRAM_UNIT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m_rootView = new LinearLayout(container.getContext());
        m_rootView.setOrientation(LinearLayout.VERTICAL);
        m_rootView.setId(View.NO_ID);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        m_rootView.setLayoutParams(params);
        return m_rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        m_attachContext = context;
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
        m_nameView = null;
        m_rootView = null;
        m_values.clear();
        m_values = null;
    }

    public void addToDiagram(Integer valueToAdd) {
        m_values.add(valueToAdd);
    }

    public void updateDiagram() {
        m_rootView.removeView(m_nameView);
        m_rootView.removeView(m_diagram);
        createLayout();
    }

    private void createLayout() {
        drawTextName();
        drawDiagram();
    }

    private void drawDiagram() {
        setDiagram(new Diagramm(m_attachContext, m_height, m_width, m_values, m_min, m_max, m_unit));
        m_diagram.setBackgroundColor(DEFAULT_COLOR_DIAGRAM_BACKGROUND);
        m_diagram.setLayoutParams(new LinearLayout.LayoutParams(m_width, m_height));
        this.m_rootView.addView(m_diagram);
    }

    private void drawTextName() {
        this.m_nameView = new TextView(m_attachContext);
        m_nameView.setText(m_name);
        m_nameView.setTextColor(DEFAULT_COLOR_NAMETEXT);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        m_nameView.setLayoutParams(params);
        this.m_rootView.addView(m_nameView);
    }

}
