package kevin.test.bluetooth.bluetooth_frame;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.ArduinoBluetoothClient;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.BluetoothInactivityException;
import kevin.test.bluetooth.bluetooth_frame.BluetoothBase.NFK_ArduinoBluetoothClient;

public class DeviceList extends ListActivity {
    private ArrayAdapter<String> addressAdapter;
    private static final String LOG_TAG = "Device List";
    List<String> addresses;
    ArduinoBluetoothClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = NFK_ArduinoBluetoothClient.getClient();
        try {
            addresses = client.getAddressesAndNames(true);
        } catch (BluetoothInactivityException e) {
            Log.e(LOG_TAG, "Bluetooh is inactive, needs to be activated", e);
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
            try {
                addresses = client.getAddressesAndNames(true);
            } catch (BluetoothInactivityException e1) {
                Log.e(LOG_TAG, "unable to activate Bluetooth", e1);
                finish();
                return;
            }
        }
        addressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addresses);
        setListAdapter(addressAdapter);
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String chosen = addresses.get(position);
        Log.i(LOG_TAG, "List item was chosen: " + chosen);
        Intent intent = new Intent(this, Connected.class);
        intent.putExtra("addresse", chosen);
        startActivity(intent);
    }

    /**
     * @see Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addresses != null) {
            addresses.clear();
            addresses = null;
        }
        if (client != null) {
            client.destroy();
            client = null;
        }
    }
}
