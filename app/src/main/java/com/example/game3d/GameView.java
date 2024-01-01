package com.example.game3d;

import static com.example.game3d.engine3d.Util.OBS;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;

import com.example.game3d.engine3d.Object3D;

import java.io.IOException;

public class GameView extends SurfaceView {

    private boolean running = true;
    private Thread drawThread = new Thread() {
        @Override
        public void run() {
            while (running) {
                invalidate();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };
    public static AssetManager ASSET_MANAGER = null;
    Object3D tire;

    public GameView(Context context) throws IOException {
        super(context);
        ASSET_MANAGER = getContext().getAssets();
        tire = new Object3D("opona.obj", Color.BLACK, Color.WHITE,OBS,90.0f,300.0f,300.0f, 1.57f,0.0f,0.0f);
        drawThread.start();
    }

    Object3D cube = Object3D.makeCube(OBS, 100f, 200f, 300f, Color.GREEN);

    @Override
    public void onDraw(Canvas canvas) {
        tire.draw(canvas);
        tire.pitch -= 0.03;
        tire.yaw -= 0.01;
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setTextSize(30);
        canvas.drawText(tire.centroid().x + "," + tire.centroid().y + "," + tire.centroid().z, 100, 100, p);
        tire.invalidate();
    }


}