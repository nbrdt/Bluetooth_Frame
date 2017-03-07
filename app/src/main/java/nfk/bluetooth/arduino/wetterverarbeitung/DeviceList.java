package nfk.bluetooth.arduino.wetterverarbeitung;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.ArduinoBluetoothClient;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.BluetoothInactivityException;
import nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase.NFK_ArduinoBluetoothClient;

/**
 * @author KI
 * @version 1.1
 */
public class DeviceList extends ListActivity implements ActivityResults {
    private ArrayAdapter<String> addressAdapter;
    private static final String LOG_TAG = "Device List";
    List<String> addresses;
    ArduinoBluetoothClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = NFK_ArduinoBluetoothClient.getClient();
        setList();
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
        Intent intent = new Intent(this, DiagramActivity.class);
        intent.putExtra("addresse", chosen);
        startActivityForResult(intent, DIAGRAMACTIVITY_REQUEST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DIAGRAMACTIVITY_REQUEST_CODE) {
            if (resultCode == DiagramActivity.RESULT_ERROR) {
                showActivityError(data);
            }
        } else if (requestCode == ACTIVATEBLUETOOTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                this.recreate();
            } else {
                finishWithError("Bluetooth could not be activated - Cannot Proceed without valid Bluetooth Connection.");
            }
        }
    }

    @Override
    public void setErrorMessage(String message) {
        Intent data = new Intent("Closed on Error");
        data.putExtra(RESULTKEY_ERROR_MESSAGE, message);
        setResult(RESULT_ERROR, data);
    }

    @Override
    public void finishWithError(String message) {
        setErrorMessage(message);
        finish();
    }

    @Override
    public void showActivityError(Intent errorMessage) {
        Toast.makeText(this, errorMessage.getStringExtra(RESULTKEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
    }

    private void setList() {
        try {
            addresses = client.getAddressesAndNames(true);
            addressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addresses);
            setListAdapter(addressAdapter);
        } catch (BluetoothInactivityException e) {
            Log.e(LOG_TAG, "Bluetooh is inactive, needs to be activated", e);
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }
    }
}
