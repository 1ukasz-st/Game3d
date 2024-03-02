package com.example.game3d;

import static com.example.game3d.elements.Generator.MAX_TILES;

import com.example.game3d.elements.Feather;
import com.example.game3d.elements.Generator.Tile;

import static com.example.game3d.elements.Generator.MIN_TILES;
import static com.example.game3d.elements.Player.CAM_YAW;
import static com.example.game3d.engine3d.Object3D.MAX_Y;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.Vector;
import static com.example.game3d.engine3d.Util.adjustBrightness;
import static com.example.game3d.engine3d.Util.getBrightness;
import static com.example.game3d.engine3d.Util.mult;
import static com.example.game3d.engine3d.Util.randInt;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import static com.example.game3d.elements.Generator.WorldElement;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.example.game3d.elements.Generator;
import com.example.game3d.elements.Player;

import java.io.IOException;

public class GameView extends SurfaceView {

    private boolean running = true;
    private Thread drawThread = new Thread() {
        @Override
        public void run() {
            while (running) {
                invalidate();
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };
    public static AssetManager ASSET_MANAGER = null;

    Generator gen ;

    private float resetRectSize = 0;
    private boolean resetting = false;
    private int maxTimeToColorChange = 1400, timeToColorChange = maxTimeToColorChange, targetColor;
    private double maxBrightness = 70, minBrightness = 60;


    private int _randomColor(){
        int x = randInt(75,255-30);
        int y = randInt(30,255-x);
        int z = 255 - x - y;

        int diceRoll = randInt(1,6);
        if(diceRoll <= 2){
            if(diceRoll == 1){
                return adjustBrightness(x,y,z,minBrightness,maxBrightness);
            }else{
                return adjustBrightness(x,z,y,minBrightness,maxBrightness);
            }
        }else if(diceRoll<=4){
            if(diceRoll == 3){
                return adjustBrightness(y,x,z,minBrightness,maxBrightness);
            }else{
                return adjustBrightness(y,z,x,minBrightness,maxBrightness);
            }
        }else{
            if(diceRoll == 5){
                return adjustBrightness(z,x,y,minBrightness,maxBrightness);
            }else{
                return adjustBrightness(z,y,x,minBrightness,maxBrightness);
            }
        }
    }
    private int randomColor(int currColor){
        int r = (currColor >> 16) & 0xFF;
        int g = (currColor >> 8) & 0xFF;
        int b = currColor & 0xFF;

        int color = _randomColor();
        int r2 = (color >> 16) & 0xFF;
        int g2 = (color >> 8) & 0xFF;
        int b2 = color & 0xFF;
        if(Math.sqrt((r-r2)*(r-r2) + (g-g2)*(g-g2) + (b-b2)*(b-b2)) < 75){
            return randomColor(currColor);
        }
        return color;
    }
    private int getColorCloser(int currColor){
        int r = (currColor >> 16) & 0xFF;
        int g = (currColor >> 8) & 0xFF;
        int b = currColor & 0xFF;

        int r2 = (targetColor >> 16) & 0xFF;
        int g2 = (targetColor >> 8) & 0xFF;
        int b2 = targetColor & 0xFF;

        //if(timeToColorChange%10 == 0) {
            if (r < r2) {
                ++r;
            } else if (r > r2) {
                --r;
            } if (g < g2) {
                ++g;
            } else if (g > g2) {
                --g;
            } if (b < b2) {
                ++b;
            } else if (b > b2) {
                --b;
            }
        //}
        return Color.rgb(r,g,b);
    }

    private void reset(){
        try {
            player = new Player();
            targetColor = randomColor(Color.TRANSPARENT);
            gen = new Generator(targetColor);
            timeToColorChange = maxTimeToColorChange;
            gen.generate(MAX_TILES,difficulty);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public GameView(Context context) throws IOException {
        super(context);
        ASSET_MANAGER = getContext().getAssets();
        Feather.ADD_FEATHER_ASSET();
        reset();
        p2.setStyle(Paint.Style.STROKE);
        drawThread.start();
    }
    Player player;
    Paint p = new Paint();
    Paint p2 = new Paint();
    Path tilePath=  new Path();

    Feather f;
    int difficulty=0;
    @Override
    public void onDraw(Canvas canvas) {
        if(timeToColorChange==0){
            if(player.expectedSpeed<Player.MAX_SPEED){
                player.expectedSpeed+=15;
                ++difficulty;
            }else{
                player.expectedSpeed=Player.MIN_SPEED;
                difficulty=0;
            }
            targetColor = randomColor(gen.tileColor);
            timeToColorChange = maxTimeToColorChange;
        }else{
            --timeToColorChange;
        }
        gen.tileColor = adjustBrightness(gen.tileColor,minBrightness,maxBrightness);
        gen.tileColor = adjustBrightness(gen.tileColor,minBrightness,maxBrightness);
        gen.tileColor = adjustBrightness(gen.tileColor,minBrightness,maxBrightness);

        assert(getBrightness(gen.tileColor)>=minBrightness && getBrightness(gen.tileColor)<=maxBrightness);
        if(resetting){
            p.setColor(gen.tileColor);
            canvas.drawRect(0,0,resetRectSize,SCR_H,p);
            resetRectSize -= (0.06 * resetRectSize + 9);
            if(resetRectSize<1){
                resetting = false;
                resetRectSize = 0;
                reset();
            }
            return;
        }

        if(player.ct>0){
            --player.ct;
        }else{
            player.yaw-=0.016*signum(player.yaw);
            player.roll -= 0.008*signum(player.roll);
            if(abs(player.yaw)<0.016){
                player.yaw=0;
            }
            if(abs(player.roll)<0.008){
                player.roll=0;
            }
        }
        if(player.baseSpeed < player.expectedSpeed){
            player.baseSpeed+=1.2;
        }

        if(player.jumpPower < player.maxJumpPower*0.75){
            p.setColor(Color.GREEN);
        }else{
            p.setColor(Color.RED);
        }
        canvas.drawRect(50,110,50 + (player.jumpPower/player.maxJumpPower)*((float)(SCR_W-2*50)),210,p);


        player.canJump = false;
        float maxZ = -1000000000.0f;

        int tilesOptimized=0;
        for(int i =gen.elements.size()-1;i>=0;i--) {
            assert (!gen.elements.get(i).isValid());
            gen.elements.get(i).calculate();
        }
        for(int i =gen.elements.size()-1;i>=0;i--) {
            WorldElement we = gen.elements.get(i);
            if(we.vertex(0).y > MAX_Y){
                if(!tilePath.isEmpty()){
                    p.setColor(gen.tileColor);
                    canvas.drawPath(tilePath, p);
                    p2.setColor(gen.tileColor);
                    canvas.drawPath(tilePath, p2);
                    tilePath.rewind();
                }
                continue;
            }
            if(we instanceof Tile) {
                Tile tile = (Tile) (gen.elements.get(i));
                if (!tile.isHill() && !tile.slightlyOutOfScreen()) {
                    ++tilesOptimized;
                    tilePath.moveTo(tile.pVertex(0).x, tile.pVertex(0).z);
                    tilePath.lineTo(tile.pVertex(1).x, tile.pVertex(1).z);
                    tilePath.lineTo(tile.pVertex(2).x, tile.pVertex(2).z);
                    tilePath.lineTo(tile.pVertex(3).x, tile.pVertex(3).z);
                } else {
                    p.setColor(gen.tileColor);
                    canvas.drawPath(tilePath, p);
                    p2.setColor(gen.tileColor);
                    canvas.drawPath(tilePath, p2);
                    tilePath.rewind();
                    tile.draw(canvas);
                }
                if(tile.isValid()) {
                /*p.setTextSize(40);
                p.setColor(Color.WHITE);
                canvas.drawText(""+i,tile.pVertex(0).x,tile.pVertex(0).z,p);*/
                    maxZ = max(maxZ, tile.vertex(0).z);
                }
            }else{
                if(!tilePath.isEmpty()) {
                    p.setColor(gen.tileColor);
                    canvas.drawPath(tilePath, p);
                    p2.setColor(gen.tileColor);
                    canvas.drawPath(tilePath, p2);
                    tilePath.rewind();
                }
                we.draw(canvas);
            }

        }

        if(!tilePath.isEmpty()){
            p.setColor(gen.tileColor);
            canvas.drawPath(tilePath,p);
            tilePath.rewind();
        }
        if(maxZ < -100.0f){
            resetting = true;
            resetRectSize = SCR_W;
            return;
        }

        p.setColor(Color.BLACK);
        canvas.drawRect(player.getRectLeft()+40,player.getRectTop()+20,player.getRectRight()-40,player.getRectBottom()-20,p);
        player.draw(canvas);

        player.currSpeed = player.baseSpeed;

        int chosenIndex = 0;
        Tile chosenTile=null;
        for(int i = 0; i< gen.elements.size(); i++) {
            if (gen.elements.get(i) instanceof Feather) {
                Feather feather = (Feather) (gen.elements.get(i));
                if (feather.isValid() && !feather.dead() && feather.collidesPlayer(player)) {
                    feather.die();
                    ++player.jumpsLeft;
                    break;
                }
            }
        }
        for(int i = 0; i< gen.elements.size(); i++) {
            if(gen.elements.get(i) instanceof Tile) {
                Tile tile = (Tile)(gen.elements.get(i));
                if (tile.isValid() && abs(tile.vertex(0).y) <= 2000) {
                    if (tile.collidesPlayer(player)) {
                        player.canJump = true;
                        chosenTile = tile;
                        chosenIndex = i;
                        break;
                    }
                }
            }
        }

        if((player.jumpsLeft>0 || player.canJump) && (player.waitForJump || (player.jumpPower>0 && player.move.z > 20 && chosenTile != null))){
            float jp = player.jumpPower < player.maxJumpPower*0.75f ? player.maxJumpPower*0.3f : player.maxJumpPower;
            float k = (float) (sqrt(1+jp/player.maxJumpPower)-1);
            float j = k*player.maxJumpPower*0.75f + player.currSpeed;

            player.move = VX(0,j*0.7f, -j*1.1f);
            player.move = yaw(player.move, OBS, -CAM_YAW);
           // player.jumpWait = 0;
            player.waitForJump = false;
            player.jumpPower = 0;
            if(!player.canJump){
                --player.jumpsLeft;
            }
        }else {
            if (chosenTile == null) {
                player.move.z += 2.7;
                player.move.x *= 0.99995;
                player.move.y *= 0.99995;
            } else {
                if (player.move.z > 20 && !chosenTile.isBackHill() && !chosenTile.speedup) {
                    float temp_z = player.move.z * (-0.65f);
                    player.move = VX(0, player.currSpeed, temp_z);
                } else {
                    if(chosenTile.isFrontHill()){
                        player.currSpeed *= 1.0f - 2*Math.atan(abs(chosenTile.getSlope()))/3.141f;
                    }
                   if(chosenTile.speedup){
                       player.speedupTime+=1;
                        player.currSpeed *= 1.2f + (player.speedupTime/60)*1.7f;
                    }else{
                       player.speedupTime=max(0.0f,player.speedupTime-2);
                   }
                    player.move = VX(0, player.currSpeed, 0);
                    Vector par = chosenTile.getDirection(), per = chosenTile.getOtherDirection();
                    player.move.z = -player.currSpeed * (float) (Math.sqrt(par.sqlen())) * (per.x / (per.y * par.x - per.x * par.y)) * chosenTile.getSlope();
                    if(chosenTile.isFrontHill()){
                        player.move.z -= 0.75f;
                        if (chosenTile.retarded && player.currSpeed>70) {
                            player.move.z -= 1.5;
                        }
                        if (player.currSpeed > 90) {
                            player.move.z -= 3;
                            if (chosenTile.retarded) {
                                player.move.z -= 1;
                            }
                        }
                    }
                }
                player.move = yaw(player.move, OBS, -CAM_YAW);
            }
        }

        //player.move = yaw(VX(0,player.currSpeed,0), OBS, -CAM_YAW);

       /* if(player.jumpWait>0){
            --player.jumpWait;
        }*/

        player.pitch+= player.currSpeed/*Math.sqrt(player.move.x*player.move.x + player.move.y*player.move.y)*/ / (Player.PLR_SY * 2*3.14) * 1.5;
        for(int i = 0; i< gen.elements.size(); i++){
            gen.elements.get(i).move(mult(player.move,-1));
        }

        //int mustDelete = max(0,chosenIndex - 5);



        /*for(int i=0;i<mustDelete-1;++i){
            gen.elements.removeFirst();
        }*/

        while(!gen.elements.isEmpty() && gen.elements.getFirst().vertex(0).y<-4000){
            gen.elements.removeFirst();
        }
        for(int i = 0; i< gen.elements.size(); i++){
            if(gen.elements.get(i).isValid()) {
                gen.elements.get(i).invalidate();
            }
        }
        player.invalidate();

        p.setTextSize(40);
        p.setColor(Color.WHITE);
        //canvas.drawText("Tiles Optimized: "+tilesOptimized,100,150,p);
      /*  int rr = (gen.tileColor >> 16) & 0xFF;
        int gg = (gen.tileColor >> 8) & 0xFF;
        int bb = gen.tileColor & 0xFF;
        canvas.drawText("R: "+rr,100,150,p);
        canvas.drawText("G: "+gg,100,200,p);
        canvas.drawText("B: "+bb,100,250,p);
        canvas.drawText("brightness: "+(int)(0.2126 * rr + 0.7152 * gg + 0.0722 * bb),100,300,p);
        canvas.drawText("yaw: "+(player.yaw),100,350,p);
        canvas.drawText("faces skipped: "+(player.facesSkipped),100,400,p);
        canvas.drawText("tiles: "+(gen.tiles.size()),100,450,p);
        canvas.drawText("BS: "+player.baseSpeed,100,500,p);
        canvas.drawText("CS: "+player.currSpeed,100,550,p);
        canvas.drawText("Tiles optimized: "+tilesOptimized,100,600,p);*/
      //  canvas.drawText("SD: "+player.slowDownTime,100,600,p);
      //  canvas.drawText("CD: "+player.slowDownCooldown,100,650,p);

        if(gen.elements.size()<MIN_TILES){
            gen.generate(MAX_TILES-gen.elements.size(),difficulty);
        }
    }

    private float endX,endY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                endX = event.getX();
                endY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = -endX;
                float dy = -endY;
                endX = event.getX();
                endY = event.getY();
                dx += endX;
                dy += endY;
                if (dx * dx + (dy * dy)*0.05 > 60000) {
                    break;
                }
                if(abs(dy)>3 && abs(dx)<abs(dy)*0.5){
                    player.jumpPower = max(0,min(player.maxJumpPower,player.jumpPower- dy*0.05f - (float)(signum(dy))*((float)(sqrt(sqrt(player.jumpPower/player.maxJumpPower))*55*0.14f)) ));
                }
                if(endY > SCR_H/8 && abs(dy)<50) {
                    CAM_YAW += 0.85 * dx * PI / SCR_W;
                    double tmp = 0.7*min(0.15, abs(3.0 * sqrt(abs(dx) / 1.50) * 2.0 * PI / SCR_W));
                    if (tmp > abs(player.yaw) || signum(player.yaw) != signum(dx)) {

                        player.yaw += tmp * signum(dx);
                        if(abs(player.yaw) > 0.15){
                            player.yaw = signum(player.yaw) * 0.15f;
                        }
                    }
                }
                player.ct = 2;
                break;
            case MotionEvent.ACTION_UP:
                endX = 0;
                endY = 0;

                if(player.jumpPower>0){
                    player.waitForJump = true;
                    //player.jumpWait = player.jumpMaxWait;
                }
                break;
        }
        return true;
    }

}