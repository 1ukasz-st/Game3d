package com.example.game3d.elements;

import static com.example.game3d.engine3d.Util.PLAYER;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.crossProduct;
import static com.example.game3d.engine3d.Util.div;
import static com.example.game3d.engine3d.Util.dotProduct;
import static com.example.game3d.engine3d.Util.getCentroid;
import static com.example.game3d.engine3d.Util.getNormal;
import static com.example.game3d.engine3d.Util.sub;
import com.example.game3d.engine3d.Util.Cuboid;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Util;
import com.example.game3d.engine3d.Util.Vector;

import java.io.IOException;

public class Player extends Object3D {
    public static float CAM_YAW = 0.0f;
    public static final float MIN_SPEED = 65, MAX_SPEED = 115;
    public float baseSpeed, currSpeed , expectedSpeed ;
    public boolean canJump, waitForJump;
    public float minJumpPower, jumpPower, maxJumpPower;
    public Vector move;
    public int ct;
    public static final int PLR_SX = 84, PLR_SY = 296, PLR_SZ = 296;
    public int jumpsLeft ;
    public float speedupTime;
    public int airTime;
    //public int slowDownCooldown=0, slowDownMaxCooldown=70;

    public Cuboid cuboid;

    @Override
    protected boolean faceSkipped(ObjectFace fc){
        if(abs(yaw) > 0.02){ // back-face culling
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
        }
    }
    public Player() throws IOException {
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

        minJumpPower = 20; jumpPower = 0; maxJumpPower = 60;
        move = VX(0,0,0);
        ct=0;
        jumpsLeft = 0;
        airTime=0;
    }

    @Override
    public void calculate(){
        super.calculate();
        cuboid = new Cuboid(centroid(),PLR_SX,PLR_SY,PLR_SZ);
    }

}
