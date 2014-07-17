package ntpclock.kamalan.com.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Hesam on 16/07/14
 */
public class MyAnalogClock extends View {

    private final float x;
    private final float y;
    private final int r = 180;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Calendar mCalendar;

    public MyAnalogClock(Context context, float x, float y, Date date) {
        super(context);
        this.x = x;
        this.y = y;
        this.mCalendar = Calendar.getInstance();
        this.mCalendar.setTime(date);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        mPaint.setColor(0xFF3300);
//        mPaint.setStrokeWidth(4);
//        canvas.drawCircle(x, y, r, mPaint);
//        canvas.save();

        float sec  = (float) mCalendar.get(Calendar.SECOND);
        float min  = (float) mCalendar.get(Calendar.MINUTE);
        float hour = (float) mCalendar.get(Calendar.HOUR) + min / 60.0f;

        mPaint.setColor(0xFFFF0000);
        mPaint.setStrokeWidth(5.0f);
        canvas.drawLine(x, y, (float)(x+(r-15)*Math.cos(Math.toRadians((hour / 12.0f * 360.0f)-90f))), (float)(y+(r-10)*Math.sin(Math.toRadians((hour / 12.0f * 360.0f)-90f))), mPaint);
        canvas.save();
        mPaint.setColor(0xFF00FF00);
        mPaint.setStrokeWidth(5.0f);
        canvas.drawLine(x, y, (float)(x+r*Math.cos(Math.toRadians((min / 60.0f * 360.0f)-90f))), (float)(y+r*Math.sin(Math.toRadians((min / 60.0f * 360.0f)-90f))), mPaint);
        canvas.save();
        mPaint.setColor(0xFF0000FF);
        mPaint.setStrokeWidth(5.0f);
        canvas.drawLine(x, y, (float)(x+(r+10)*Math.cos(Math.toRadians((sec / 60.0f * 360.0f)-90f))), (float)(y+(r+15)*Math.sin(Math.toRadians((sec / 60.0f * 360.0f)-90f))), mPaint);

    }
}
