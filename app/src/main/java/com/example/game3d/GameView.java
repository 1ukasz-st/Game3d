package com.example.game3d;

import static com.example.game3d.elements.Generator.MAX_TILES;
import static com.example.game3d.elements.Generator.MIN_TILES;
import static com.example.game3d.elements.Generator.WorldElement;
import static com.example.game3d.elements.Player.CAM_YAW;
import static com.example.game3d.engine3d.Object3D.MAX_Y;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.Vector;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.adjustBrightness;
import static com.example.game3d.engine3d.Util.getBrightness;
import static com.example.game3d.engine3d.Util.getColorCloser;
import static com.example.game3d.engine3d.Util.isPointInTriangle;
import static com.example.game3d.engine3d.Util.mult;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.randomDistantColor;
import static com.example.game3d.engine3d.Util.sub;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.example.game3d.elements.Feather;
import com.example.game3d.elements.GameHUD;
import com.example.game3d.elements.Generator;
import com.example.game3d.elements.Generator.Tile;
import com.example.game3d.elements.Player;
import com.example.game3d.elements.Portal;
import com.example.game3d.elements.Potion;
import com.example.game3d.engine3d.FixedMaxSizeDeque;
import com.example.game3d.engine3d.Util;

import java.io.IOException;

public class GameView extends SurfaceView {

    private static final int MAX_ELEMENTS_PER_FRAME = 100;
    public static AssetManager ASSET_MANAGER = null;
    private final int maxTimeToColorChange = 1000;
    private final int maxBrightness = 75, minBrightness = 55;
    private final boolean running = true;
    private final Thread drawThread = new Thread() {
        @Override
        public void run() {
            while (running) {
                long t0 = System.nanoTime();
                invalidate();
                t0 = (System.nanoTime() - t0) / 1000000;
                if (t0 < 12) {
                    try {
                        Thread.sleep(12 - t0);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.i("OUCH", "OUCH");
                }
            }
        }
    };
    Player player;
    Paint p = new Paint();
    Paint p2 = new Paint();
    Path tilePath = new Path();
    int difficulty = 0, tilesOptimized = 0;
    private final FixedMaxSizeDeque<Tile> tileQueue = new FixedMaxSizeDeque<>(MAX_ELEMENTS_PER_FRAME);
    private final FixedMaxSizeDeque<WorldElement> otherElementsQueue = new FixedMaxSizeDeque<>(MAX_ELEMENTS_PER_FRAME);
    private Generator gen;
    private float resetRectSize = 0;
    private boolean resetting = false;
    private int timeToColorChange = maxTimeToColorChange, targetColor;
    private GameHUD hud;
    private float endX, endY;
    private boolean touchReleased = true;
    private final CalculateTask evenTask;
    private final CalculateTask oddTask;
    private int time = 0;

    public GameView(Context context) throws IOException {
        super(context);
        ASSET_MANAGER = getContext().getAssets();
        Util.GAMEFONT = Typeface.createFromAsset(ASSET_MANAGER, "ncr.ttf");
        Feather.ADD_FEATHER_ASSETS();
        GameHUD.ADD_FEATHER_ICON_ASSETS();
        GameHUD.ADD_BOTTLE_ICON_ASSETS();
        Portal.ADD_PORTAL_ASSETS();
        Potion.ADD_POTION_ASSETS();
        reset();
        evenTask = new CalculateTask(2, 0);
        oddTask = new CalculateTask(2, 1);
        evenTask.start();
        oddTask.start();
        drawThread.start();
    }

    private void prepareObjects(int elementsThisTime) {
        player.calculate();
        //long t0 = System.nanoTime();
        evenTask.setSize(elementsThisTime);
        oddTask.setSize(elementsThisTime);
        evenTask.begin();
        oddTask.begin();
        try {
            synchronized (this) {
                while (evenTask.stillGoing()) {
                    wait();
                }
            }
            synchronized (this) {
                while (oddTask.stillGoing()) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }
        /*Log.i("Time taken by computations", String.valueOf((System.nanoTime()-t0)/10));*/
    }

    private void reset() {
        resetting = false;
        resetRectSize = 0;
        try {
            player = new Player(this);
            targetColor = adjustBrightness(randomDistantColor(Color.TRANSPARENT, minBrightness, maxBrightness), minBrightness, maxBrightness);
            gen = new Generator(targetColor, this);
            timeToColorChange = maxTimeToColorChange;
            gen.generate(MAX_TILES, difficulty);
            p2.setStyle(Paint.Style.STROKE);
            hud = new GameHUD(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void flushTilePath(Canvas canvas) {
        p.setColor(gen.tileColor);
        canvas.drawPath(tilePath, p);
        p2.setColor(gen.tileColor);
        canvas.drawPath(tilePath, p2);
        tilePath.rewind();
    }

    private void drawTile(Tile tile, Canvas canvas) {
        /*if(tile==player.tileBelow){
            tile.faces[0].color = Color.YELLOW;
            flushTilePath(canvas);
            tile.draw(canvas);
        }else*/
        if (tile.centroid().y < MAX_Y) {
            if (!tile.isHill() && !tile.slightlyOutOfScreen() && tile.centroid().y > 3000) {
                ++tilesOptimized;
                tilePath.moveTo(tile.pVertex(0).x, tile.pVertex(0).z);
                tilePath.lineTo(tile.pVertex(1).x, tile.pVertex(1).z);
                tilePath.lineTo(tile.pVertex(2).x, tile.pVertex(2).z);
                tilePath.lineTo(tile.pVertex(3).x, tile.pVertex(3).z);
            } else {
                flushTilePath(canvas);
                tile.draw(canvas);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {

        tilesOptimized = 0;
        if (timeToColorChange == 0) {
            if (player.expectedSpeed < Player.MAX_SPEED) {
                player.expectedSpeed += 10;
                ++difficulty;
            } else {
                player.expectedSpeed = Player.MIN_SPEED;
                difficulty = 0;
            }
            targetColor = adjustBrightness(randomDistantColor(gen.tileColor, minBrightness, maxBrightness), minBrightness, maxBrightness);
            timeToColorChange = maxTimeToColorChange;
        } else {
            --timeToColorChange;
        }
        gen.tileColor = getColorCloser(gen.tileColor, targetColor);
        gen.tileColor = getColorCloser(gen.tileColor, targetColor);
        gen.tileColor = getColorCloser(gen.tileColor, targetColor);
        if (!(getBrightness(targetColor) >= minBrightness && getBrightness(targetColor) <= maxBrightness)) {
            Log.i("BAD COLOR", "" + getBrightness(targetColor));
            System.exit(1);
        }

        if (resetting) {
            resetRectSize -= (0.06 * resetRectSize + 9);
            if (resetRectSize < 1) {
                reset();
            }
            hud.draw(canvas);
            return;
        }

        int elementsThisTime = min(gen.elements.size(), MAX_ELEMENTS_PER_FRAME);
        prepareObjects(elementsThisTime);

        float maxZ = -1000000000.0f;
        for (int i = 0; i < elementsThisTime; ++i) {
            WorldElement we = gen.elements.get(i);
            if (we instanceof Tile) {
                //if(sub(we.centroid(),player.centroid()).sqlen() < 3000*3000 && !((Tile)we).isHill()) {
                maxZ = max(maxZ, we.vertex(0).z + 600);
                //  maxZ = max(maxZ,-pointAndPlanePosition(we.vertex(0),we.vertex(1),we.vertex(2),add(player.centroid(),VX(0,0,-1500))));
                //}

                tileQueue.pushBack((Tile) (we));
                Vector a = we.vertex(0), b = we.vertex(1), c = we.vertex(2), d = we.vertex(3);
                a.z = 0;
                b.z = 0;
                c.z = 0;
                d.z = 0;
                Vector pc = player.centroid();
                pc.z = 0;
                if ((isPointInTriangle(a, b, c, pc) || isPointInTriangle(a, d, c, pc)) && we.centroid().z - player.centroid().z < 1400 && pointAndPlanePosition(we.vertex(0), we.vertex(1), we.vertex(2), player.centroid()) == -1) {
                    if (player.tileBelow == null) {
                        player.tileBelow = (Tile) (we);
                    } else if (abs(add(player.centroid(), VX(0, 500, 0)).y - player.tileBelow.centroid().y) > abs(add(player.centroid(), VX(0, 500, 0)).y - we.centroid().y)) {
                        player.tileBelow = (Tile) (we);
                    }
                }
            } else {
                otherElementsQueue.pushBack(we);
            }
        }

        while (tileQueue.size() * otherElementsQueue.size() > 0) {
            Tile tile = tileQueue.getLast();
            WorldElement element = otherElementsQueue.getLast();
            if (sub(tile.centroid(), element.centroid()).sqlen() > 800 * 800) {
                if (tile.centroid().sqlen() < element.centroid().sqlen() * 0.8) {
                    flushTilePath(canvas);
                    element.draw(canvas);
                    otherElementsQueue.removeLast();
                } else {
                    drawTile(tile, canvas);
                    tileQueue.removeLast();
                }
            } else {
                if (tile.centroid().z < -30) {
                    flushTilePath(canvas);
                    element.draw(canvas);
                    otherElementsQueue.removeLast();
                } else {
                    drawTile(tile, canvas);
                    tileQueue.removeLast();
                }
            }
        }
        while (!tileQueue.isEmpty()) {
            drawTile(tileQueue.getLast(), canvas);
            tileQueue.removeLast();
        }
        flushTilePath(canvas);
        while (!otherElementsQueue.isEmpty()) {
            otherElementsQueue.getLast().draw(canvas);
            otherElementsQueue.removeLast();
        }

        if (maxZ < 0) {
            startResetting();
            return;
        }

        p.setColor(Color.BLACK);
        canvas.drawRect(player.getRectLeft() + 40, player.getRectTop() + 20, player.getRectRight() - 40, player.getRectBottom() - 20, p);
        player.draw(canvas);

        for (int i = 0; i < elementsThisTime; ++i) {
            WorldElement we = gen.elements.get(i);
            if (we.collidesPlayer(player)) {
                we.interactWithPlayer(player);
            }
        }

       /* if (((player.jumpsLeft > 0 && touchReleased && (player.tileBelow==null || player.move.z < 0)) || (player.chosenTile != null && (player.airTime > 40 || touchReleased))) && player.jumpPower>1 && !(player.jumpPower > player.minJumpPower)){
            player.jumpPower=0;
        }
        if (((player.jumpsLeft > 0 && touchReleased && (player.tileBelow==null || player.move.z < 0)) || (player.chosenTile != null && (player.airTime > 40 || touchReleased))) && player.jumpPower > player.minJumpPower) {*/
        if ((touchReleased && player.jumpPower < player.minJumpPower && player.tileBelow == null)) {
            player.jumpPower = max(0,player.jumpPower-0.5f);
        }
        if (touchReleased && player.jumpPower >= player.minJumpPower && player.chosenTile != null) {
            player.jump(false);
        } else if (!touchReleased && player.jumpPower >= player.minJumpPower && player.chosenTile != null && player.move.z > 20) {
            player.jump(false);
        } else if (touchReleased && player.jumpPower >= player.minJumpPower && player.chosenTile == null && (player.tileBelow == null ||  player.tileBelow.centroid().z - player.centroid().z > 1000 || player.move.z < -20) && player.jumpsLeft > 0) {
            player.jump(true);
        } else {
            if(touchReleased&&player.jumpPower>0 && player.tileBelow==null){
                player.jumpPower =0;// max(0,player.jumpPower-2f);
            }
            if (player.chosenTile == null) {

                player.move.z += 3.2;
                //    player.move.x *= 0.999995;
                //   player.move.y *= 0.999995;
                player.speedupTime = max(0.0f, player.speedupTime - 2);
            } else {
                player.portalMagic = false;
                if (player.move.z > 35 && !player.chosenTile.isBackHill() && player.chosenTile.speedup==1.0f && !player.pressingFalling) {
                    float temp_z = player.move.z * (-0.65f);
                    player.move = VX(0, player.currSpeed, temp_z);
                } else {
                    player.pressingFalling = false;
                    if (player.chosenTile.isFrontHill()) {
                        player.currSpeed *= 1.0f - 2 * Math.atan(abs(player.chosenTile.getSlope())) / 3.141f;
                    }
                   if (player.chosenTile.speedup>1.0f) {
                        player.speedupTime += 1;
                       // player.currSpeed *= 2.0f + (player.speedupTime / 30) * 1.7f;
                    } else {
                        player.speedupTime = 0.0f;
                    }
                    player.currSpeed *= player.chosenTile.speedup*1.1f + (player.speedupTime / 20);
                    player.move = VX(0, player.currSpeed, 0);
                    Vector par = player.chosenTile.getDirection(), per = player.chosenTile.getOtherDirection();
                    player.move.z = -player.currSpeed * (float) (Math.sqrt(par.sqlen())) * (per.x / (per.y * par.x - per.x * par.y)) * player.chosenTile.getSlope();
                    if (player.chosenTile.isFrontHill()) {
                        player.move.z -= 0.25f;
                        if (player.chosenTile.retarded) {
                            player.move.z -= 2f;
                            if(player.currSpeed > 90){
                                player.move.z -= 0.5f;
                            }
                        }
                        if (player.currSpeed > 110) {
                            player.move.z -= 2.25f;
                            if (player.chosenTile.retarded) {
                                player.move.z -= 1;
                            }
                        }
                    }

                }
                player.move = yaw(player.move, OBS, -CAM_YAW);
            }
        }

        if (player.chosenTile == null) {
            ++player.airTime;
        } else {
            player.airTime = 0;
        }

        player.pitch += player.currSpeed/*Math.sqrt(player.move.x*player.move.x + player.move.y*player.move.y)*/ / (Player.PLR_SY * 2 * 3.14) * 1.5;
        for (int i = 0; i < gen.elements.size(); i++) {
            gen.elements.get(i).move(mult(player.move, -1));
        }

        hud.draw(canvas);


        while (!gen.elements.isEmpty()) {
            if ((gen.elements.getFirst().centroid().y < -1000 && gen.elements.getFirst().outOfScreen()) || gen.elements.getFirst() instanceof Feather) {
                gen.elements.removeFirst();
                --elementsThisTime;
            } else {
                break;
            }
        }
        for (int i = 0; i < elementsThisTime; i++) {
            if (gen.elements.get(i).isValid()) {
                gen.elements.get(i).invalidate();
            }
        }
        player.invalidate();
        if (gen.elements.size() < MIN_TILES) {
            gen.generate(MAX_TILES - gen.elements.size(), difficulty);
        }
        ++time;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                endX = event.getX();
                endY = event.getY();
                touchReleased = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = -endX;
                float dy = -endY;
                touchReleased = false;
                endX = event.getX();
                endY = event.getY();
                dx += endX;
                dy += endY;
                if (dx * dx + (dy * dy) * 0.05 > 60000) {
                    break;
                }
                if (abs(dy) > 3 && abs(dx) < abs(dy) * 0.5) {
                    player.jumpPower = max(0, min(player.maxJumpPower, player.jumpPower - dy * 0.05f - (signum(dy)) * ((float) (sqrt(player.jumpPower / player.maxJumpPower) * 22 * 0.17f))));
                }
                if (abs(dx) < 8 && dy > 10 && player.airTime > 9 && player.jumpPower == 0 && player.chosenTile == null) {
                    player.pressingFalling = true;
                } else if (endY > SCR_H / 8 && abs(dy) < 50) {
                    CAM_YAW += 0.85 * dx * PI / SCR_W;
                    double tmp = 0.7 * min(0.15, abs(3.0 * sqrt(abs(dx) / 1.50) * 2.0 * PI / SCR_W));
                    if (tmp > abs(player.yaw) || signum(player.yaw) != signum(dx)) {
                        player.yaw += tmp * signum(dx);
                        if (abs(player.yaw) > 0.15) {
                            player.yaw = signum(player.yaw) * 0.15f;
                        }
                    }
                }
                player.ct = 2;
                break;
            case MotionEvent.ACTION_UP:
                endX = 0;
                endY = 0;
                touchReleased = true;
                if (player.jumpPower > 0) {
                    player.waitForJump = true;
                }
                player.pressingFalling = false;
                break;
        }
        return true;
    }

    public Generator getGenerator() {
        return gen;
    }

    public float getResetRectSize() {
        return resetRectSize;
    }

    public boolean isResetting() {
        return resetting;
    }

    public int getMaxTimeToColorChange() {
        return maxTimeToColorChange;
    }

    public int getTimeToColorChange() {
        return timeToColorChange;
    }

    public int getTargetColor() {
        return targetColor;
    }

    public int getMaxBrightness() {
        return maxBrightness;
    }

    public int getMinBrightness() {
        return minBrightness;
    }

    public Player getPlayer() {
        return player;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getTime() {
        return time;
    }

    public void startResetting() {
        resetting = true;
        resetRectSize = SCR_W;
    }

    public int getTileColor() {
        return gen.tileColor;
    }

    public int elementCount() {
        return gen.elements.size();
    }

    public int getTilesOptimized() {
        return tilesOptimized;
    }

    private class Task extends Thread {

        private boolean running = false;

        public synchronized void begin() {
            running = true;
        }

        public synchronized boolean stillGoing() {
            return running;
        }

        protected void execute() {

        }

        @Override
        public void run() {
            while (true) {
                if (running) {
                    execute();
                    running = false;
                    synchronized (GameView.this) {
                        GameView.this.notifyAll();
                    }
                }
            }
        }
    }

    private class CalculateTask extends Task {
        private final int mod;
        private final int off;
        private int size = 0;

        public CalculateTask(int mod, int off) {
            super();
            this.mod = mod;
            this.off = off;
        }

        public void setSize(int size) {
            this.size = size;
        }

        @Override
        public void execute() {
            for (int i = off; i < size; i += mod) {
                gen.elements.get(i).calculate();
            }
        }
    }
}