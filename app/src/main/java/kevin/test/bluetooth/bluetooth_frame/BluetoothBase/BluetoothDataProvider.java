package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author KI
 * @version 1.0a
 **/
public class BluetoothDataProvider {
    private static final String DATAFILE_NAME = "received_data";
    private static final String INDICATOR_TIME = "Date:";
    private static final String INDICATOR_TEMP = "Temperature:";
    private static final String INDICATOR_HUMID = "Humidity:";
    private static final String INDICATOR_SOIL = "Soil Moisture:";

    private static final String LOG_TAG = "Bluetooth-Data Provider";

    private Context m_callingContext;
    private File m_fileDirectory;
    private File m_Data;
    private PrintWriter m_FileWriter;
    private LineNumberReader m_FileReader;
    private DateFormat m_formatter;

    public BluetoothDataProvider(Context context) {
        if (context != null) {
            m_callingContext = context;
            m_fileDirectory = context.getFilesDir();
        } else {
            throw new IllegalArgumentException("Can't manage Data without an context");
        }
        m_Data = new File(m_fileDirectory, DATAFILE_NAME);
        m_formatter = DateFormat.getDateInstance();
    }

    public List<DataSet> readData(boolean leaveInputStreamOpen) throws UnrecognizableBluetoothDataException {
        if (m_FileReader == null) {
            try {
                m_FileReader = new LineNumberReader(new FileReader(m_Data));
            } catch (FileNotFoundException e) {
                throw new UnrecognizableBluetoothDataException("Could not resolve File", e);
            }
        }
        List<DataSet> readData = new LinkedList<>();
        try {
            //m_FileReader.reset();
            String read;
            do {
                read = m_FileReader.readLine();
                if (read != null && read.equalsIgnoreCase("####")) {
                    Date date = readDate(m_FileReader, INDICATOR_TIME);
                    BigDecimal temperature = readValue(m_FileReader, INDICATOR_TEMP);
                    BigDecimal humidity = readValue(m_FileReader, INDICATOR_HUMID);
                    BigDecimal soilMoisture = readValue(m_FileReader, INDICATOR_SOIL);
                    readData.add(new DataSet(date, temperature, humidity, soilMoisture));
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

    public void writeData(List<DataSet> toWrite, boolean leaveOutputStreamOpen) {
        if (m_FileWriter == null) {
            try {
                m_FileWriter = new PrintWriter(new FileWriter(m_Data), true);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not resolve File Writer", e);
                return;
            }
        }
        for (DataSet data :
                toWrite) {
            m_FileWriter.println("####");
            writeDate(m_FileWriter, INDICATOR_TIME, data.getTimeStamp());
            writeValue(m_FileWriter, INDICATOR_TEMP, data.getTemperature());
            writeValue(m_FileWriter, INDICATOR_HUMID, data.getHumidity());
            writeValue(m_FileWriter, INDICATOR_SOIL, data.getSoilMoisture());
        }
        if (!leaveOutputStreamOpen) {
            m_FileWriter.close();
            m_FileWriter = null;
        }
    }

    public List<DataSet> readData() throws UnrecognizableBluetoothDataException {
        return readData(false);
    }

    public void writeData(List<DataSet> toWrite) {
        writeData(toWrite, false);
    }

    private BigDecimal readValue(LineNumberReader reader, String indicator) throws IOException, UnrecognizableBluetoothDataException {
        String read = reader.readLine();
        if (read != null && read.contains(indicator)) {
            String[] parts = read.split(indicator);
            return new BigDecimal(parts[1]);
        } else {
            throw new UnrecognizableBluetoothDataException("File does not Match read requirements, read:" + read);
        }
    }

    private Date readDate(LineNumberReader reader, String indicator) throws IOException, UnrecognizableBluetoothDataException {
        String read = reader.readLine();
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

    private void writeValue(PrintWriter writer, String indicator, BigDecimal toWrite) {
        writer.print(indicator);
        writer.println(toWrite.toString());
    }

    private void writeDate(PrintWriter writer, String indicator, Date toWrite) {
        writer.print(indicator);
        writer.println(m_formatter.format(toWrite));
    }
}
