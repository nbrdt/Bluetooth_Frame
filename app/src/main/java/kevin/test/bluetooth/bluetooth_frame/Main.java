package kevin.test.bluetooth.bluetooth_frame;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothInactivityException;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothMissingException;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.NFK_ArduinoBluetoothClient;

public class Main extends AppCompatActivity {
    //Class Attributes
    private Button btnPaired = null;
    private ListView deviceList = null;
    private ArduinoBluetoothClient myBtClient = NFK_ArduinoBluetoothClient.getClient();

    //end of Class Attributes

    //Class initilisation

    //end of intitilisation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnPaired = (Button) findViewById(R.id.button_connect);
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showAddresses = new Intent(Main.this, DeviceList.class);
                startActivity(showAddresses);
            }
        });
    }

    private void showMessageBox(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


}
