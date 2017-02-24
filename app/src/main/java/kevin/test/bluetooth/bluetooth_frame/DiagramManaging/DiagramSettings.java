package kevin.test.bluetooth.bluetooth_frame.DiagramManaging;

/**
 * @author KI
 * @version 0.1
 **/
public class DiagramSettings implements Comparable<DiagramSettings> {
    public static final String nameless = "!:%:nameless:%:!";

    private DiagramViewSettings m_viewSettings;
    private String m_name;
    private String m_unit;
    private int m_height;
    private int m_width;
    private int m_min;
    private int m_max;

    public DiagramSettings(DiagramViewSettings viewSettings, String name, String unit, int height, int width, int min, int max) {
        m_viewSettings = viewSettings;
        m_name = name;
        m_unit = unit;
        m_height = height;
        m_width = width;
        m_min = min;
        m_max = max;
    }

    public DiagramSettings(String name, String unit, int height, int width, int min, int max) {
        this(DiagramViewSettings.getDefaultSettings(), name, unit, height, width, min, max);
    }

    public DiagramViewSettings getViewSettings() {
        return m_viewSettings;
    }

    public void setViewSettings(DiagramViewSettings m_viewSettings) {
        this.m_viewSettings = m_viewSettings;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String m_name) {
        this.m_name = m_name;
    }

    public String getUnit() {
        return m_unit;
    }

    public void setUnit(String m_unit) {
        this.m_unit = m_unit;
    }

    public int getHeight() {
        return m_height;
    }

    public void setHeight(int m_height) {
        this.m_height = m_height;
    }

    public int getWidth() {
        return m_width;
    }

    public void setWidth(int m_width) {
        this.m_width = m_width;
    }

    public int getMin() {
        return m_min;
    }

    public void setMin(int m_min) {
        this.m_min = m_min;
    }

    public int getMax() {
        return m_max;
    }

    public void setMax(int m_max) {
        this.m_max = m_max;
    }


    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param toCompare the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(DiagramSettings toCompare) {
        return getName().compareTo(toCompare.getName());
    }
}
