package kevin.test.bluetooth.bluetooth_frame.DiagramManaging;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.List;

/**
 * @author NB
 * @version 1.1
 */

public class Diagram extends View {

    private DiagramSettings settings;
    private DiagramViewSettings viewSettings;
    private List<Integer> werte;
    private DiagramValueViewer textViewer;

    private int center;

    private float yMeasurement;
    private float xMeasurement;

    private float xClickPosition = 0;

    private Context context;

    private Paint paint = new Paint();


    public Diagram(Context con, List<Integer> messwerte, DiagramSettings settings) {
        super(con);
        context = con;
        setSettings(settings);
        werte = messwerte;
        //xMeasurement = settings.getWidth() / werte.size();

        yMeasurement = settings.getHeight() / (settings.getMax() - settings.getMin());

        if (werte.size() > 0)
            xMeasurement = settings.getWidth() / werte.size();  // TODO passende Lösung anstatt dieser Notlösung
        else xMeasurement = 1;

    }




    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);  //always call the super method...


        paint.setColor(viewSettings.getFrameColor());
        paint.setStrokeWidth(viewSettings.getFrameStrokeSize());
        paint.setStyle(viewSettings.getFrameStyle());
        paint.setTextSize(viewSettings.getFrameTextSize());

        int zero;

        center = Math.round(settings.getHeight() / 2);

        if (settings.getMin() < 0 && settings.getMax() > 0) {
            //0 Grad Orientierungslinie
            zero = Math.round(center + ((settings.getMax() + settings.getMin()) / 2) * yMeasurement);
            canvas.drawLine(0, zero, settings.getWidth(), zero, paint);
            canvas.drawText("0" + settings.getUnit(), paint.getTextSize() / 2, zero + paint.getTextSize(), paint);

            //mittlere Orientierungslinie
            canvas.drawLine(0, center, settings.getWidth(), center, paint);
            canvas.drawText(((settings.getMax() + settings.getMin()) / 2) + settings.getUnit(), paint.getTextSize() / 2, center + paint.getTextSize(), paint);


            //oberste Orientierungslinie
            canvas.drawLine(0, center - ((settings.getMax() - ((settings.getMax() + settings.getMin()) / 2)) * yMeasurement), settings.getWidth(), center - ((settings.getMax() - ((settings.getMax() + settings.getMin()) / 2)) * yMeasurement), paint);
            canvas.drawText(settings.getMax() + settings.getUnit(), paint.getTextSize() / 2, center - ((settings.getMax() - ((settings.getMax() + settings.getMin()) / 2)) * yMeasurement) + paint.getTextSize(), paint);


            //unterste Orientierungslinie
            canvas.drawLine(0, center + ((settings.getMax() - ((settings.getMax() + settings.getMin()) / 2)) * yMeasurement), settings.getWidth(), center + ((settings.getMax() - ((settings.getMax() + settings.getMin()) / 2)) * yMeasurement), paint);
            canvas.drawText(settings.getMin() + settings.getUnit(), paint.getTextSize() / 2, center + ((settings.getMax() - ((settings.getMax() + settings.getMin()) / 2)) * yMeasurement) - paint.getTextSize(), paint);


            paint.setColor(viewSettings.getGraphColor());
            paint.setStrokeWidth(viewSettings.getGraphStrokeSize());
            paint.setStyle(viewSettings.getGraphStyle());
            for (int i = 0; i < werte.size() - 1; i++) {
                canvas.drawLine(i * xMeasurement, toYPosition(werte.get(i)), (i + 1) * xMeasurement, toYPosition(werte.get(i + 1)), paint);
                canvas.drawCircle(i * xMeasurement, toYPosition(werte.get(i)), paint.getStrokeWidth() * 1.5f, paint);
            }

            paint.setColor(viewSettings.getCursorColor());
            paint.setStrokeWidth(viewSettings.getCursorStrokeSize());
            paint.setStyle(viewSettings.getCursorStyle());
            canvas.drawLine(xClickPosition, 0, xClickPosition, settings.getHeight(), paint);
        }
    }

    //Bei Berühren des Diagramms soll ein "Cursor" verschoben werden, mit dem die genauen Werte angezeigt werden
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                xClickPosition = x;
                this.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(toIndex(x) < werte.size()) {
                    xClickPosition = toXPosition(toIndex(x));
                    this.invalidate();
                    String toShow = toIndex(x) + "s | " + werte.get(toIndex(x)) + settings.getUnit();
                    if (textViewer != null) {
                        textViewer.onShowCursorValue(toShow);
                    } else {
                        Toast.makeText(context, toShow, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                else break;
        }
        return true;
    }

    public void setDiagramValueViewer(DiagramValueViewer textViewer) {
        this.textViewer = textViewer;
    }

    public DiagramSettings getSettings() {
        return this.settings;
    }
    public void setSettings(DiagramSettings settings) {
        this.settings = settings;
        viewSettings = this.settings.getViewSettings();
    }

    public void updateViewSettings() {
        viewSettings = this.settings.getViewSettings();
    }

    private int toIndex(float xPosition) {
        return Math.round(xPosition/xMeasurement);
    }

    private float toYPosition(int wert) {
        return center - ((wert - (settings.getMax() + settings.getMin()) / 2) * yMeasurement);
    }

    private float toXPosition(int index) {
        return index*xMeasurement;
    }


    public void addWert(int neu) {
        if(werte.size()>0)werte.remove(0);
        werte.add(neu);
        invalidate(); //TODO ?
    }

    public interface DiagramValueViewer {
        public void onShowCursorValue(String toShow);
    }


}
