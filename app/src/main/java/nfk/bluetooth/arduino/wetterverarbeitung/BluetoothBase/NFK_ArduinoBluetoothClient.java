package nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
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
 *@author KI
  *@version 1.0b from 05.02.2017 in Bluetooth_Frame
 **/

public final class NFK_ArduinoBluetoothClient extends NFK_BluetoothClient implements ArduinoBluetoothClient{
    private List<BluetoothDataSet> m_receivedData;
    private BufferedReader m_Reader;
    private Timer m_MessageTimer;
    private long m_ScheduleRate;
    private long m_TimerOffset;
    private volatile int m_messageBufferSize;
    private volatile double m_falseProtocolSinceClear;
    private volatile double m_readSinceClear;
    private OnReceiveListener m_receiveListener;
    private static final String LOG_TAG = "ArduinoBluetoothClient";

    private NFK_ArduinoBluetoothClient(long scheduleRate, long timerOffset) {
        super(ARDUINO_CHARSET);
        setListener(new ArduinoConnectionListener());
        m_receivedData = new LinkedList<BluetoothDataSet>();
        m_Reader = null;
        m_ScheduleRate = scheduleRate;
        m_TimerOffset = timerOffset;
        m_falseProtocolSinceClear = 0;
        m_readSinceClear = 0;
        m_messageBufferSize = MESSAGE_BUFFER_SIZE;
        resetTimer();
    }

    private NFK_ArduinoBluetoothClient(long scheduleRate) {
        this(scheduleRate,(long)0);
    }

    private NFK_ArduinoBluetoothClient() {
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
    public int getMessageBufferSize() {
        return m_messageBufferSize;
    }

    @Override
    public void setMessageBufferSize(int messageBufferSize) {
        if (messageBufferSize > 0) {
            this.m_messageBufferSize = messageBufferSize;
        } else {
            Log.w(LOG_TAG, "Tried to Assign a negative or zero Buffer Size. Array Buffer size has to be bigger than 0. Ignoring value.");
        }
    }

    /**
     * Copys and returns the received Data since the last call of clear received Data.
     * @return a List of BluetoothDataSet's containing the received Data, if there is no received Data available, it will return null.
     * @throws ArduinoProtocolException if an much too high amount of numbers had to be overread and an connection to the wrong device is suspected.
     */
    @Override
    public
    @Nullable
    List<BluetoothDataSet> getReceivedData() {
        if (((double) m_falseProtocolSinceClear / m_readSinceClear) >= ((double) 0.8)) {
            throw new ArduinoProtocolException("Giant number of misreads - wrong device connected", m_falseProtocolSinceClear, m_readSinceClear);
        }
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
            m_falseProtocolSinceClear = 0;
            m_readSinceClear = 0;
        }
    }

    public void setReceiveListener(OnReceiveListener listener) {
        m_receiveListener = listener;
    }

    @Override
    public void connectBT(String AddressAndName, int tries) throws BluetoothConnectionStateException {
        super.connectBT(AddressAndName, tries);
        if (isLogEnabled()) Log.v(LOG_TAG, "creating ByteStream Converter");
        m_Reader = m_ConnectionHandler.getInputStream(); //@throws BluetoothConnectionState
        if (isLogEnabled()) Log.i(LOG_TAG, "ready to read InputStreams");
        resetTimer();
        m_MessageTimer.scheduleAtFixedRate(new ArduinoDecoder(), m_TimerOffset, m_ScheduleRate);
    }

    @Override
    public int getConnectionState() {
        if (m_ConnectionHandler != null) {
            return m_ConnectionHandler.getConnectionState();
        } else {
            throw new NullPointerException("Tried to retrieve Connection-State from an Null-Client. Initialisation failed");
        }
    }

    @Override
    public boolean destroy() {
        if (isLogEnabled()) Log.i(LOG_TAG, "destroying ArduinoBluetoothClient");
        clearReceivedData();
        m_receivedData = null;
        cancelTimer();
        m_MessageTimer = null;
        m_Reader = null;
        return super.destroy();
    }

    /**
     * Inherited Method from NFK_BluetoothClient is, because of Communication Control, not supported in this class.
     *
     * @throws UnsupportedOperationException This Class does not support the write method
     */
    @Override
    public void write(String toWrite) throws BluetoothConnectionStateException {
        throw new UnsupportedOperationException("do not send any Messages to the ArduinoClient, for this may disrupt receiving information");
    }

    /**
     * Inherited Method from NFK_BluetoothClient is, because of Communication Control, not supported in this class.
     *
     * @throws UnsupportedOperationException This Class does not support the read method.
     */
    @Override
    public int read(char[] buffer) throws BluetoothConnectionStateException {
        throw new UnsupportedOperationException("you may not interfere with internal reading");
    }

    @Override
    public void setTimer(long scheduleRate) {
        m_ScheduleRate = scheduleRate;
        if (getConnectionState() == CONNECTION_STATE_CONNECTED) {
            resetTimer();
            m_MessageTimer.scheduleAtFixedRate(new ArduinoDecoder(), m_TimerOffset, m_ScheduleRate);
        }
    }

    @Override
    public long getTimerRate() {
        return m_ScheduleRate;
    }

    private List<BluetoothDataSet> copyData(List<BluetoothDataSet> toCopy) {
        List<BluetoothDataSet> clone = new Vector<BluetoothDataSet>();
        for (BluetoothDataSet data :
                toCopy) {
            clone.add(data.clone());
        }
        return clone;
    }

    private void resetTimer() {
        cancelTimer();
        m_MessageTimer = new Timer("Bluetooth-Message Timer");
    }

    private void cancelTimer() {
        if (m_MessageTimer != null) {
            m_MessageTimer.purge();
            m_MessageTimer.cancel();
        }
    }

    private class ArduinoConnectionListener extends ConnectionListener {
        ArduinoConnectionListener() {
            super();
        }

        @Override
        public void onConnectionTermination() {
            resetTimer();
            m_Reader = null;
            super.onConnectionTermination();
        }

        @Override
        public void onConnectionLoss() {
            resetTimer();
            m_Reader = null;
            super.onConnectionLoss();
        }
    }

    private class ArduinoDecoder extends TimerTask {
        private List<BigDecimal> pm_Temps;
        private List<BigDecimal> pm_Rains;
        private List<BigDecimal> pm_Mois;
        private List<BigDecimal> pm_Light;

        public ArduinoDecoder() {
            super();
            pm_Temps = new LinkedList<BigDecimal>();
            pm_Rains = new LinkedList<BigDecimal>();
            pm_Mois = new LinkedList<BigDecimal>();
            pm_Light = new LinkedList<BigDecimal>();
        }

        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            char[] bufferedInput = new char[m_messageBufferSize];
            int received;
            try {
                received = m_Reader.read(bufferedInput);  //reading input from the Bluetooth-Queue into the Buffer
            } catch (IOException e) {
                Log.e(LOG_TAG, "unable to read", e);
                received = 0;
            }
            if (received > 0) {
                if (received > m_messageBufferSize) {  //automatically increases the messageBuffer as necessary
                    m_messageBufferSize += (received - m_messageBufferSize);
                } else if (received == m_messageBufferSize) {
                    m_messageBufferSize += 10;
                }
                if (m_receiveListener != null) m_receiveListener.onPreReceive();
                recognizeArduinoSend(bufferedInput, received);
                if (pm_Temps.size() > 0 &&
                        pm_Rains.size() > 0 &&
                        pm_Light.size() > 0 &&
                        pm_Mois.size() > 0) {
                    BigDecimal temperature = calculateMedian(pm_Temps, "Temperature");
                    BigDecimal rainStrength = calculateMedian(pm_Rains, "Rain strength");
                    BigDecimal soilMoisture = calculateMedian(pm_Mois, "Soil Moisture");
                    BigDecimal brightness = calculateMedian(pm_Light, "Brightness");
                    Date time = Calendar.getInstance().getTime();
                    BluetoothDataSet toAdd = new BluetoothDataSet(time, temperature, rainStrength, soilMoisture, brightness);
                    m_receivedData.add(toAdd);
                    clearLists();
                    if (m_receiveListener != null) m_receiveListener.onPostReceive();
                }
            }
        }

        /**
         * Reads Characters from the given Input Array and adds read values to the appropriate list.
         *
         * @param bufferedInput The char Array in which he should look for Arduino Indikators with following numbers.
         * @param received      The number of received Chars, or any other additional Border until which he should read
         */
        private void recognizeArduinoSend (char[] bufferedInput, int received) {
            for (Integer i = 0; (i + 1 < bufferedInput.length) && (i + 1 < received); i++) {  //checking for plus 1, so that the condition doesn't have to be watched, when checking for values
                boolean read = false;
                switch (bufferedInput[i]) {
                    case (BluetoothDataSet.ARDUINO_INDICATOR_SOIL_MOISTURE): {  // notices a Humidity Indicator -> next has to be corresponding value
                        if (isLogEnabled())
                            Log.v(LOG_TAG, "Found: " + Character.toString(bufferedInput[i]));
                        i++;
                        if (isDigit(bufferedInput[i])) {  // next should be a corresponding value -> adding
                            String readNumber = readNumber(bufferedInput, received, i);
                            addStringToList(pm_Mois, readNumber);
                            read = true;
                        }
                        else {
                            Log.w(LOG_TAG,"Next Character isn't a number: "+Character.toString(bufferedInput[i+1])+" -> Ignoring Indicator.");
                        }
                        break;
                    }
                    case (BluetoothDataSet.ARDUINO_INDICATOR_TEMPERATURE): {  // notices a Temperature Indicator -> next has to be corresponding value
                        if (isLogEnabled())
                            Log.v(LOG_TAG, "Found: " + Character.toString(bufferedInput[i]));
                        i++;
                        if (isDigit(bufferedInput[i])) {  // next should be a corresponding value -> adding
                            String readNumber = readNumber(bufferedInput, received, i);
                            addStringToList(pm_Temps, readNumber);
                            read = true;
                        }
                        else {
                            Log.w(LOG_TAG,"Next Character isn't a number: "+Character.toString(bufferedInput[i+1])+" -> Ignoring Indicator.");
                        }
                        break;
                    }
                    case (BluetoothDataSet.ARDUINO_INDICATOR_RAINING): {  // notices a Soil-Moisture Indicator -> next has to be corresponding value
                        if (isLogEnabled())
                            Log.v(LOG_TAG, "Found: " + Character.toString(bufferedInput[i]));  // shows the received Value
                        i++;
                        if (isDigit(bufferedInput[i])) {  // next should be a corresponding value -> adding
                            String readNumber = readNumber(bufferedInput, received, i);
                            addStringToList(pm_Rains, readNumber);
                            read = true;
                        }
                        else {
                            Log.w(LOG_TAG,"Next Character isn't a number: "+Character.toString(bufferedInput[i+1])+" -> Ignoring Indicator.");
                        }
                        break;
                    }
                    case (BluetoothDataSet.ARDUINO_INDICATOR_LIGHT): {  // notices a Soil-Moisture Indicator -> next has to be corresponding value
                        if (isLogEnabled())
                            Log.v(LOG_TAG, "Found: " + Character.toString(bufferedInput[i]));  // shows the received Value
                        i++;
                        if (isDigit(bufferedInput[i])) {  // next should be a corresponding value -> adding
                            String readNumber = readNumber(bufferedInput, received, i);
                            addStringToList(pm_Light, readNumber);
                            read = true;
                        } else {
                            Log.w(LOG_TAG, "Next Character isn't a number: " + Character.toString(bufferedInput[i + 1]) + " -> Ignoring Indicator.");
                        }
                        break;
                    }
                    //if he didn't enter any case, something has to be wrong, so he'll increment just by one, and take a look at the next Character
                }
                if (read) {
                    m_readSinceClear++;
                } else {
                    m_falseProtocolSinceClear++;
                    m_readSinceClear++;
                }
            }
        }

        /**
         * Reads on number on the given input Char array.
         *
         * @param bufferedInput The Array from which the read an received number
         * @param received      The End to which he may read at maximum
         * @param counter       The counter variable to be incremented, whilst wakling over the array. Functions as a start position.
         * @return A String containing the read number. Might be "" if the Character at the counter position isn't a number as defined by {@link java.lang.Character}.isDigit
         */
        private String readNumber(char[] bufferedInput, int received, Integer counter) {
            StringBuilder builder = new StringBuilder();
            while (counter < bufferedInput.length && isDigit(bufferedInput[counter]) && counter < received) {
                builder.append(bufferedInput[counter]);
                counter++;
            }
            return builder.toString();
        }

        /**
         * Adds a {@link BigDecimal} to the given List from the given String value. Will properly set the Scaling values.
         * @param list The List in which to add the number.
         * @param toAdd The String representation of a number, which should be converted into an BigDecimal
         */
        private void addStringToList(List<BigDecimal> list, String toAdd) {
            if (isLogEnabled()) Log.v(LOG_TAG, "Adding: " + toAdd);   // shows the received Value
            BigDecimal toInsert = new BigDecimal(toAdd);  //creates a BigDecimal from the received Value
            toInsert = toInsert.setScale(BluetoothDataSet.DATA_PRECISION, BigDecimal.ROUND_HALF_UP);  // sets this BegDecimals-DiagramSettings
            list.add(toInsert);  //adds the BigDecimal
        }

        /**
         * Calculates the Average of the given List.
         * It sums all values and then divides by the number of those values.
         *
         * @param list     The List from which to calculate the Average
         * @param listName The String used to refer to the Operation in the Log
         * @return a BigDecimal which contains the Average from the List, or 0 if the List didn't have any Elements.
         */
        private BigDecimal calculateMedian(List<BigDecimal> list, String listName) {
            BigDecimal tempCalculator = new BigDecimal(0);
            tempCalculator = tempCalculator.setScale(BluetoothDataSet.DATA_PRECISION, BigDecimal.ROUND_HALF_UP);
            if (!list.isEmpty()) {
                for (BigDecimal value :
                        list) {
                    tempCalculator = tempCalculator.add(value);
                }
                tempCalculator = tempCalculator.divide(new BigDecimal(list.size()), BigDecimal.ROUND_HALF_UP);
                if (isLogEnabled())
                    Log.v(LOG_TAG, "calculated Median for the List: " + listName + " with the value: " + tempCalculator.toString());
            }
            return tempCalculator;
        }

        private void clearLists() {
            pm_Mois.clear();
            pm_Rains.clear();
            pm_Temps.clear();
            pm_Light.clear();
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
            clearLists();
            pm_Temps = null;
            pm_Rains = null;
            pm_Mois = null;
            pm_Light = null;
            return super.cancel();
        }
    }
}
