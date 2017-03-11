package nfk.bluetooth.arduino.wetterverarbeitung.BluetoothBase;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * writes and reads Data File of name received_data:
 * with Content like:
 * ####
 * Date:Tue Feb 28 14:56:20 GMT+01:00 2017
 * Temperature:20.2000000000
 * Humidity:56.2000000000
 * Soil Moisture:37.7500000000
 * ####
 * Date:Tue Feb 28 18:23:19 GMT+01:00 2017
 * Temperature:49.8571428571
 * Humidity:50.5000000000
 * Soil Moisture:37.5714285714
 *
 * @author KI
 * @version 1.1a
 **/
public class BluetoothDataProvider {
    private static final String DATAFILE_NAME = "received_data";
    private static final String INDICATOR_SETSEPARATOR = "####";
    private static final String INDICATOR_TIME = "Date:";
    private static final String INDICATOR_TEMP = "Temperature:";
    private static final String INDICATOR_HUMID = "Humidity:";
    private static final String INDICATOR_SOIL = "Soil Moisture:";
    private static final String INDICATOR_LIGHT = "Brightness:";

    private static final String LOG_TAG = "Bluetooth-Data Provider";

    private Context m_callingContext;
    private File m_fileDirectory;
    private File m_Data;
    private PrintWriter m_FileWriter;
    private LineNumberReader m_FileReader;
    private DateFormat m_formatter;
    private boolean logEnabled;

    public BluetoothDataProvider(Context context) {
        if (context != null) {
            m_callingContext = context;
            m_fileDirectory = context.getFilesDir();
        } else {
            throw new IllegalArgumentException("Can't manage Data without an context");
        }
        m_Data = new File(m_fileDirectory, DATAFILE_NAME);
        m_formatter = DateFormat.getDateTimeInstance();
        logEnabled = true;
        //Log.i(LOG_TAG, "Path:" + m_Data.getAbsolutePath());
    }

    private List<BluetoothDataSet> readData(boolean leaveInputStreamOpen) throws UnrecognizableBluetoothDataException {
        checkAndRecreate(true, false);  //throws UnrecognizableBluetoothDataException
        List<BluetoothDataSet> readData = new Vector<>();
        try {
            String read;
            do {
                read = m_FileReader.readLine();
                if (read != null && read.equalsIgnoreCase(INDICATOR_SETSEPARATOR)) {
                    Date date = readDate(INDICATOR_TIME);
                    BigDecimal temperature = readValue(INDICATOR_TEMP);
                    BigDecimal humidity = readValue(INDICATOR_HUMID);
                    BigDecimal soilMoisture = readValue(INDICATOR_SOIL);
                    BigDecimal brightness = readValue(INDICATOR_LIGHT);
                    readData.add(new BluetoothDataSet(date, temperature, humidity, soilMoisture, brightness));
                }
            } while (read != null);
        } catch (IOException e) {
            throw new UnrecognizableBluetoothDataException("Could not read Data", e);
        }
        if (!leaveInputStreamOpen) {
            try {
                m_FileReader.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close File, destroying reference anyway", e);
            }
            m_FileReader = null;
        }
        return readData;
    }

    private void writeData(List<BluetoothDataSet> toWrite, boolean leaveOutputStreamOpen) {
        if (m_FileWriter == null) {
            try {
                m_FileWriter = new PrintWriter(new FileWriter(m_Data), true);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not resolve File Writer", e);
                return;
            }
        }
        m_FileWriter.println("");
            for (BluetoothDataSet data :
                    toWrite) {
                m_FileWriter.println(INDICATOR_SETSEPARATOR);
                writeDate(INDICATOR_TIME, data.getTimeStamp());
                writeValue(INDICATOR_TEMP, data.getTemperature());
                writeValue(INDICATOR_HUMID, data.getRainStrength());
                writeValue(INDICATOR_SOIL, data.getSoilMoisture());
                writeValue(INDICATOR_LIGHT, data.getBrightness());
            }
        if (!leaveOutputStreamOpen) {
            m_FileWriter.close();
            m_FileWriter = null;
        }
    }

    public List<BluetoothDataSet> readData() throws UnrecognizableBluetoothDataException {
        return readData(false);
    }

    public void writeData(List<BluetoothDataSet> toWrite) {
        writeData(toWrite, false);
    }

    public void deleteAll() {
        m_Data.delete();
    }

    public void setLogEnabled(boolean enabled) {
        logEnabled = enabled;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    private BigDecimal readValue(String indicator) throws IOException, UnrecognizableBluetoothDataException {
        String read = m_FileReader.readLine();
        if (read != null && read.contains(indicator)) {
            String[] parts = read.split(indicator);
            return new BigDecimal(parts[1]);
        } else {
            throw new UnrecognizableBluetoothDataException("File does not Match read requirements, read:" + read);
        }
    }

    private Date readDate(String indicator) throws IOException, UnrecognizableBluetoothDataException {
        String read = m_FileReader.readLine();
        if (read != null && read.contains(indicator)) {
            String[] parts = read.split(indicator);
            Date parsed;
            try {
                parsed = m_formatter.parse(parts[1]);  //TODO find Arguments, that parse the complete Data and not only Day,Month,Year
            } catch (ParseException e) {
                throw new UnrecognizableBluetoothDataException("Could not Parse Date", e);
            }
            return parsed;
        } else {
            throw new UnrecognizableBluetoothDataException("File does not Match read requirements, read:" + read);
        }
    }

    private void writeValue(String indicator, BigDecimal toWrite) {
        m_FileWriter.print(indicator);
        String numberToWrite = toWrite.toString();
        if (isLogEnabled()) Log.v(LOG_TAG, "Writing Number:" + numberToWrite);
        m_FileWriter.println(numberToWrite);
    }

    private void writeDate(String indicator, Date toWrite) {
        m_FileWriter.print(indicator);
        String date = m_formatter.format(toWrite);
        if (isLogEnabled()) Log.v(LOG_TAG, "Writing Date:" + toWrite);
        m_FileWriter.println(date);
    }

    private void checkAndRecreate(boolean leaveReadStreamOpen, boolean leaveWriteStreamOpen) throws UnrecognizableBluetoothDataException {
        try {
            m_FileReader = new LineNumberReader(new FileReader(m_Data));
        } catch (FileNotFoundException e) {
            if (m_FileWriter == null) {
                try {
                    m_FileWriter = new PrintWriter(new FileWriter(m_Data), true);
                } catch (IOException e1) {
                    throw new UnrecognizableBluetoothDataException("Could not resolve File", e1);
                }
            }
            m_FileWriter.println("");
            try {
                m_FileReader = new LineNumberReader(new FileReader(m_Data));
            } catch (FileNotFoundException e1) {
                throw new UnrecognizableBluetoothDataException("Could not resolve File", e1);
            }
        }
        if (!leaveReadStreamOpen && m_FileReader != null) {
            try {
                m_FileReader.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close File, destroying reference anyway", e);
            }
            m_FileReader = null;
        }
        if (!leaveWriteStreamOpen && m_FileWriter != null) {
            m_FileWriter.close();
            m_FileWriter = null;
        }
    }
}
