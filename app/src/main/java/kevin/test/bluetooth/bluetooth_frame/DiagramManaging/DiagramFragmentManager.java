package kevin.test.bluetooth.bluetooth_frame.DiagramManaging;

import android.app.Activity;
import android.app.FragmentManager;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author KI
 * @version 0.0
 **/
public class DiagramFragmentManager {
    private Activity m_host;
    private List<DiagramSettings> m_diagrams;
    private ViewGroup m_rootView;

    public DiagramFragmentManager(Activity hostActivity, ViewGroup rootView, List<DiagramSettings> diagrams) {
        this.m_host = hostActivity;
        this.m_diagrams = diagrams;
        this.m_rootView = rootView;
    }

    public DiagramFragmentManager(Activity hostActivity, int rootViewId, List<DiagramSettings> diagrams) {
        this(hostActivity, (ViewGroup) hostActivity.findViewById(rootViewId), diagrams);
    }


}
