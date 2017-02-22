package kevin.test.bluetooth.bluetooth_frame;

import android.app.Activity;
import android.graphics.Color;
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

    Diagramm temperatureDiagram;
    ArrayList<Integer> temperaturWerte = new ArrayList<>();

    Diagramm luftfeuchteDiagram;
    ArrayList<Integer> luftfeuchteWerte = new ArrayList<>();

    int layoutWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        String addresse = ""+getIntent().getExtras().getString("addresse");

        Toast.makeText(getApplicationContext(), addresse, Toast.LENGTH_SHORT).show();


        if(!addresse.isEmpty()) {

            final ArduinoBluetoothClient client = NFK_ArduinoBluetoothClient.getClient();

            try {
                client.connectBT(addresse, 1);

                final LinearLayout l = (LinearLayout)findViewById(R.id.layout);
                l.setBackgroundColor(Color.LTGRAY);

//Größe des Felds
                l.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            l.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        else {
                            l.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        final float touchWidth = l.getWidth();
                        layoutWidth = (int)touchWidth;

                        int width = layoutWidth;
                        int height = (int) Math.round(layoutWidth*0.6);
                        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);

                        TextView temperatureTitle = new TextView(getApplicationContext());
                        temperatureTitle.setTextColor(Color.BLACK);
                        temperatureTitle.setText("Temperatur:");
                        l.addView(temperatureTitle);

                        temperatureDiagram = new Diagramm(getApplicationContext(), height, width, temperaturWerte, -25, 50, "°C");
                        temperatureDiagram.setBackgroundColor(Color.WHITE);
                        temperatureDiagram.setLayoutParams(parms);
                        l.addView(temperatureDiagram);

                        TextView luftfeuchteTitle = new TextView(getApplicationContext());
                        luftfeuchteTitle.setTextColor(Color.BLACK);
                        luftfeuchteTitle.setText("Luftfeuchte:");
                        l.addView(luftfeuchteTitle);

                        luftfeuchteDiagram = new Diagramm(getApplicationContext(), height, width, luftfeuchteWerte, 0, 100, "%");
                        luftfeuchteDiagram.setBackgroundColor(Color.WHITE);
                        luftfeuchteDiagram.setLayoutParams(parms);
                        l.addView(luftfeuchteDiagram);



                    }
                });

            //Die Diagramme werden alle 10 sekunden geupdatet

            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    List<DataSet> data = client.getReceivedData();
                    if(data != null) {
                        for (int i = 0; i < 10; i++) {
                            DataSet werteSet = data.get(data.size() - 10 + i);
                            temperatureDiagram.addWert(werteSet.getTemperature().intValue());
                            luftfeuchteDiagram.addWert(werteSet.getHumidity().intValue());
                        }
                    }
                }
            }, 0, 10000);

            }
            catch (Throwable t){
                Log.e("Connected:", t.toString());
            }


        }
        else {
            Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
        }

    }


}
