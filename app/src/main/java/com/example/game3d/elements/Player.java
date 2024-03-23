package com.example.game3d.elements;

import static com.example.game3d.GameView.BASE_SCR_Y;
import static com.example.game3d.GameView.SCR_Y;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PLAYER;
import static com.example.game3d.engine3d.Util.VCPY;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.getCentroid;
import static com.example.game3d.engine3d.Util.mult;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.sub;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;

import android.graphics.Color;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.game3d.GameView;
import com.example.game3d.elements.interactables.Tile;
import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Util.Cuboid;
import com.example.game3d.engine3d.Util.Vector;

import java.io.IOException;

public class Player extends Object3D {
    public static final float MIN_SPEED = 75, MAX_SPEED = 150;
    public static final int PLR_SX = 84, PLR_SY = 296, PLR_SZ = 296;
    public static float CAM_YAW = 0.0f;
    public final int maxFeatherCooldown = 60;
    public final int maxBoostTime = 600;
    public int featherCooldown;
    public boolean hasSpikeBelow;
    public float baseSpeed, currSpeed, expectedSpeed;
    public boolean canJump, waitForJump;
    public float minJumpPower, jumpPower, strongJumpPower, maxJumpPower;
    public int boostTime;
    public Vector move;
    public int ct;
    public boolean pressingFalling = false, portalMagic = false;
    public int jumpsLeft, timeSinceRelease = 0;
    public float speedupTime;
    public int airTime;
    //public int slowDownCooldown=0, slowDownMaxCooldown=70;

    public Cuboid cuboid;
    public GameView game;
    public Tile chosenTile = null, tileBelow = null;
    private boolean dead;

    private float squishingTime=0;
    private final float squishingTimeMax=0.3f*7;
    private float squishingDelay = 0;
    private final float squishingDelayMax = 7;

    public void startSquishing(){
        if(squishingDelay==0) {
            float x = max(minJp,min(move.z,maxJp));
            mink = minSq - (x-minJp)*(x-minJp)*(minSq-maxSq)/((maxJp-minJp)*(maxJp-minJp));
            squishingTime = squishingTimeMax;
            squishingDelay = squishingDelayMax;
        }
    }

    private float minSq = 0.99f, maxSq = 0.961f;
    public final float minJp = 27f, maxJp = 115f;
    private float mink = 0.99f;
    private Vector[] ORIGINAL_VERTS;

    public Player() throws IOException {
        super("opona.obj", Color.BLACK, Color.WHITE, PLAYER, PLR_SX, PLR_SY, PLR_SZ, (float) (PI / 2.0), 0.0f, 0.0f);
        is_obs = true;
        CAM_YAW = 0.0f;
        baseSpeed = MIN_SPEED;
        currSpeed = baseSpeed;
        expectedSpeed = baseSpeed;
        canJump = false;
        waitForJump = false;
        speedupTime = 0.0f;

        ORIGINAL_VERTS = new Vector[verts.length];
        for(int i=0;i<verts.length;++i){
            ORIGINAL_VERTS[i] = VCPY(verts[i]);
        }
        //  jumpWait = 0;mjumpMaxWait = 20;
        //  slowDownMaxTime = 70; slowDownTime = slowDownMaxTime;

        minJumpPower = 18;
        jumpPower = 0;
        maxJumpPower = 60;
        strongJumpPower = maxJumpPower * 0.81f;

        move = VX(0, 0, 0);
        ct = 0;
        jumpsLeft = 0;
        airTime = 0;
        dead = false;
        portalMagic = false;
        boostTime = 0;
        featherCooldown = 0;
        hasSpikeBelow = false;
    }

    @Override
    protected boolean faceSkipped(ObjectFace fc) {
        return pointAndPlanePosition(vertex(fc.inds[0]), vertex(fc.inds[1]), vertex(fc.inds[2]), OBS) == -1;
    }

    @Override
    public void calculate() {
        super.calculate();
        cuboid = new Cuboid(centroid(), PLR_SX, PLR_SY, PLR_SZ);
        if (featherCooldown > 0) {
            --featherCooldown;
        }
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
        if (abs(baseSpeed - expectedSpeed) < 1) {
            baseSpeed = expectedSpeed;
        }
        if (baseSpeed < expectedSpeed) {
            if (portalMagic) {
                baseSpeed += 0.5;
            } else {
                baseSpeed += 1.2;
            }
        } else if (baseSpeed > expectedSpeed) {
            if (portalMagic) {
                baseSpeed -= 0.5;
            } else {
                baseSpeed -= 1.2;
            }
        }
        if (boostTime > 0) {
            if (SCR_Y > BASE_SCR_Y - 60) {
                SCR_Y -= 2;
            }
            baseSpeed = expectedSpeed * 1.3f;
            --boostTime;
            /*if(boostTime==0){
                jumpsLeft = max(0,jumpsLeft-1);
            }*/
        } else {
            if (SCR_Y < BASE_SCR_Y) {
                ++SCR_Y;
            }
            if (SCR_Y > BASE_SCR_Y) {
                --SCR_Y;
            }
        }
        if(squishingTime>0){
            squishingTime-=0.3f;
       //     float mink = 0.976f;
            float t = squishingTime/squishingTimeMax;
            float k = (t-0.5f)*(t-0.5f)*(1.0f-mink)/0.25f + mink;
           // Log.i("SCALE FACTOR",""+k);
            Vector c = getCentroid(verts), c2 = getCentroid(ORIGINAL_VERTS);
            for(int i=0;i<nVerts();++i){
                Vector d = sub(ORIGINAL_VERTS[i],c2);
                d.z*=k;
                verts[i] = add(c,d);
            }

        }
        if(squishingDelay>0){
            --squishingDelay;
        }
       /* if(unSquishingTime>0){
            --unSquishingTime;
            float k = (float) (1.0f - 0.1f*sqrt((float)(unSquishingTimeMax - unSquishingTime)/(float)(unSquishingTimeMax)));
           // Log.i("SCALE FACTOR",""+k);
            Vector c = getCentroid(verts), c2 = getCentroid(ORIGINAL_VERTS);
            for(int i=0;i<nVerts();++i){
                Vector d = sub(ORIGINAL_VERTS[i],c2);
                d.z*=k;
                verts[i] = add(c,d);
            }
        }*/
    }

    public void die() {
        dead = true;
    }

    public boolean isDead() {
        return dead;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        currSpeed = baseSpeed;
        canJump = false;
        chosenTile = null;
        tileBelow = null;
        ++timeSinceRelease;
        hasSpikeBelow = false;
    }


    public void stopSquishing(){
        squishingTime = 0;
    }

    public void jump(boolean noFooting) {
        float jp = jumpPower < strongJumpPower ? maxJumpPower * 0.3f : maxJumpPower;
        float k = (float) (sqrt(1 + jp / maxJumpPower) - 1);
        float j = expectedSpeed < 100 ? k * maxJumpPower * 0.75f + currSpeed : k * maxJumpPower * 0.9f + currSpeed * 0.75f;
        move = VX(0, j * 0.7f, -j * 1.3f);
        move = yaw(move, OBS, -CAM_YAW);
        waitForJump = false;
        jumpPower = 0;
        startSquishing();
        if (noFooting) {
            --jumpsLeft;
            featherCooldown = maxFeatherCooldown;
        }
    }
}
