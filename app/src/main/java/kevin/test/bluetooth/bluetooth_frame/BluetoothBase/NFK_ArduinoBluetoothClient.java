package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static java.lang.Character.isDigit;

/**
  *@author Kevin Iselborn
  *@version 1.0b from 05.02.2017 in Bluetooth_Frame
 **/

public final class NFK_ArduinoBluetoothClient extends NFK_BluetoothClient implements ArduinoBluetoothClient{
    private List<DataSet> m_receivedData;
    private BufferedReader m_Reader;
    private BufferedWriter m_Writer;
    private Timer m_MessageTimer;
    private long m_ScheduleRate;
    private long m_TimerOffset;
    private static final String LOG_TAG = "ArduinoBluetoothClient";

    private NFK_ArduinoBluetoothClient(long scheduleRate, long timerOffset) {
        super(ARDUINO_CHARSET);
        setListener(new ArduinoConnectionListener());
        m_MessageTimer = new Timer("Bluetooth-Message Timer");
        m_receivedData = new LinkedList<DataSet>();
        m_Reader = null;
        m_Writer = null;
        m_ScheduleRate = scheduleRate;
        m_TimerOffset = timerOffset;
    }

    private NFK_ArduinoBluetoothClient(long scheduleRate) {
        this(scheduleRate,(long)0);
    }

    public NFK_ArduinoBluetoothClient() {
        this((long)1000);
    }

    public static ArduinoBluetoothClient getClient(long scheduleRate, long timerOffset) {
        return new NFK_ArduinoBluetoothClient(scheduleRate, timerOffset);
    }

    public static ArduinoBluetoothClient getClient(long scheduleRate) {
        return new NFK_ArduinoBluetoothClient(scheduleRate);
    }

    public static ArduinoBluetoothClient getClient() {
        return new NFK_ArduinoBluetoothClient();
    }

    @Override
    public List<DataSet> getReceivedData  () {
        if (m_receivedData.isEmpty()) {
            return null;
        }
        else {
            return copyData(m_receivedData);
        }
    }

    @Override
    public void clearReceivedData(){
        if (!m_receivedData.isEmpty()) {
            m_receivedData.clear();
        }
    }

    private List<DataSet> copyData (List<DataSet> toCopy) {
        List<DataSet> clone = new Vector<DataSet>();
        for (DataSet data:
             toCopy) {
            clone.add(data.clone());
        }
        return clone;
    }

    @Override
    public void connectBT(String AddressAndName, int tries) throws BluetoothConnectionStateException {
        super.connectBT(AddressAndName, tries);
        Log.v(LOG_TAG,"creating ByteStream Converter");
        m_Reader = m_ConnectionHandler.getInputStream(); //@throws BluetoothConnectionState
        m_Writer = m_ConnectionHandler.getOutputStream(); //@throws BluetoothConnectionState
        Log.i(LOG_TAG,"ready to read InputStreams");
        m_MessageTimer.scheduleAtFixedRate(new ArduinoDecoder(), m_TimerOffset, m_ScheduleRate);
    }



    private class ArduinoConnectionListener extends ConnectionListener {
        ArduinoConnectionListener() {
            super();
        }

        @Override
        public void onConnectionTermination() {
            super.onConnectionTermination();
            m_MessageTimer.purge();
            m_Reader = null;
            m_Writer = null;
        }

        @Override
        public void onConnectionLoss() {
            super.onConnectionLoss();
            m_MessageTimer.purge();
            m_Reader = null;
            m_Writer = null;
        }
    }

    private class ArduinoDecoder extends TimerTask {
        private List<BigDecimal> pm_Temps;
        private List<BigDecimal> pm_Humids;
        private List<BigDecimal> pm_Mois;

        public ArduinoDecoder() {
            super();
            pm_Temps = new LinkedList<BigDecimal>();
            pm_Humids = new LinkedList<BigDecimal>();
            pm_Mois = new LinkedList<BigDecimal>();
        }

        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            char[] bufferedInput = new char[MESSAGE_BUFFER_SIZE];
            try {
                int received = m_Reader.read(bufferedInput);  //reading input from the Bluetooth-Queue into the Buffer
                if(received>0) {
                    recognizeArduinoSend(bufferedInput,received);
                    if (pm_Temps.size() >0 &&
                        pm_Humids.size()>0 &&
                        pm_Mois.size()  >0 ) {
                        BigDecimal temperature = calculateMedian(pm_Temps,"Temperature");
                        BigDecimal humidity = calculateMedian(pm_Humids, "Humidity");
                        BigDecimal soilMoisture = calculateMedian(pm_Mois,"Soil Moisture");
                        Date time = Calendar.getInstance().getTime();
                        DataSet toAdd = new DataSet(time, temperature, humidity, soilMoisture);
                        m_receivedData.add(toAdd);
                        clearLists();
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "unable to read", e);
            }
        }

        private void recognizeArduinoSend (char[] bufferedInput, int received) {
            for (Integer i=0; ((i+1) <received) && ((i+1) <bufferedInput.length); i++) {
                switch (bufferedInput[i]) {
                    case (DataSet.ARDUINO_INDICATOR_HUMIDITY): {  // notices a Humidity Indicator -> next has to be corresponding value
                        Log.v(LOG_TAG, "Found: " + Character.toString(bufferedInput[i]));
                        if (isDigit(bufferedInput[i+1])) {  // next might be a corresponding value -> adding
                            StringBuilder builder = new StringBuilder();
                            for (int j=i;isDigit(bufferedInput[j+1]) && (j+1)<received && (j+1)<bufferedInput.length;j++) {
                                builder.append(bufferedInput[j+1]);
                                i++;
                            }
                            addStringToList(pm_Humids,builder.toString());
                            i++; // Increments so that he can read the next set of two
                        }
                        else {
                            Log.w(LOG_TAG,"Next Character isn't a number: "+Character.toString(bufferedInput[i+1])+" -> Ignoring Indicator.");
                        }
                        break;
                    }
                    case (DataSet.ARDUINO_INDICATOR_TEMPERATURE): {  // notices a Temperature Indicator -> next has to be corresponding value
                        Log.v(LOG_TAG, "Found: " + Character.toString(bufferedInput[i]));
                        if (isDigit(bufferedInput[i+1])) {  // next might be a corresponding value -> adding
                            StringBuilder builder = new StringBuilder();
                            for (int j=i;isDigit(bufferedInput[j+1]) && (j+1)<received && (j+1)<bufferedInput.length;j++) {
                                builder.append(bufferedInput[j+1]);
                                i++;
                            }
                            addStringToList(pm_Temps, builder.toString());
                            i++;  // Increments so that he can read the next set of two
                        }
                        else {
                            Log.w(LOG_TAG,"Next Character isn't a number: "+Character.toString(bufferedInput[i+1])+" -> Ignoring Indicator.");
                        }
                        break;
                    }
                    case (DataSet.ARDUINO_INDICATOR_SOIL_MOISTURE): {  // notices a Soil-Moisture Indicator -> next has to be corresponding value
                        Log.v(LOG_TAG, "Found: " + Character.toString(bufferedInput[i]));  // shows the received Value
                        if (isDigit(bufferedInput[i+1])) {  // next might be a corresponding value -> adding
                            StringBuilder builder = new StringBuilder();
                            for (int j=i;isDigit(bufferedInput[j+1]) && (j+1)<received && (j+1)<bufferedInput.length;j++) {
                                builder.append(bufferedInput[j+1]);
                                i++;
                            }
                            addStringToList(pm_Mois, builder.toString());
                            i++; // Increments so that he can read the next set of two
                        }
                        else {
                            Log.w(LOG_TAG,"Next Character isn't a number: "+Character.toString(bufferedInput[i+1])+" -> Ignoring Indicator.");
                        }
                        break;
                    }
                    //if he didn't enter any case, something has to be wrong, so he'll increment just one, o look, wheter the next Character is a possible set
                }
            }
        }

        private void addStringToList(List<BigDecimal> list, String toAdd) {
            Log.v(LOG_TAG, "Adding: " + toAdd);   // shows the received Value
            BigDecimal toInsert = new BigDecimal(toAdd);  //creates a BigDecimal from the received Value
            toInsert = toInsert.setScale(DataSet.DATA_PRECISION, BigDecimal.ROUND_HALF_UP);  // sets this BegDecimals-Settings
            list.add(toInsert);  //adds the BigDecimal
        }

        private BigDecimal calculateMedian(List<BigDecimal> list, String listName) {
            BigDecimal tempCalculator = new BigDecimal(0);
            tempCalculator = tempCalculator.setScale(DataSet.DATA_PRECISION, BigDecimal.ROUND_HALF_UP);
            for (BigDecimal value:
                    list) {
                tempCalculator = tempCalculator.add(value);
            }
            tempCalculator = tempCalculator.divide(new BigDecimal(list.size()), BigDecimal.ROUND_HALF_UP);
            Log.v(LOG_TAG,"calculated Median for the List: "+listName+" with the value: "+tempCalculator.toString());
            return tempCalculator;
        }

        private void clearLists() {
            pm_Mois.clear();
            pm_Humids.clear();
            pm_Temps.clear();
        }

        /**
         * Cancels this timer task.  If the task has been scheduled for one-time
         * execution and has not yet run, or has not yet been scheduled, it will
         * never run.  If the task has been scheduled for repeated execution, it
         * will never run again.  (If the task is running when this call occurs,
         * the task will run to completion, but will never run again.)
         * <p>
         * <p>Note that calling this method from within the <tt>run</tt> method of
         * a repeating timer task absolutely guarantees that the timer task will
         * not run again.
         * <p>
         * <p>This method may be called repeatedly; the second and subsequent
         * calls have no effect.
         *
         * @return true if this task is scheduled for one-time execution and has
         * not yet run, or this task is scheduled for repeated execution.
         * Returns false if the task was scheduled for one-time execution
         * and has already run, or if the task was never scheduled, or if
         * the task was already cancelled.  (Loosely speaking, this method
         * returns <tt>true</tt> if it prevents one or more scheduled
         * executions from taking place.)
         */
        @Override
        public boolean cancel() {
            pm_Temps.clear();
            pm_Mois.clear();
            pm_Humids.clear();
            pm_Temps = null;
            pm_Humids = null;
            pm_Mois = null;
            return super.cancel();
        }
    }
}
