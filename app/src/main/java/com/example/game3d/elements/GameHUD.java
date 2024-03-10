package com.example.game3d.elements;

import static com.example.game3d.engine3d.Object3D.FC;
import static com.example.game3d.engine3d.Object3D.FCS;
import static com.example.game3d.engine3d.Object3D.loadFromFile;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.SCR_Y;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.VXS;
import static com.example.game3d.engine3d.Util.blue;
import static com.example.game3d.engine3d.Util.green;
import static com.example.game3d.engine3d.Util.multBrightness;
import static com.example.game3d.engine3d.Util.red;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Pair;

import com.example.game3d.GameView;
import com.example.game3d.engine3d.FixedMaxSizeDeque;
import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Object3D.Face;

import java.io.IOException;
import java.util.ArrayList;

import com.example.game3d.engine3d.Util.Vector;

public class GameHUD {
    private GameView game;
    private Paint p=new Paint();
    private Paint strokeP = new Paint(), fillP = new Paint();

    private Path reservedPath = new Path();
    private static Vector[] FEATHER_ICON_VERTS;
    private static Face[] FEATHER_ICON_FACES;
    private static final int MAX_FEATHERS = 64;
    private FixedMaxSizeDeque<Object3D> icons = new FixedMaxSizeDeque<>(MAX_FEATHERS);

    public static void ADD_FEATHER_ICON_ASSETS() throws IOException {
        Pair<Vector[], Face[]> data = loadFromFile("feather.obj", Color.CYAN, Color.CYAN, VX(0,1000,0), iconW, iconT, iconH, 0, 0, 0);
        FEATHER_ICON_VERTS = data.first;
        FEATHER_ICON_FACES = data.second;
    }

    public void addFeather(){
        icons.pushBack(new Object3D(FEATHER_ICON_VERTS, FEATHER_ICON_FACES) {
            @Override
            protected void extraInit() {
                facesSorted = false;
                oneColorAndFace = true;
                is_obs = true;
            }
        });
        icons.getLast().move(VX(currX - SCR_W / 2, 0, currY - SCR_H / 2));
        currX += iconW +30;
        if(currX>SCR_W){
            currX = 60;
            currY += iconH +30;
        }
    }
    public void removeFeather(){
        icons.removeLast();
        currX -= iconW +30;
        if(currX<=30){
            currX = 30;
            currY -= iconH +30;
        }
    }

    private Object3D arrow;

    public GameHUD(GameView gameView){
        this.game = gameView;
        strokeP.setStyle(Paint.Style.STROKE);
        fillP.setStyle(Paint.Style.FILL);
        arrow=new Object3D(VXS(
                VX(-iconW/4.0f,SCR_Y-iconT/2.0f, -iconH/2.0f), // 0
                VX(iconW/4.0f,SCR_Y-iconT/2.0f,-iconH/2.0f),   // 1
                VX(iconW/4.0f,SCR_Y-iconT/2.0f,iconH/10.0f),   // 2
                VX(iconW/2.0f,SCR_Y-iconT/2.0f,iconH/10.0f),   // 3
                VX(0,SCR_Y-iconT/2.0f,iconH/2.0f),             // 4
                VX(-iconW/2.0f,SCR_Y-iconT/2.0f,iconH/10.0f),  // 5
                VX(-iconW/4.0f,SCR_Y-iconT/2.0f,iconH/10.0f)  // 6

        ), FCS(
                FC(Color.RED,Color.RED,0,1,2,3,4,5,6)
        )) {
            @Override
            protected void extraInit() {
                facesSorted = false;
                oneColorAndFace = true;
                is_obs = true;
            }
        };
        arrow.move(VX(SCR_W/2 - iconW - 20,0,-SCR_H/2 + 350));
    }
    private void fillTriangle(Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3, int color){
        reservedPath.rewind();
        reservedPath.moveTo(x1,y1);
        reservedPath.lineTo(x2,y2);
        reservedPath.lineTo(x3,y3);
        reservedPath.close();
        fillP.setColor(color);
        canvas.drawPath(reservedPath,fillP);
    }
    private void drawMarker(Canvas canvas, float xc, float yc){
        float x[] = new float[]{
                xc-markerWidth/2,
                xc,
                xc+markerWidth/2,
                xc+markerWidth/2,
                xc,
                xc-markerWidth/2,
        };
        float y[] = new float[]{
                yc-markerHeight/4,
                yc-markerHeight*0.4f,
                yc-markerHeight/4,
                yc+markerHeight/4,
                yc+markerHeight*0.4f,
                yc+markerHeight/4,
        };

        fillTriangle(canvas, xc-markerWidth/2,yc-markerHeight*0.4f,x[0],y[0],x[1],y[1],Color.BLACK);
        fillTriangle(canvas, xc+markerWidth/2,yc-markerHeight*0.4f,x[1],y[1],x[2],y[2],Color.BLACK);
        fillTriangle(canvas, xc+markerWidth/2,yc+markerHeight*0.4f,x[3],y[3],x[4],y[4],Color.BLACK);
        fillTriangle(canvas, xc-markerWidth/2,yc+markerHeight*0.4f,x[4],y[4],x[5],y[5],Color.BLACK);
        strokeP.setColor(Color.WHITE);
        reservedPath.rewind();
        reservedPath.moveTo(x[0],y[0]);
        for(int i=1;i<x.length;++i){
            reservedPath.lineTo(x[i],y[i]);
        }
        reservedPath.close();
        canvas.drawPath(reservedPath,strokeP);

    }
    private void drawRect(Canvas canvas, float x1, float y1, float x2, float y2, int color){
        int lc = strokeP.getColor();
        strokeP.setColor(color);
        canvas.drawRect(x1,y1,x2,y2,strokeP);
        strokeP.setColor(lc);
    }
    private void fillRect(Canvas canvas, float x1, float y1, float x2, float y2, int color){
        int lc = fillP.getColor();
        fillP.setColor(color);
        canvas.drawRect(x1,y1,x2,y2,fillP);
        fillP.setColor(lc);
    }
    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.99f; // decrease value/brightness to darken
        return Color.HSVToColor(hsv);
    }

    private final float barIndentX = 50, jumpBarIndentY = 100, barMaxWidth = SCR_W - 2* barIndentX, barHeight = 80, timeBarIndentY = 100 + barHeight + 10;
    private int currX = 60, currY = 350;
    private final float markerWidth = 40,markerHeight = 200;
    private static final int iconW = 40, iconT = 20, iconH = 120;
    private static final float triangleWidth = 27, triangleHeight = 25;
    Path path = new Path();

    private void drawBarWithMarkersOutline(Canvas canvas, float x0, float y0, float x1, float y1, float progress, int outlineColor, int fillColor, Float... markerPositions){

        fillRect(canvas,x0,y0,x0+progress,y1,fillColor);

        path.rewind();
        path.moveTo(x0,y0);

        for(float pos : markerPositions) {
            path.lineTo(min(x1,max(x0, x0 + pos - triangleWidth / 2)),      y0);
            path.lineTo(min(x1,max(x0, x0 + pos)),                       y0 + triangleHeight);
            path.lineTo(min(x1,max(x0, x0 + pos + triangleWidth / 2)),      y0);
            fillTriangle(canvas,
                    min(x1,max(x0,x0 + pos - triangleWidth / 2)), y0,
                    min(x1,max(x0,x0 + pos)), y0 + triangleHeight,
                    min(x1,max(x0,x0 + pos + triangleWidth / 2)), y0,
                    Color.BLACK);
        }

        path.lineTo(x1,y0);
        path.lineTo(x1,y1);

        for(int i=markerPositions.length-1;i>=0;--i) {
            float pos = markerPositions[i];
            path.lineTo(min(x1,max(x0, x0 + pos + triangleWidth / 2)),      y1);
            path.lineTo(min(x1,max(x0, x0 + pos)),                       y1 - triangleHeight);
            path.lineTo(min(x1,max(x0, x0 + pos - triangleWidth / 2)),      y1);
            fillTriangle(canvas,
                    min(x1,max(x0,x0 + pos + triangleWidth / 2)), y1,
                    min(x1,max(x0,x0 + pos)), y1 - triangleHeight,
                    min(x1,max(x0,x0 + pos - triangleWidth / 2)), y1,
                    Color.BLACK);
        }

        path.lineTo(x0,y1);
        path.close();

        int lc = strokeP.getColor();
        strokeP.setColor(outlineColor);
        canvas.drawPath(path,strokeP);
        strokeP.setColor(lc);

    }

    int time=0;

    public void draw(Canvas canvas){
        if(game.isResetting()){
            fillRect(canvas,0,0,game.getResetRectSize(),SCR_H,game.getTileColor());
            return;
        }

        float barWidthCurrPower = (game.getPlayer().jumpPower/game.getPlayer().maxJumpPower) * barMaxWidth;
        float barWithMinPower = (game.getPlayer().minJumpPower/game.getPlayer().maxJumpPower) * barMaxWidth;
        float barWithStrongPower = (game.getPlayer().strongJumpPower/game.getPlayer().maxJumpPower) * barMaxWidth;

        int barColor = multBrightness(game.getTileColor(),min(1.0f,1.1f*game.getPlayer().jumpPower/game.getPlayer().maxJumpPower));

        drawBarWithMarkersOutline(canvas, barIndentX, jumpBarIndentY, barIndentX +barMaxWidth, jumpBarIndentY +barHeight,barWidthCurrPower,Color.WHITE,barColor,barWithMinPower,barWithStrongPower);

        ArrayList<Float> markers = new ArrayList<>();
        float dist = barMaxWidth/2.2f, x = ((float)(game.getTimeToColorChange())/game.getMaxTimeToColorChange())*dist;
        while(x < barIndentX+barMaxWidth) {
            markers.add(x);
            x += dist;
        }
        Float[] toArr = new Float[markers.size()];
        toArr = markers.toArray(toArr);
        drawBarWithMarkersOutline(canvas, barIndentX, timeBarIndentY, barIndentX +barMaxWidth, timeBarIndentY +barHeight,barWidthCurrPower,Color.WHITE,Color.BLACK,toArr);

        if(game.getPlayer().pressingFalling){
            //drawArrow(canvas,SCR_W/2,200,650,400,Color.argb(50,red(game.getTileColor()),green(game.getTileColor()),blue(game.getTileColor())),Color.TRANSPARENT);
            arrow.draw(canvas);
            arrow.yaw += 0.025f;
            arrow.invalidate();
        }

        for(int i = 0; i< icons.size(); ++i) {
            icons.get(i).yaw += 0.02f;
            icons.get(i).draw(canvas);
            icons.get(i).invalidate();
        }

        //drawDebug(canvas);


        ++time;

    }

    public void drawDebug(Canvas canvas){
        p.setTextSize(40);
        p.setColor(Color.WHITE);
        int rr = red(game.getTileColor());
        int gg = green(game.getTileColor());
        int bb = blue(game.getTileColor());
        canvas.drawText("R: "+rr,100,150,p);
        canvas.drawText("G: "+gg,100,200,p);
        canvas.drawText("B: "+bb,100,250,p);
        canvas.drawText("brightness: "+(int)(0.2126 * rr + 0.7152 * gg + 0.0722 * bb),100,300,p);
        canvas.drawText("yaw: "+(game.getPlayer().yaw),100,350,p);
        canvas.drawText("faces skipped: "+(game.getPlayer().facesSkipped),100,400,p);
        canvas.drawText("BS: "+game.getPlayer().baseSpeed,100,450,p);
        canvas.drawText("CS: "+game.getPlayer().currSpeed,100,500,p);
        canvas.drawText("Tiles optimized: "+game.getTilesOptimized(),100,550,p);
        canvas.drawText("Tiles total: "+game.elementCount(),100,600,p);
        canvas.drawText("Air time: "+game.getPlayer().airTime,100,650,p);
    }
}
