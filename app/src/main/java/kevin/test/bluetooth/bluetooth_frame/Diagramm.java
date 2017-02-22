package kevin.test.bluetooth.bluetooth_frame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Niklas on 23.01.2017.
 */

public class Diagramm extends View {

    private String einheit;
    private int height;
    private int width;
    private ArrayList<Integer> werte;

    private int minWert;
    private int maxWert;

    private int center;

    private float yMeasurement;
    private float xMeasurement;

    private float xClickPosition = 0;

    private Context context;

    private Paint paint = new Paint();




    public Diagramm(Context con, int hei, int wid, ArrayList<Integer> messwerte, int min, int max, String einh) {
        super(con);
        context = con;
        einheit = einh;
        height = hei;
        width = wid;
        werte = messwerte;
        minWert = min;
        maxWert = max;

        yMeasurement = height/(maxWert-minWert);
        xMeasurement = width/werte.size();
    }

    public void onDraw(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(25);

        int zero;

        center = Math.round(height/2);

        if(minWert < 0 && maxWert > 0) {
            //0 Grad Orientierungslinie
            zero = Math.round(center+((maxWert+minWert)/2)*yMeasurement);
            canvas.drawLine(0,zero, width, zero, paint);
            canvas.drawText("0"+einheit, paint.getTextSize()/2, zero+paint.getTextSize(), paint);
        }

        canvas.drawLine(0, center, width, center, paint);
        canvas.drawText(((maxWert + minWert) / 2) + einheit, paint.getTextSize() / 2, center + paint.getTextSize(), paint);



        //oberste Orientierungslinie
        canvas.drawLine(0,center-((maxWert-((maxWert+minWert)/2))*yMeasurement), width, center-((maxWert-((maxWert+minWert)/2))*yMeasurement), paint);
        canvas.drawText(maxWert+einheit, paint.getTextSize()/2, center-((maxWert-((maxWert+minWert)/2))*yMeasurement)+paint.getTextSize(), paint);

        //unterste Orientierungslinie
        canvas.drawLine(0,center+((maxWert-((maxWert+minWert)/2))*yMeasurement), width, center+((maxWert-((maxWert+minWert)/2))*yMeasurement), paint);
        canvas.drawText(minWert+einheit, paint.getTextSize()/2, center+((maxWert-((maxWert+minWert)/2))*yMeasurement)-paint.getTextSize(), paint);


        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        for(int i = 0; i < werte.size()-1; i++) {
            canvas.drawLine(i*xMeasurement, toYPosition(werte.get(i)), (i+1)*xMeasurement, toYPosition(werte.get(i+1)), paint);
            canvas.drawCircle(i*xMeasurement, toYPosition(werte.get(i)), paint.getStrokeWidth()*1.5f, paint);
        }

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(xClickPosition, 0, xClickPosition, height, paint);
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
                    Toast.makeText(context, toIndex(x) + "s | " + werte.get(toIndex(x)) + einheit, Toast.LENGTH_SHORT).show();
                    break;
                }
                else break;
        }
        return true;
    }


    private int toIndex(float xPosition) {
        return Math.round(xPosition/xMeasurement);
    }

    private float toYPosition(int wert) {
        return center-((wert-(maxWert+minWert)/2)*yMeasurement);
    }

    private float toXPosition(int index) {
        return index*xMeasurement;
    }


    public void addWert(int neu) {
        werte.remove(0);
        werte.add(neu);
        invalidate();
    }


}
