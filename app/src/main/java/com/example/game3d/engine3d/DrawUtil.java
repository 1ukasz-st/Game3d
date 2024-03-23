package com.example.game3d.engine3d;

import static com.example.game3d.engine3d.Util.GAMEFONT;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

public class DrawUtil {
    private static final Paint paint = new Paint();
    private static final Paint strokeP = new Paint();
    private static final Paint fillP = new Paint();

    static {
        strokeP.setStyle(Paint.Style.STROKE);
        fillP.setStyle(Paint.Style.FILL);
    }

    public static void drawText(Canvas canvas, String text, float x, float y, float fontSize, int color) {
        int pc = paint.getColor();
        float pts = paint.getTextSize();
        Typeface ptf = paint.getTypeface();
        paint.setColor(color);
        paint.setTextSize(fontSize);
        paint.setTypeface(GAMEFONT);
        canvas.drawText(text, x, y, paint);
        paint.setColor(pc);
        paint.setTextSize(pts);
        paint.setTypeface(ptf);
    }

    public static float drawCenteredText(Canvas canvas, String text, float cx, float cy, float width, int color) {
        int originalColor = paint.getColor();
        float originalTextSize = paint.getTextSize();
        Typeface originalTypeface = paint.getTypeface();
        paint.setColor(color);
        paint.setTypeface(GAMEFONT);
        float testTextSize = 48f; // Starting with an arbitrary text size
        paint.setTextSize(testTextSize);
        android.graphics.Rect bounds = new android.graphics.Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float desiredTextSize = testTextSize * width / bounds.width();
        paint.setTextSize(desiredTextSize);
        paint.getTextBounds(text, 0, text.length(), bounds);
        float textHeight = bounds.height();
        float x = cx - bounds.width() / 2f;
        float y = cy + textHeight / 2f;
        canvas.drawText(text, x, y, paint);
        paint.setColor(originalColor);
        paint.setTextSize(originalTextSize);
        paint.setTypeface(originalTypeface);
        return textHeight;
    }

    public static void drawRect(Canvas canvas, float x1, float y1, float x2, float y2, int color) {
        int lc = strokeP.getColor();
        strokeP.setColor(color);
        canvas.drawRect(x1, y1, x2, y2, strokeP);
        strokeP.setColor(lc);
    }

    public static void fillRect(Canvas canvas, float x1, float y1, float x2, float y2, int color) {
        int lc = fillP.getColor();
        fillP.setColor(color);
        canvas.drawRect(x1, y1, x2, y2, fillP);
        fillP.setColor(lc);
    }
}
