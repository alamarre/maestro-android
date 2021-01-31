package ca.omny.videos.maestro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Overlay extends View {

    public Overlay(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // put your drawing commands here
        Paint myPaint = new Paint();
        myPaint.setColor(Color.argb(100, 255, 0, 0));
        canvas.drawRect(canvas.getClipBounds(), myPaint);
    }

}
