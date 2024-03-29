package com.example.game3d.elements.interactables;

import static com.example.game3d.elements.Player.CAM_YAW;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PI;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.randFloat;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import com.example.game3d.elements.Player;
import com.example.game3d.elements.WorldElement;
import com.example.game3d.engine3d.Util.Cuboid;

import android.graphics.Color;
import android.util.Pair;

import java.io.IOException;

import com.example.game3d.engine3d.Util.Vector;

public class Feather extends WorldElement {
    public static int FEATHER_SX = 120, FEATHER_SY = 40, FEATHER_SZ = 400;
    public static Vector[] FEATHER_VERTS;
    public static Face[] FEATHER_FACES;

    public static void ADD_FEATHER_ASSETS(){
        try {
            Pair<Vector[], Face[]> data = loadFromFile("feather.obj",Color.CYAN,Color.CYAN,VX(0,0,0),FEATHER_SX,FEATHER_SY,FEATHER_SZ,0,0,0);
            Feather.FEATHER_VERTS = data.first;
            Feather.FEATHER_FACES = data.second;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Feather(float x, float y, float z) {
        super(FEATHER_VERTS,FEATHER_FACES,false);
        assert(FEATHER_FACES.length>0);
        move(VX(x,y,z));
        pitch = 0.5f;
        yaw = randFloat(0,PI/2,2);
        oneColorAndFace = true;
    }

    @Override
    public boolean collidesPlayer(Player player){
        if(abs(vertex(0).y)>2000){
            return false;
        }
        return cuboid.intersectsCuboid(player.cuboid);
    }

    @Override
    public void interactWithPlayer(Player player) {
        if (!dead()) {
            die();
            ++player.jumpsLeft;
        }
    }

    private int t=0;
    private float dy=0.0f;
    private Vector deathMove = VX(0,0,0);

    public boolean dead(){
        return deathMove.sqlen()>1;
    }
    public void die(){
        deathMove = yaw(VX(-200,-10,-150),OBS,-CAM_YAW);
    }

    private Cuboid cuboid;
    @Override
    public void calculate() {
        yaw+=0.04;
        ++t;
        dy = (float) (37.0f*sin((float)(t)*0.06f));
        move(VX( 0,  0, dy));
        super.calculate();
        if(dead() && abs(centroid().y)>MAX_Y){
            deathMove = VX(0,0,0);
            for(Vector v : verts){
                v.x = -100000;
            }
        }
        move(VX( 0,  0, -dy));
        move(deathMove);
        cuboid = new Cuboid(centroid(),FEATHER_SX*1.5f,FEATHER_SX*1.5f,FEATHER_SZ*1.25f);
    }
}
