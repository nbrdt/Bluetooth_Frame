package kevin.test.bluetooth.bluetooth_frame;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.*;

public class Connected extends Activity {
    private static final String LOG_TAG = "Connected Activity";
    ArduinoBluetoothClient client;
    private Button refreshButton;

    DiagramFragment temperatureDiagram;
    ArrayList<Integer> temperaturWerte = new ArrayList<>();

    DiagramFragment luftfeuchteDiagram;
    ArrayList<Integer> luftfeuchteWerte = new ArrayList<>();

    int layoutWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        refreshButton = (Button) findViewById(R.id.refreshButton);

        String addresse = "" + getIntent().getExtras().getString("addresse");

        Toast.makeText(getApplicationContext(), addresse, Toast.LENGTH_SHORT).show();


        if (!addresse.isEmpty()) {

             client = NFK_ArduinoBluetoothClient.getClient();

            try {
                client.connectBT(addresse, 1);

                final LinearLayout l = (LinearLayout) findViewById(R.id.activity_connected);
                l.setBackgroundColor(Color.LTGRAY);


//Größe des Felds
                l.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            l.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            l.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        final float touchWidth = l.getWidth();
                        layoutWidth = (int) touchWidth;

                        int width = layoutWidth;
                        int height = (int) Math.round(layoutWidth * 0.6);
                        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
                        temperatureDiagram = DiagramFragment.newInstance("Temperatur:", height, width, temperaturWerte, -25, 100, "°C"); //
                        fragmentTransaction.add(R.id.activity_connected, temperatureDiagram, "temperaturen");
                        fragmentTransaction.addToBackStack("temperatures");
                        fragmentTransaction.commit();

                        fragmentTransaction = fragmentManager.beginTransaction();
                        luftfeuchteDiagram = DiagramFragment.newInstance("Luftfeuchte:",height, width,luftfeuchteWerte,0,100,"%");
                        fragmentTransaction.add(R.id.activity_connected, luftfeuchteDiagram, "luftfeuchten");
                        fragmentTransaction.addToBackStack("luftfeuchten");
                        fragmentTransaction.commit();

                    }
                });

            } catch (BluetoothConnectionStateException e) {
                Log.e(LOG_TAG, "connection Error", e);
                Toast.makeText(getApplicationContext(), "could not connect Client", Toast.LENGTH_LONG);
                finish();
            }


        } else {
            Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }


    public void refreshButtonClicked(View v) {
        List<DataSet> received = client.getReceivedData();
        if (received != null) {
            client.clearReceivedData();
            for (DataSet data :
                    received) {
                temperatureDiagram.addToDiagram(data.getTemperature().intValue());
                luftfeuchteDiagram.addToDiagram(data.getHumidity().intValue());
            }
            temperatureDiagram.updateDiagram();
            luftfeuchteDiagram.updateDiagram();
        }
    }

    public void disconnectButtonClicked(View v) {
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the {@link #isFinishing} method.
     * <p>
     * <p><em>Note: do not count on this method being called as a place for
     * saving data! For example, if an activity is editing data in a content
     * provider, those edits should be committed in either {@link #onPause} or
     * {@link #onSaveInstanceState}, not here.</em> This method is usually implemented to
     * free resources like threads that are associated with an activity, so
     * that a destroyed activity does not leave such things around while the
     * rest of its application is still running.  There are situations where
     * the system will simply kill the activity's hosting process without
     * calling this method (or any others) in it, so it should not be used to
     * do things that are intended to remain around after the process goes
     * away.
     * <p>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onPause
     * @see #onStop
     * @see #finish
     * @see #isFinishing
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.destroy();
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
