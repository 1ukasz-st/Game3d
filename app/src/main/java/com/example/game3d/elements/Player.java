package com.example.game3d.elements;

import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PLAYER;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.div;
import static com.example.game3d.engine3d.Util.getCentroid;
import static com.example.game3d.engine3d.Util.getNormal;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.yaw;

import com.example.game3d.GameView;
import com.example.game3d.elements.Generator.Tile;
import com.example.game3d.engine3d.Util.Cuboid;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;

import android.graphics.Color;
import android.util.Log;

import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Util.Vector;

import java.io.IOException;

public class Player extends Object3D {
    public static float CAM_YAW = 0.0f;
    public static final float MIN_SPEED = 75, MAX_SPEED = 150;
    public float baseSpeed, currSpeed , expectedSpeed ;
    public boolean canJump, waitForJump;
    public float minJumpPower, jumpPower, strongJumpPower, maxJumpPower;
    public final int maxBoostTime = 600;
    public int boostTime;
    public Vector move;
    public int ct;
    public static final int PLR_SX = 84, PLR_SY = 296, PLR_SZ = 296;
    public boolean pressingFalling = false, portalMagic = false;
    public int jumpsLeft ;
    public float speedupTime;
    public int airTime;
    //public int slowDownCooldown=0, slowDownMaxCooldown=70;

    public Cuboid cuboid;
    public GameView game;
    public Tile chosenTile = null, tileBelow = null;

    @Override
    protected boolean faceSkipped(ObjectFace fc){
        /*if(abs(yaw) > 0.02){ // back-face culling
            Vector normal = getNormal(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]));//crossProduct(edge1,edge2);
            normal = div(normal, (float) sqrt(normal.sqlen()));
            Vector v ;
            if(fc.inds.length==4){
                v = getCentroid(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]),vertex(fc.inds[3]));
            }else{
                v = getCentroid(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]));
            }
            return normal.y > 0.15 && v.z > centroid().z - PLR_SZ*0.5f + 50;
        }else{ // back-face culling but simplified
            Vector v ;
            if(fc.inds.length==4){
                v = getCentroid(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]),vertex(fc.inds[3]));
            }else{
                v = getCentroid(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]));
            }
            return v.y > 830 && v.z > 450 - PLR_SZ/2 + 50;
        }*/
        return pointAndPlanePosition(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]),OBS)==-1;
    }
    public Player(GameView game) throws IOException {
        super("opona.obj",Color.BLACK, Color.WHITE,PLAYER,PLR_SX,PLR_SY,PLR_SZ,(float)(PI/2.0),0.0f,0.0f);
        is_obs = true;
        CAM_YAW = 0.0f;
        baseSpeed = MIN_SPEED;
        currSpeed = baseSpeed;
        expectedSpeed = baseSpeed;
        canJump = false;
        waitForJump = false;
        speedupTime=0.0f;
        //  jumpWait = 0;mjumpMaxWait = 20;
        //  slowDownMaxTime = 70; slowDownTime = slowDownMaxTime;

        minJumpPower = 18; jumpPower = 0; maxJumpPower = 60;
        strongJumpPower = maxJumpPower*0.81f;

        move = VX(0,0,0);
        ct=0;
        jumpsLeft = 0;
        airTime=0;
        portalMagic = false;
        boostTime = 0;
        this.game=game;
    }

    @Override
    public void calculate(){
        super.calculate();
        cuboid = new Cuboid(centroid(),PLR_SX,PLR_SY,PLR_SZ);
        if (ct > 0) {
            --ct;
        } else {
            yaw -= 0.016 * signum(yaw);
            roll -= 0.008 * signum(roll);
            if (abs(yaw) < 0.016) {
                yaw = 0;
            }
            if (abs(roll) < 0.008) {
                roll = 0;
            }
        }
        if(abs(baseSpeed-expectedSpeed)<1){
            baseSpeed=expectedSpeed;
        }
        if (baseSpeed < expectedSpeed) {
            if(portalMagic){
                baseSpeed += 0.5;
            }else {
                baseSpeed += 1.2;
            }
        }else if(baseSpeed > expectedSpeed){
            if(portalMagic){
                baseSpeed -= 0.5;
            }else {
                baseSpeed -= 1.2;
            }
        }
        if(boostTime>0){
            baseSpeed = expectedSpeed*1.3f;
            --boostTime;
            if(boostTime==0){
                jumpsLeft = max(0,jumpsLeft-1);
            }
        }
    }

    @Override
    public void invalidate(){
        super.invalidate();
        currSpeed = baseSpeed;
        canJump = false;
        chosenTile = null;
        tileBelow = null;
    }


    public void jump(boolean noFooting){
        float jp = jumpPower < strongJumpPower ? maxJumpPower * 0.3f : maxJumpPower;
        float k = (float) (sqrt(1 + jp / maxJumpPower) - 1);
        float j = expectedSpeed < 100 ? k * maxJumpPower * 0.75f + currSpeed : k * maxJumpPower * 0.9f + currSpeed*0.75f ;
        move = VX(0, j * 0.7f, -j * 1.3f);
        move = yaw(move, OBS, -CAM_YAW);
        waitForJump = false;
        jumpPower = 0;
        if(noFooting){
            --jumpsLeft;
        }
    }
}
