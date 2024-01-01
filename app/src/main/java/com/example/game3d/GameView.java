package com.example.game3d;

import static com.example.game3d.engine3d.Util.PLAYER;

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

    public GameView(Context context) throws IOException {
        super(context);
        ASSET_MANAGER = getContext().getAssets();
        player = new Player();
        drawThread.start();
    }

    Player player;

    @Override
    public void onDraw(Canvas canvas) {
        player.draw(canvas);
        player.invalidate();
    }


}