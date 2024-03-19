package com.example.game3d.elements;

import static com.example.game3d.elements.Player.CAM_YAW;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PI;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.randFloat;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.abs;
import static java.lang.Math.sin;

import android.graphics.Color;
import android.util.Pair;

import com.example.game3d.GameView;
import com.example.game3d.elements.Generator.WorldElement;
import com.example.game3d.engine3d.Util.Cuboid;
import com.example.game3d.engine3d.Util.Vector;

import java.io.IOException;

public class Potion extends WorldElement {
    public static int POTION_SX = 130, POTION_SY = 130, POTION_SZ = 275;
    public static Vector[] POTION_VERTS;
    public static Face[] POTION_FACES;

    public static void ADD_POTION_ASSETS(){
        try {
            Pair<Vector[], Face[]> data = loadFromFile("vodka.obj",Color.rgb(255,0,255),Color.WHITE,VX(0,0,0), POTION_SX, POTION_SY, POTION_SZ,0,-PI/2,0);
            Potion.POTION_VERTS = data.first;
            Potion.POTION_FACES = data.second;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Potion(float x, float y, float z) {
        super(POTION_VERTS, POTION_FACES,true);
        assert(POTION_FACES.length>0);
        move(VX(x,y,z));
    }

    @Override
    protected boolean faceSkipped(ObjectFace fc){
        return pointAndPlanePosition(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]),OBS)==1;
    }

    @Override
    public boolean collidesPlayer(Player player){
        if(abs(vertex(0).y)>2000) {
            return false;
        }
        return cuboid.intersectsCuboid(player.cuboid);
    }

    @Override
    public void interactWithPlayer(Player player) {
        if (!dead()) {
            //game.getHUD().addFeather();
            die();
            player.jumpsLeft += 1;
            player.boostTime = player.maxBoostTime;
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
        move(VX( 0,  0, -dy));
        move(deathMove);
        cuboid = new Cuboid(centroid(), POTION_SX*1.25f, POTION_SX*1.25f, POTION_SZ*1.1f);
    }
}
