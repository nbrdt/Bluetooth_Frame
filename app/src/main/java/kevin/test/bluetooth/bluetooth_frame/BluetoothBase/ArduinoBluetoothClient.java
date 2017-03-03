package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;
/**
 * This Interface defines specific Methods to be implemented by an subclass of any BluetoothClient
 * The Arduino BluetoothClient should contain some kind of Timer which decodes in specified Intervals data Send by the Arduino
 * Received Data is specified as a List, to provide platform independency and a possibility to change internal List represenations,
 * which might be necessary to increase System Perfomance
 *
 *@author KI
 *@version 1.0
 **/
import java.util.List;

public interface ArduinoBluetoothClient extends BluetoothClient {
  /**
   * Returns all identified Data since the last Timer-Event fired, but will not clear the internal List.
   * The internal representation will be copied, so that you do not need to worry about Data being modified in an unexpected moment.
   * This therefore decreases Performance to at least the Number of Dataset's, which has been recieved since the last call of clearReceivedData.
   * Consequently, it is recommended to Read and clear the Data in regular intervals.
   *
   * @return List of BluetoothDataSet which represents the received Data, in case of no received Data, this will return a null Object reference
   **/
  public List<BluetoothDataSet> getReceivedData();

  /**
   * Clears the internal representation of the receivedData List.
   * Warning: it is not checked, whether or not the Data has been outputed or not, you might therefore lose Data, if you don't call the receive Data Method before.
   *
   * Because of heavy workload required by the getReceivedData Method,m it is recommended to Read and clear the Data in regular intervals.
  **/
  public void clearReceivedData();

  /**
   * Sets The Receiving Timer to the given scheduleRate.
   */
  public void setTimer(long scheduleRate);

  /**
   * returns the current schedule Rate of the Receiving Timer
   *
   * @return the current receive Rate as a long
   */
  public long getTimerRate();

  /**
   * Sets the Receive Listener, to be notified on Receive Events.
   */
  public void setReceiveListener(ArduinoBluetoothClient.OnReceiveListener listener);

  /**
   * This interface is called when decoding Arduino Messages.
   */
  public interface OnReceiveListener {
    /**
     * called when Messages were read, but not yet decoded.
     */
    public void onPreReceive();

    /**
     * called when Messages were read, decoded and new ReceivedData was added.
     */
    public void onPostReceive();
  }

  ;
}
