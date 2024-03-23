package com.example.game3d;

import static com.example.game3d.GameView.DEFAULT_SCR_Y;
import static com.example.game3d.elements.Player.PLR_SX;
import static com.example.game3d.elements.Player.PLR_SY;
import static com.example.game3d.elements.Player.PLR_SZ;
import static com.example.game3d.engine3d.DrawUtil.drawCenteredText;
import static com.example.game3d.engine3d.Util.PI;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.VX;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.example.game3d.engine3d.Object3D;

import java.io.IOException;

public class TestDrawing {
    Object3D tire ;//

    public TestDrawing() {
        try {
            tire = new Object3D("opona.obj", Color.BLACK, Color.WHITE, VX(0, DEFAULT_SCR_Y-100, 0), PLR_SX, PLR_SY, PLR_SZ, PI*0.5f, 0.0f, 0.0f);
            tire.yaw = 0.5f*PI - 0.5f;
        }catch(Exception ignored){
            Log.e("LOADING FAIL","LOADING FAIL");
        }
    }

    public void draw(Canvas canvas){
        tire.draw(canvas);
        tire.invalidate();
        drawCenteredText(canvas,"BETA",SCR_W*0.5f,SCR_H*0.5f,PLR_SZ*0.99f,Color.RED);
      //  tire.yaw+=0.01f;
    }
}
