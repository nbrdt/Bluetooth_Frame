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

public class DeviceList extends AppCompatActivity {
    //Class Attributes
    private Button btnPaired = null;
    private ListView deviceList = null;
    private ArduinoBluetoothClient myBtClient = NFK_ArduinoBluetoothClient.getClient();
    private AdapterView.OnItemClickListener m_afterChoosing = new AdapterView.OnItemClickListener() {
        public void onItemClick (AdapterView av, View v, int iarg, long larg) {
            Log.d("DeviceList","Item was chosen");
            String clicked = ((TextView)v).getText().toString();

            Intent intent = new Intent(DeviceList.this, Connected.class);
            intent.putExtra("addresse", clicked);
            startActivity(intent);



        }
    };


    //end of Class Attributes

    //Class initilisation

    //end of intitilisation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        btnPaired = (Button) findViewById(R.id.button_connect);
        deviceList = (ListView)findViewById(R.id.ListView);
        deviceList.setOnItemClickListener(m_afterChoosing);
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddressesAndNames(false);
            }
        });
    }

    public void showAddressesAndNames(boolean tried) {
        try {
            List<String> adan =  myBtClient.getAddressesAndNames(true);
            final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, adan);
            deviceList.setAdapter(adapter);

        }
        catch (BluetoothInactivityException e) {
            showMessageBox(e.getMessage());
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
            if (!tried) showAddressesAndNames(true);
        }
        catch (BluetoothMissingException e) {
            showMessageBox(e.getMessage());
            finish();
        }
    }

    private void showMessageBox (String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


}
