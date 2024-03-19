package com.example.game3d.elements;

import static com.example.game3d.elements.Player.CAM_YAW;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PI;
import static com.example.game3d.engine3d.Util.SCR_Y;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.mult;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.randFloat;
import static com.example.game3d.engine3d.Util.roll;
import static com.example.game3d.engine3d.Util.sub;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.graphics.Color;
import android.util.Pair;

import com.example.game3d.GameView;
import com.example.game3d.elements.Generator.WorldElement;
import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Util.Cuboid;
import com.example.game3d.engine3d.Util.Vector;

import java.io.IOException;

public class GearIcon extends Object3D {
    public static int OUTER_RAD = 115/4, INNER_RAD = 80/4, TEETH = 6;
    private static float TOOTH_ANGLE = 3.0f*PI/(2.0f*TEETH), TOOTH_SPACE_ANGLE = TOOTH_ANGLE*1.0f/3.0f;
    private static float TOOTH_LEN = (float) (2*INNER_RAD*sin(TOOTH_ANGLE*0.5f)), TOOTH_HEIGHT = OUTER_RAD-INNER_RAD;
    public static Vector[] VERTS;
    public static Face[] FACES;

    public static void ADD_GEAR_ICON_ASSETS(){
        int nVerts = (TEETH*2*2 + TEETH*2);
        VERTS = new Vector[nVerts];
        int nFaces = 2 + TEETH*3;
        FACES = new Face[1];
        float angle = 0.5f*PI - TOOTH_ANGLE/2;
        VERTS[0] = VX(INNER_RAD*cos(angle),SCR_Y,INNER_RAD*sin(angle));
        VERTS[3] = VX(INNER_RAD*cos(angle + TOOTH_ANGLE),SCR_Y,INNER_RAD*sin(angle + TOOTH_ANGLE));
        VERTS[1] = add(VERTS[0],VX(0,0,TOOTH_HEIGHT));
        VERTS[2] = add(VERTS[3],VX(0,0,TOOTH_HEIGHT));
        VERTS[4] = VX(INNER_RAD*cos(angle+TOOTH_ANGLE+TOOTH_SPACE_ANGLE/3),SCR_Y,INNER_RAD*sin(angle+TOOTH_ANGLE+TOOTH_SPACE_ANGLE/3));
        VERTS[5] = VX(INNER_RAD*cos(angle+TOOTH_ANGLE+2*TOOTH_SPACE_ANGLE/3),SCR_Y,INNER_RAD*sin(angle+TOOTH_ANGLE+2*TOOTH_SPACE_ANGLE/3));
        angle = 0.0f;
        for(int i=0;i<TEETH;++i){
            for(int j=0;j<6;++j) {
                VERTS[6 * i + j] = roll(VERTS[j], OBS, angle);
            }
            angle += TOOTH_ANGLE + TOOTH_SPACE_ANGLE;
        }
        int[] indices0 = new int[nVerts];
        for(int i=0;i< indices0.length;++i){
            indices0[i]=i;
        }
        FACES[0] = FC(Color.BLACK,Color.WHITE,indices0);
    }
    public GearIcon() {
        super(VERTS, FACES,true);
        assert(FACES.length>0);
    }

  /*  @Override
    protected boolean faceSkipped(ObjectFace fc){
        return pointAndPlanePosition(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]),OBS)==1;
    }*/

}
