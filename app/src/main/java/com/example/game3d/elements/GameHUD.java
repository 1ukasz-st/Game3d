package com.example.game3d.elements;

import static com.example.game3d.GameView.DEFAULT_SCR_Y;
import static com.example.game3d.GameView.SCR_Y;
import static com.example.game3d.engine3d.Object3D.FC;
import static com.example.game3d.engine3d.Object3D.FCS;
import static com.example.game3d.engine3d.Object3D.loadFromFile;
import static com.example.game3d.engine3d.Object3D.moveToScreen;
import static com.example.game3d.engine3d.Object3D.project;
import static com.example.game3d.engine3d.Util.GAMEFONT;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PI;
import static com.example.game3d.engine3d.Util.Rect;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.VXS;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.blue;
import static com.example.game3d.engine3d.Util.green;
import static com.example.game3d.engine3d.Util.multBrightness;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.red;
import static com.example.game3d.engine3d.Util.roundTo;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Pair;

import com.example.game3d.GameView;
import com.example.game3d.engine3d.FixedMaxSizeDeque;
import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Object3D.Face;
import com.example.game3d.engine3d.Util.Vector;

import java.io.IOException;
import java.util.ArrayList;

public class GameHUD {
    private static final int MAX_FEATHERS = 64;
    private static final int iconW = 40, iconT = 20, iconH = 120;
    private static final float triangleWidth = 27, triangleHeight = 25;
    private static Vector[] FEATHER_ICON_VERTS, BOTTLE_ICON_VERTS;
    private static Face[] FEATHER_ICON_FACES, BOTTLE_ICON_FACES;
    private final float markerWidth = 40, markerHeight = 200;
    private final GameView game;
    private final Paint strokeP = new Paint();
    private final Paint fillP = new Paint();
    private final Paint paint = new Paint();
    private final Path reservedPath = new Path();
    private final FixedMaxSizeDeque<Object3D> icons = new FixedMaxSizeDeque<>(MAX_FEATHERS);
    private final GearIcon gearIcon;
    private final Object3D arrow;
    private final Object3D iconFeather;
    private final Object3D iconBottle;
    private final int iconX = 60;
    private final int iconY = 350;
    private final long airTimeTime = 0;
    public Rect pauseRect, gearRect;
    public Rect startRect = new Rect(SCR_W * 0.5f - 200 - 10, SCR_H * 0.5f - 100 * 0.5f - 10, SCR_W * 0.5f + 200 + 10, SCR_H * 0.5f + 100 * 0.5f + 10);
    Path path = new Path();

    public GameHUD(GameView gameView) {
        this.game = gameView;
        strokeP.setStyle(Paint.Style.STROKE);
        fillP.setStyle(Paint.Style.FILL);

        iconFeather = new Object3D(FEATHER_ICON_VERTS, FEATHER_ICON_FACES) {
            @Override
            protected void extraInit() {
                facesSorted = false;
                oneColorAndFace = true;
                is_obs = true;
            }
        };
        //iconFeather.move(VX(iconX - SCR_W / 2, 0, iconY - SCR_H / 2));

        iconBottle = new Object3D(BOTTLE_ICON_VERTS, BOTTLE_ICON_FACES) {
            @Override
            protected void extraInit() {
                facesSorted = true;
                is_obs = true;
            }

            @Override
            protected boolean faceSkipped(ObjectFace fc) {
                return pointAndPlanePosition(vertex(fc.inds[0]), vertex(fc.inds[1]), vertex(fc.inds[2]), OBS) == 1;
            }
        };
        // iconBottle.move(VX(10 + 2*iconX - SCR_W / 2, 0, iconY - SCR_H / 2));
        arrow = new Object3D(VXS(
                VX(-iconW / 4.0f, SCR_Y - iconT / 2.0f, -iconH / 2.0f), // 0
                VX(iconW / 4.0f, SCR_Y - iconT / 2.0f, -iconH / 2.0f),   // 1
                VX(iconW / 4.0f, SCR_Y - iconT / 2.0f, iconH / 10.0f),   // 2
                VX(iconW / 2.0f, SCR_Y - iconT / 2.0f, iconH / 10.0f),   // 3
                VX(0, SCR_Y - iconT / 2.0f, iconH / 2.0f),             // 4
                VX(-iconW / 2.0f, SCR_Y - iconT / 2.0f, iconH / 10.0f),  // 5
                VX(-iconW / 4.0f, SCR_Y - iconT / 2.0f, iconH / 10.0f)  // 6

        ), FCS(
                FC(Color.RED, Color.RED, 0, 1, 2, 3, 4, 5, 6)
        )) {
            @Override
            protected void extraInit() {
                facesSorted = false;
                oneColorAndFace = true;
                is_obs = true;
            }
        };
        arrow.move(VX(SCR_W / 2 - iconW - 20, 0, -SCR_H / 2 + iconY));

        gearIcon = new GearIcon();
    }    private final float barIndentXLeft = 30, barIndentXRight = 125, jumpBarIndentY = 100, barMaxWidth = SCR_W - barIndentXLeft - barIndentXRight, barHeight = 80, timeBarIndentY = 100 + barHeight + 10;

    public static void ADD_FEATHER_ICON_ASSETS() throws IOException {
        Pair<Vector[], Face[]> data = loadFromFile("feather.obj", Color.CYAN, Color.CYAN, VX(0, DEFAULT_SCR_Y, 0), iconW, iconT, iconH, 0, 0, 0);
        FEATHER_ICON_VERTS = data.first;
        FEATHER_ICON_FACES = data.second;
    }

    public static void ADD_BOTTLE_ICON_ASSETS() throws IOException {
        Pair<Vector[], Face[]> data = loadFromFile("vodka.obj", Color.rgb(255, 0, 255), Color.WHITE, VX(0, DEFAULT_SCR_Y, 0), iconW, iconW, iconH * 0.9f, 0, -PI / 2, 0);
        BOTTLE_ICON_VERTS = data.first;
        BOTTLE_ICON_FACES = data.second;
    }

    private void fillTriangle(Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        reservedPath.rewind();
        reservedPath.moveTo(x1, y1);
        reservedPath.lineTo(x2, y2);
        reservedPath.lineTo(x3, y3);
        reservedPath.close();
        fillP.setColor(color);
        canvas.drawPath(reservedPath, fillP);
    }

    private void drawMarker(Canvas canvas, float xc, float yc) {
        float[] x = new float[]{
                xc - markerWidth / 2,
                xc,
                xc + markerWidth / 2,
                xc + markerWidth / 2,
                xc,
                xc - markerWidth / 2,
        };
        float[] y = new float[]{
                yc - markerHeight / 4,
                yc - markerHeight * 0.4f,
                yc - markerHeight / 4,
                yc + markerHeight / 4,
                yc + markerHeight * 0.4f,
                yc + markerHeight / 4,
        };

        fillTriangle(canvas, xc - markerWidth / 2, yc - markerHeight * 0.4f, x[0], y[0], x[1], y[1], Color.BLACK);
        fillTriangle(canvas, xc + markerWidth / 2, yc - markerHeight * 0.4f, x[1], y[1], x[2], y[2], Color.BLACK);
        fillTriangle(canvas, xc + markerWidth / 2, yc + markerHeight * 0.4f, x[3], y[3], x[4], y[4], Color.BLACK);
        fillTriangle(canvas, xc - markerWidth / 2, yc + markerHeight * 0.4f, x[4], y[4], x[5], y[5], Color.BLACK);
        strokeP.setColor(Color.WHITE);
        reservedPath.rewind();
        reservedPath.moveTo(x[0], y[0]);
        for (int i = 1; i < x.length; ++i) {
            reservedPath.lineTo(x[i], y[i]);
        }
        reservedPath.close();
        canvas.drawPath(reservedPath, strokeP);

    }

    private void drawRect(Canvas canvas, float x1, float y1, float x2, float y2, int color) {
        int lc = strokeP.getColor();
        strokeP.setColor(color);
        canvas.drawRect(x1, y1, x2, y2, strokeP);
        strokeP.setColor(lc);
    }

    private void fillRect(Canvas canvas, float x1, float y1, float x2, float y2, int color) {
        int lc = fillP.getColor();
        fillP.setColor(color);
        canvas.drawRect(x1, y1, x2, y2, fillP);
        fillP.setColor(lc);
    }

    private void drawBarWithMarkersOutline(Canvas canvas, float x0, float y0, float x1, float y1, float progress, int outlineColor, int fillColor, Float... markerPositions) {

        fillRect(canvas, x0, y0, x0 + progress, y1, fillColor);

        path.rewind();
        path.moveTo(x0, y0);

        for (float pos : markerPositions) {
            path.lineTo(min(x1, max(x0, x0 + pos - triangleWidth / 2)), y0);
            path.lineTo(min(x1, max(x0, x0 + pos)), y0 + triangleHeight);
            path.lineTo(min(x1, max(x0, x0 + pos + triangleWidth / 2)), y0);
            fillTriangle(canvas,
                    min(x1, max(x0, x0 + pos - triangleWidth / 2)), y0,
                    min(x1, max(x0, x0 + pos)), y0 + triangleHeight,
                    min(x1, max(x0, x0 + pos + triangleWidth / 2)), y0,
                    Color.BLACK);
        }

        path.lineTo(x1, y0);
        path.lineTo(x1, y1);

        for (int i = markerPositions.length - 1; i >= 0; --i) {
            float pos = markerPositions[i];
            path.lineTo(min(x1, max(x0, x0 + pos + triangleWidth / 2)), y1);
            path.lineTo(min(x1, max(x0, x0 + pos)), y1 - triangleHeight);
            path.lineTo(min(x1, max(x0, x0 + pos - triangleWidth / 2)), y1);
            fillTriangle(canvas,
                    min(x1, max(x0, x0 + pos + triangleWidth / 2)), y1,
                    min(x1, max(x0, x0 + pos)), y1 - triangleHeight,
                    min(x1, max(x0, x0 + pos - triangleWidth / 2)), y1,
                    Color.BLACK);
        }

        path.lineTo(x0, y1);
        path.close();

        int lc = strokeP.getColor();
        strokeP.setColor(outlineColor);
        canvas.drawPath(path, strokeP);
        strokeP.setColor(lc);

    }

    public void drawText(Canvas canvas, String text, float x, float y, float fontSize, int color) {
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

    public float drawCenteredText(Canvas canvas, String text, float cx, float cy, float width, int color) {
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

    private void drawReset(Canvas canvas) {
        fillRect(canvas, 0, 0, game.getResetRectSize(), SCR_H, game.getTileColor());

    }

    public void draw(Canvas canvas) {
        if (game.isStarting()) {
            drawIntro(canvas);
            return;
        }
        if (game.isResetting()) {
            drawReset(canvas);
            return;
        }
        if (game.isAtSettings()) {
            drawSettings(canvas);
            return;
        }
        drawGameStats(canvas);


    }

    private void drawProgressBar(Canvas canvas, float progress, float x1, float y1, float x2, float y2, float mw, float mh, int outlineColor, int fillColor) {
        fillRect(canvas, x1, y1, x1 + (x2 - x1) * progress, y2, fillColor);
        drawRect(canvas, x1, y1, x2, y2, outlineColor);
        /*x2 -= mw/2;
        float mcx = x1 + (x2-x1)*progress;
        float mcy = (y1+y2)*0.5f;
        path.rewind();
        path.moveTo(mcx,y1-mh*0.5f);
        path.lineTo(mcx+mw*0.5f,y1);
        path.lineTo(mcx+mw*0.5f,y2);
        path.lineTo(mcx,y2+mh*0.5f);
        path.lineTo(mcx-mw*0.5f,y2);
        path.lineTo(mcx-mw*0.5f,y1);
        path.lineTo(mcx,y1-mh*0.5f);
        path.close();
        int lc = strokeP.getColor();
        int lc2 = fillP.getColor();
        strokeP.setColor(outlineColor);
        fillP.setColor(fillColor);
        canvas.drawPath(path,strokeP);
        canvas.drawPath(path,fillP);
        strokeP.setColor(lc);
        fillP.setColor(lc2);*/
    }

    public Rect getSensitivityRect() {
        return new Rect(barIndentXLeft + 450, jumpBarIndentY + 163f - 22, SCR_W - 70, jumpBarIndentY + 163f + 80 - 22);
    }

    public Rect getSensitivityMarkerRect() {
        float dx = getSensitivityRect().x1 + ((game.camSensitivity - 0.4f) / 1.1f) * (getSensitivityRect().x2 - getSensitivityRect().x1);
        float dy = (getSensitivityRect().y1 + getSensitivityRect().y2) * 0.5f;
        float bh = getSensitivityRect().y2 - getSensitivityRect().y1;
        return new Rect(dx - 20 - 20, dy - 60 - bh * 0.5f - 22, dx + 20 + 20, dy + 60 + bh * 0.5f - 22);
    }

    public Rect getFOVRect() {
        return new Rect(barIndentXLeft + 450, jumpBarIndentY + 163f + 160 - 22, SCR_W - 70, jumpBarIndentY + 163f + 160 + 80 - 22);
    }

    public Rect getFOVMarkerRect() {
        float dx = getFOVRect().x1 + (1.0f - (GameView.BASE_SCR_Y - 700) / 500) * (getFOVRect().x2 - getFOVRect().x1);
        float dy = (getFOVRect().y1 + getFOVRect().y2) * 0.5f;
        float bh = getFOVRect().y2 - getFOVRect().y1;
        return new Rect(dx - 20 - 20, dy - 35 - bh * 0.5f - 22, dx + 20 + 20, dy + 35 + bh * 0.5f - 22);
    }

    public Rect getResetRect() {
        return new Rect(gearRect.x1 - 210, gearRect.y1, gearRect.x1 - 40, gearRect.y1 + 85);
    }

    private void drawIntro(Canvas canvas) {
        drawCenteredText(canvas, "GRA LUKASZA :)", SCR_W * 0.5f, 400, SCR_W * 0.95f, Color.WHITE);
        drawCenteredText(canvas, "bottom text", SCR_W * 0.5f, 600, SCR_W * 0.5f, Color.WHITE);

        float h = drawCenteredText(canvas, "PLAY", SCR_W * 0.5f, SCR_H * 0.5f, 400, Color.WHITE);
        drawRect(canvas, SCR_W * 0.5f - 200 - 10, SCR_H * 0.5f - h * 0.5f - 10, SCR_W * 0.5f + 200 + 10, SCR_H * 0.5f + h * 0.5f + 10, Color.WHITE);
        startRect = new Rect(SCR_W * 0.5f - 200 - 10, SCR_H * 0.5f - h * 0.5f - 10, SCR_W * 0.5f + 200 + 10, SCR_H * 0.5f + h * 0.5f + 10);
        drawGearIcon(canvas);
    }

    private void drawSettings(Canvas canvas) {
        drawGearIcon(canvas);
        drawRect(canvas, getResetRect().x1, getResetRect().y1, getResetRect().x2, getResetRect().y2, Color.WHITE);
        drawText(canvas, "RESET", gearRect.x1 - 200, gearRect.y1 + 65, 60, Color.WHITE);


        drawText(canvas, "CAMERA", barIndentXLeft, jumpBarIndentY + 200 - 90, 90, Color.WHITE);

        drawText(canvas, "SENSITIVITY: " + roundTo(game.camSensitivity, 2), barIndentXLeft, jumpBarIndentY + 200, 60, Color.WHITE);
        drawProgressBar(canvas, (game.camSensitivity - 0.4f) / 1.1f, getSensitivityRect().x1, getSensitivityRect().y1, getSensitivityRect().x2, getSensitivityRect().y2, 40, 70, Color.WHITE, GameView.getColorTheme());

        drawText(canvas, "FOV: " + (700 + (1200 - GameView.BASE_SCR_Y)), barIndentXLeft, jumpBarIndentY + 200 + 160, 60, Color.WHITE);
        drawProgressBar(canvas, (1.0f - (GameView.BASE_SCR_Y - 700) / 500), getFOVRect().x1, getFOVRect().y1, getFOVRect().x2, getFOVRect().y2, 40, 70, Color.WHITE, GameView.getColorTheme());
        //  fillRect(canvas,getSensitivityMarkerRect().x1,getSensitivityMarkerRect().y1,getSensitivityMarkerRect().x2,getSensitivityMarkerRect().y2,Color.RED);
    }

    private void drawGearIcon(Canvas canvas) {
        gearIcon.move(VX((SCR_W / 2 - barIndentXRight / 2), -(DEFAULT_SCR_Y - SCR_Y), (-SCR_H / 2 + jumpBarIndentY + barHeight / 2)));

        gearIcon.draw(canvas);
        if (gearRect == null) {
            gearRect = new Rect(SCR_W - barIndentXRight / 2 - GearIcon.OUTER_RAD, jumpBarIndentY + barHeight / 2 - GearIcon.OUTER_RAD, SCR_W - barIndentXRight / 2 + GearIcon.OUTER_RAD, jumpBarIndentY + barHeight / 2 + GearIcon.OUTER_RAD);
        }

        float x1 = 12 + SCR_W / 2 + gearIcon.centroid().x - GearIcon.OUTER_RAD, y1 = SCR_H / 2 + gearIcon.centroid().z + GearIcon.OUTER_RAD + 12;
        float x2 = x1 + 60, y2 = y1 + barHeight - 10;

        if (pauseRect == null) {
            pauseRect = new Rect(x1, y1, x2, y2);
        }

        gearIcon.move(VX(-(SCR_W / 2 - barIndentXRight / 2), DEFAULT_SCR_Y - SCR_Y, -(-SCR_H / 2 + jumpBarIndentY + barHeight / 2)));


        gearIcon.invalidate();
    }

    private void drawGameStats(Canvas canvas) {

        float barWidthCurrPower = (game.getPlayer().jumpPower / game.getPlayer().maxJumpPower) * barMaxWidth;
        float barWithMinPower = (game.getPlayer().minJumpPower / game.getPlayer().maxJumpPower) * barMaxWidth;
        float barWithStrongPower = (game.getPlayer().strongJumpPower / game.getPlayer().maxJumpPower) * barMaxWidth;

        int barColor = multBrightness(game.getTileColor(), min(1.0f, 1.1f * game.getPlayer().jumpPower / game.getPlayer().maxJumpPower));

        drawBarWithMarkersOutline(canvas, barIndentXLeft, jumpBarIndentY, barIndentXLeft + barMaxWidth, jumpBarIndentY + barHeight, barWidthCurrPower, Color.WHITE, barColor, barWithMinPower, barWithStrongPower);

        ArrayList<Float> markers = new ArrayList<>();
        float dist = barMaxWidth / 2.2f, x = ((float) (game.getTimeToColorChange()) / game.getMaxTimeToColorChange()) * dist;
        while (x < barIndentXLeft + barMaxWidth) {
            markers.add(x);
            x += dist;
        }
        Float[] toArr = new Float[markers.size()];
        toArr = markers.toArray(toArr);
        drawBarWithMarkersOutline(canvas, barIndentXLeft, timeBarIndentY, barIndentXLeft + barMaxWidth, timeBarIndentY + barHeight, barWidthCurrPower, Color.WHITE, Color.BLACK, toArr);

        if (game.getPlayer().pressingFalling) {
            //drawArrow(canvas,SCR_W/2,200,650,400,Color.argb(50,red(game.getTileColor()),green(game.getTileColor()),blue(game.getTileColor())),Color.TRANSPARENT);
            arrow.draw(canvas);
            arrow.yaw += 0.025f;
            arrow.invalidate();
        }

    /*for(int i = 0; i< icons.size(); ++i) {
        icons.get(i).yaw += 0.02f;
        icons.get(i).draw(canvas);
        icons.get(i).invalidate();
    }*/
        iconFeather.move(VX(0, -(DEFAULT_SCR_Y - SCR_Y), 0));
        if (game.getPlayer().jumpsLeft > 0) {
            iconFeather.drawWithOffset(canvas, VX(iconX - SCR_W / 2, 0, iconY - SCR_H / 2));
            iconFeather.yaw += 0.02f;
            Vector pc = add(VX(iconX - SCR_W / 2, 0, iconY - SCR_H / 2), add(project(iconFeather.centroid()), moveToScreen));
            float wdt = iconW * 1.5f * (float) (game.getPlayer().featherCooldown) / (float) (game.getPlayer().maxFeatherCooldown);
            fillRect(canvas, (pc.x - 1 - iconW * 0.5f), (pc.z + iconH * 0.5f) + 15, (pc.x - 1 - iconW * 0.5f) + wdt, (pc.z + iconH * 0.5f) + 30, Color.RED);
            drawText(canvas, "" + game.getPlayer().jumpsLeft, (pc.x - 1 + iconW * 0.5f), (pc.z + iconH * 0.4f), 42, Color.WHITE);
            //canvas.drawText((pc.x-1+iconW/2)+" "+(pc.z+iconH*0.4f),500,600,fillP);
            iconFeather.invalidate();
        }
        iconFeather.move(VX(0, (DEFAULT_SCR_Y - SCR_Y), 0));
        iconBottle.move(VX(0, -(DEFAULT_SCR_Y - SCR_Y), 0));
        if (game.getPlayer().boostTime > 0) {
            iconBottle.drawWithOffset(canvas, VX(40 + 2 * iconX - SCR_W / 2, 0, iconY - SCR_H / 2 - 15));
            iconBottle.yaw += 0.02f;
            Vector pc = add(VX(40 + 2 * iconX - SCR_W / 2, 0, iconY - SCR_H / 2 - 15), add(project(iconBottle.centroid()), moveToScreen));
            // drawText(canvas,""+game.getPlayer().boo,(pc.x-1+iconW/2),(pc.z+iconH*0.4f),35 ,Color.WHITE);
            float wdt = iconW * 1.5f * (float) (game.getPlayer().boostTime) / (float) (game.getPlayer().maxBoostTime);
            fillRect(canvas, (pc.x - 1 - iconW * 0.75f), (pc.z + iconH * 0.5f) + 15, (pc.x - 1 - iconW * 0.75f) + wdt, (pc.z + iconH * 0.5f) + 30, Color.RED);
            //canvas.drawText((pc.x-1+iconW/2)+" "+(pc.z+iconH*0.4f),500,600,fillP);
            iconBottle.invalidate();
        }
        iconBottle.move(VX(0, (DEFAULT_SCR_Y - SCR_Y), 0));


        drawGearIcon(canvas);
        drawRect(canvas, pauseRect.x1, pauseRect.y1,
                pauseRect.x1 + 22, pauseRect.y2, Color.WHITE);
        drawRect(canvas, pauseRect.x1 + 39, pauseRect.y1,
                pauseRect.x2, pauseRect.y2, Color.WHITE);
        if (game.waitingToPause()) {
            fillRect(canvas, pauseRect.x1, pauseRect.y1,
                    pauseRect.x1 + 22, pauseRect.y2, GameView.getColorTheme());
            fillRect(canvas, pauseRect.x1 + 39, pauseRect.y1,
                    pauseRect.x2, pauseRect.y2, GameView.getColorTheme());
        }

        //fillRect(canvas,gearRect.x1,gearRect.y1,gearRect.x2,gearRect.y2,Color.RED);
        //  drawDebug(canvas);
    }

    public void drawDebug(Canvas canvas) {
        paint.setTextSize(40);
        paint.setColor(Color.WHITE);
        int rr = red(game.getTileColor());
        int gg = green(game.getTileColor());
        int bb = blue(game.getTileColor());
     /*   canvas.drawText("R: "+rr,100,150, paint);
        canvas.drawText("G: "+gg,100,200, paint);
        canvas.drawText("B: "+bb,100,250, paint);
        canvas.drawText("brightness: "+(int)(0.2126 * rr + 0.7152 * gg + 0.0722 * bb),100,300, paint);
        canvas.drawText("yaw: "+(game.getPlayer().yaw),100,350, paint);
        canvas.drawText("faces skipped: "+(game.getPlayer().facesSkipped),100,400, paint);
        canvas.drawText("BS: "+game.getPlayer().baseSpeed,100,450, paint);
        canvas.drawText("CS: "+game.getPlayer().currSpeed,100,500, paint);*/
        //    canvas.drawText("Tiles optimized: "+game.getTilesOptimized(),100,550, paint);
        canvas.drawText("Tiles total: " + game.elementCount(), 100, 600, paint);
       /* canvas.drawText("Air time: "+game.getPlayer().airTime,100,650, paint);
        canvas.drawText("Jumps Left: "+game.getPlayer().jumpsLeft,100,700, paint);*/
        canvas.drawText("Has spike below: " + game.getPlayer().hasSpikeBelow, 100, 750, paint);
    }




}
