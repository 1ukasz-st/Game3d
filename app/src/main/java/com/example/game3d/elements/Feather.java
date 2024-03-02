package com.example.game3d.elements;

import static com.example.game3d.elements.Player.CAM_YAW;
import static com.example.game3d.elements.Player.PLR_SX;
import static com.example.game3d.elements.Player.PLR_SY;
import static com.example.game3d.elements.Player.PLR_SZ;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.getBoundingCuboid;
import static com.example.game3d.engine3d.Util.getCentroid;
import static com.example.game3d.engine3d.Util.randDouble;
import static com.example.game3d.engine3d.Util.rayCuboidDistance;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import com.example.game3d.engine3d.Util.Cuboid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Pair;

import com.example.game3d.elements.Generator.WorldElement;

import java.io.IOException;

import com.example.game3d.engine3d.Util.Vector;

public class Feather extends WorldElement {
    public static int FEATHER_SX = 120, FEATHER_SY = 40, FEATHER_SZ = 400;
    public static Vector[] FEATHER_VERTS;
    public static Face[] FEATHER_FACES;

    public static void ADD_FEATHER_ASSET(){
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
        yaw = (float) randDouble(0,PI/2,2);
        oneColorAndFace = true;
    }

    public boolean collidesPlayer(Player player){
        return cuboid.intersectsCuboid(player.cuboid);
        /*for(Vector off = VX(-PLR_SX*0.8f,-PLR_SY*0.5f,0);off.x<=PLR_SX*0.8f;off.x+=10.0f) {
            for(off.z=-PLR_SZ*0.5f;off.z<=PLR_SZ*0.25f;off.z+=10.0f) {
                double d = rayCuboidDistance(add(player.centroid(), off), VX(0, 1, -0.2f), cuboid);
                if (d < PLR_SY + player.expectedSpeed && d > 0) {
                    return true;
                }
            }
        }
         return false;*/
    }

    private int t=0;
    private float dy=0.0f;
    private Vector deathMove = VX(0,0,0);

    public boolean dead(){
        return deathMove.sqlen()>1;
    }
    public void die(){
        deathMove = yaw(VX(-200,100,-150),OBS,-CAM_YAW);
    }

    private Cuboid cuboid;
    @Override
    public void calculate() {
        super.calculate();
        yaw+=0.04;
        t=(t+1)%50;
        dy = (float) (sin((float)(t)*0.1));
        move(VX( 0,  0, dy));
        move(deathMove);
        cuboid = new Cuboid(centroid(),FEATHER_SX,FEATHER_SX,FEATHER_SZ);
    }
}
