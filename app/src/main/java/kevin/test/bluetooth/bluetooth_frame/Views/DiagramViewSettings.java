package kevin.test.bluetooth.bluetooth_frame.Views;

import android.graphics.Color;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author kevin
 * @version 0.0
 **/
public class DiagramViewSettings implements Parcelable {
    public static final int DEFAULT_FRAME_COLOR = Color.BLACK;
    public static final Style DEFAULT_FRAME_STYLE = Style.STROKE;
    public static final int DEFAULT_FRAME_STROKESIZE = 2;
    public static final int DEFAULT_FRAME_TEXTSIZE = 25;

    public static final int DEFAULT_GRAPH_COLOR = Color.RED;
    public static final Style DEFAULT_GRAPH_STYLE = Style.STROKE;
    public static final int DEFAULT_GRAPH_STROKESIZE = 5;
    public static final int DEFAULT_GRAPH_TEXTSIZE = 25;

    public static final int DEFAULT_CURSOR_COLOR = Color.BLUE;
    public static final Style DEFAULT_CURSOR_STYLE = Style.STROKE;
    public static final int DEFAULT_CURSOR_STROKESIZE = 3;
    public static final int DEFAULT_CURSOR_TEXTSIZE = 25;


    private static final String FRAME_COLOR = "frame color";
    private static final String FRAME_STYLE = "frame style";
    private static final String FRAME_STROKESIZE = "frame stroke size";
    private static final String FRAME_TEXTSIZE = "frame text size";

    private static final String GRAPH_COLOR = "graph color";
    private static final String GRAPH_STYLE = "graph style";
    private static final String GRAPH_STROKESIZE = "graph stroke size";
    private static final String GRAPH_TEXTSIZE = "graph text size";

    private static final String CURSOR_COLOR = "cursor color";
    private static final String CURSOR_STYLE = "cursor style";
    private static final String CURSOR_STROKESIZE = "cursor stroke size";
    private static final String CURSOR_TEXTSIZE = "cursor text size";


    //These Settings describe the View of the DiagramFrame
    private int pm_frameColor;
    private Style pm_frameStyle;
    private int pm_frameStrokeSize;
    private int pm_frameTextSize;
    //These Settings describe the View of the Graph-Lines
    private int pm_graphColor;
    private Style pm_graphStyle;
    private int pm_graphStrokeSize;
    private int pm_graphTextSize;
    //These Settings describe the View if the Cursor
    private int pm_cursorColor;
    private Style pm_cursorStyle;
    private int pm_cursorStrokeSize;
    private int pm_cursorTextSize;


    private DiagramViewSettings(int frameColor, Style frameStyle, int frameStrokeSize, int frameTextSize,
                                int graphColor, Style graphStyle, int graphStrokeSize, int graphTextSize,
                                int cursorColor, Style cursorStyle, int cursorStrokeSize, int cursorTextSize) {
        pm_cursorColor = cursorColor;
        pm_cursorStrokeSize = cursorStrokeSize;
        pm_cursorStyle = cursorStyle;
        pm_cursorTextSize = cursorTextSize;
        pm_frameColor = frameColor;
        pm_frameStrokeSize = frameStrokeSize;
        pm_frameStyle = frameStyle;
        pm_frameTextSize = frameTextSize;
        pm_graphColor = graphColor;
        pm_graphStrokeSize = graphStrokeSize;
        pm_graphStyle = graphStyle;
        pm_graphTextSize = graphTextSize;
    }

    private DiagramViewSettings(Bundle args) {
        pm_cursorColor = args.getInt(CURSOR_COLOR);
        pm_cursorStrokeSize = args.getInt(CURSOR_STROKESIZE);
        pm_cursorTextSize = args.getInt(CURSOR_TEXTSIZE);
        pm_frameColor = args.getInt(FRAME_COLOR);
        pm_frameStrokeSize = args.getInt(FRAME_STROKESIZE);
        pm_frameTextSize = args.getInt(FRAME_TEXTSIZE);
        pm_graphColor = args.getInt(GRAPH_COLOR);
        pm_graphStrokeSize = args.getInt(GRAPH_STROKESIZE);
        pm_graphTextSize = args.getInt(GRAPH_TEXTSIZE);
        pm_cursorStyle = createStyleFromInt(args.getInt(CURSOR_STYLE));
        pm_frameStyle = createStyleFromInt(args.getInt(FRAME_STYLE));
        pm_graphStyle = createStyleFromInt(args.getInt(GRAPH_STYLE));
    }

    private DiagramViewSettings(Parcel toBeCreatedFrom) {
        this(toBeCreatedFrom.readBundle());
    }

    private Style createStyleFromInt(int toCreateFrom) {
        switch (toCreateFrom) {
            case (0): {
                return Style.FILL;
            }
            case (1): {
                return Style.FILL_AND_STROKE;
            }
            case (2): {
                return Style.STROKE;
            }
        }
        return Style.STROKE;
    }

    private int createIntFromStyle(Style toCreateFrom) {
        switch (toCreateFrom) {
            case FILL:
                return 0;
            case FILL_AND_STROKE:
                return 1;
            case STROKE:
                return 2;
        }
        return -1;
    }

    public static DiagramViewSettings getDefaultSettings() {
        return new DiagramViewSettings(DEFAULT_FRAME_COLOR, DEFAULT_FRAME_STYLE, DEFAULT_FRAME_STROKESIZE, DEFAULT_FRAME_TEXTSIZE,
                DEFAULT_GRAPH_COLOR, DEFAULT_GRAPH_STYLE, DEFAULT_GRAPH_STROKESIZE, DEFAULT_GRAPH_TEXTSIZE,
                DEFAULT_CURSOR_COLOR, DEFAULT_CURSOR_STYLE, DEFAULT_CURSOR_STROKESIZE, DEFAULT_CURSOR_TEXTSIZE);
    }

    public int getFrameColor() {
        return pm_frameColor;
    }

    public void setFrameColor(int pm_frameColor) {
        this.pm_frameColor = pm_frameColor;
    }

    public Style getFrameStyle() {
        return pm_frameStyle;
    }

    public void setFrameStyle(Style pm_frameStyle) {
        this.pm_frameStyle = pm_frameStyle;
    }

    public int getFrameStrokeSize() {
        return pm_frameStrokeSize;
    }

    public void setFrameStrokeSize(int pm_frameStrokeSize) {
        this.pm_frameStrokeSize = pm_frameStrokeSize;
    }

    public int getFrameTextSize() {
        return pm_frameTextSize;
    }

    public void setFrameTextSize(int pm_frameTextSize) {
        this.pm_frameTextSize = pm_frameTextSize;
    }

    public int getGraphColor() {
        return pm_graphColor;
    }

    public void setGraphColor(int pm_graphColor) {
        this.pm_graphColor = pm_graphColor;
    }

    public Style getGraphStyle() {
        return pm_graphStyle;
    }

    public void setGraphStyle(Style pm_graphStyle) {
        this.pm_graphStyle = pm_graphStyle;
    }

    public int getGraphStrokeSize() {
        return pm_graphStrokeSize;
    }

    public void setGraphStrokeSize(int pm_graphStrokeSize) {
        this.pm_graphStrokeSize = pm_graphStrokeSize;
    }

    public int getGraphTextSize() {
        return pm_graphTextSize;
    }

    public void setGraphTextSize(int pm_graphTextSize) {
        this.pm_graphTextSize = pm_graphTextSize;
    }

    public int getCursorColor() {
        return pm_cursorColor;
    }

    public void setCursorColor(int pm_cursorColor) {
        this.pm_cursorColor = pm_cursorColor;
    }

    public Style getCursorStyle() {
        return pm_cursorStyle;
    }

    public void setCursorStyle(Style pm_cursorStyle) {
        this.pm_cursorStyle = pm_cursorStyle;
    }

    public int getCursorStrokeSize() {
        return pm_cursorStrokeSize;
    }

    public void setCursorStrokeSize(int pm_cursorStrokeSize) {
        this.pm_cursorStrokeSize = pm_cursorStrokeSize;
    }

    public int getCursorTextSize() {
        return pm_cursorTextSize;
    }

    public void setCursorTextSize(int pm_cursorTextSize) {
        this.pm_cursorTextSize = pm_cursorTextSize;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see #CONTENTS_FILE_DESCRIPTOR
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(createToBundle());
    }

    public static final Parcelable.Creator<DiagramViewSettings> CREATOR
            = new Parcelable.Creator<DiagramViewSettings>() {
        public DiagramViewSettings createFromParcel(Parcel in) {
            return new DiagramViewSettings(in);
        }

        public DiagramViewSettings[] newArray(int size) {
            return new DiagramViewSettings[size];
        }
    };

    public Bundle createToBundle() {
        Bundle args = new Bundle();
        args.putInt(FRAME_COLOR, pm_frameColor);
        args.putInt(FRAME_TEXTSIZE, pm_frameTextSize);
        args.putInt(FRAME_STROKESIZE, pm_frameStrokeSize);
        args.putInt(GRAPH_COLOR, pm_graphColor);
        args.putInt(GRAPH_STROKESIZE, pm_graphStrokeSize);
        args.putInt(GRAPH_TEXTSIZE, pm_graphTextSize);
        args.putInt(CURSOR_STROKESIZE, pm_cursorStrokeSize);
        args.putInt(CURSOR_COLOR, pm_cursorColor);
        args.putInt(CURSOR_TEXTSIZE, pm_cursorTextSize);
        args.putInt(FRAME_STYLE, createIntFromStyle(pm_frameStyle));
        args.putInt(GRAPH_STYLE, createIntFromStyle(pm_graphStyle));
        args.putInt(CURSOR_STYLE, createIntFromStyle(pm_cursorStyle));
        return args;
    }

    public static DiagramViewSettings createFromBundle(Bundle toCreateFrom) {
        return new DiagramViewSettings(toCreateFrom);
    }
}