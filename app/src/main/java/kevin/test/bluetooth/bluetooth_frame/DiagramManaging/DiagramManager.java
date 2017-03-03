package kevin.test.bluetooth.bluetooth_frame.DiagramManaging;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KI
 * @version 0.0
 **/
public class DiagramManager {
    private static final String LOG_TAG = "Diagram Manager";
    private Activity m_host;
    private List<DiagramSettings> m_diagrams;
    private ViewGroup m_rootView;
    private DataProvider m_refresher;
    private DiagramFragment m_current;
    private DiagramFragment.RefreshListener m_diagramRefresher;

    public DiagramManager(Activity hostActivity, ViewGroup rootView, List<DiagramSettings> diagrams, final DataProvider refresher) {
        this.m_host = hostActivity;
        this.m_diagrams = diagrams;
        this.m_rootView = rootView;
        this.m_refresher = refresher;
        this.m_current = null;
        this.m_diagramRefresher = new DiagramFragment.RefreshListener() {
            @Override
            public void onRefreshRequest(DiagramFragment requester) {
                Log.v(LOG_TAG, "requested Data Refresh");
                if (requester == m_current) {
                    requester.updateDiagram(refresher.onRefreshRequest(requester.getSettings()));
                }
            }
        };  
    }

    public DiagramManager(Activity hostActivity, int rootViewId, List<DiagramSettings> diagrams, DataProvider refresher) {
        this(hostActivity, (ViewGroup) hostActivity.findViewById(rootViewId), diagrams, refresher);
    }

    public void showDiagram(String name) {
        DiagramSettings settings = getSettingsFromName(name); //throws IllegalArgumentException
        ArrayList<Integer> data = m_refresher.onRefreshRequest(settings);
        FragmentTransaction fragmentTransaction = m_host.getFragmentManager().beginTransaction();
        if (!isActive()) {
            m_current = DiagramFragment.newInstance(settings, data);
            fragmentTransaction.add(m_rootView.getId(), m_current, settings.getName());
        } else {
            DiagramFragment replaceFragment = DiagramFragment.newInstance(settings, data);
            fragmentTransaction.remove(m_current);
            fragmentTransaction.add(m_rootView.getId(), replaceFragment, settings.getName());
            m_current = replaceFragment;
        }
        m_current.setRefreshListener(m_diagramRefresher);
        //fragmentTransaction.addToBackStack(settings.getName());  //das Diagramm soll mmer angezeigt werden: daher kein Back stack
        fragmentTransaction.commit();
    }

    public boolean remove() {
        if (isActive()) {
            Log.v(LOG_TAG, "removing Diagram");
            FragmentTransaction fragmentTransaction = m_host.getFragmentManager().beginTransaction();
            fragmentTransaction.remove(m_current);
            m_current = null;
            fragmentTransaction.commit();
            return true;
        } else {
            Log.v(LOG_TAG, "no diagram to remove");
            return false;
        }
    }

    private boolean isActive() {
        return m_current != null;
    }

    public DiagramSettings getShown() {
        if (isActive()) {
            return m_current.getSettings();
        } else {
            return null;
        }
    }

    private DiagramSettings getSettingsFromName(String name) {
        for (DiagramSettings setting :
                m_diagrams) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        throw new IllegalArgumentException("could not resolve Diagram settings");
    }

    public void update() {
        if (m_current != null) {
            m_current.updateDiagram(m_refresher.onRefreshRequest(m_current.getSettings()));
        }
    }

    public void setDiagrams(List<DiagramSettings> diagrams) {
        m_diagrams = diagrams;
    }

    public interface DataProvider {
        public ArrayList<Integer> onRefreshRequest(DiagramSettings fragmentDescription);
    }
}
