package com.example.game3d.elements;

import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PI;
import static com.example.game3d.GameView.SCR_Y;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.roll;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.graphics.Color;

import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Util.Vector;

public class GearIcon extends Object3D {
    public static float OUTER_RAD = 1.5f*(115.0f/4.0f), INNER_RAD = 1.5f*(80.0f/4.0f), AXIS_RAD =  1.5f*(50.0f/4.0f);
    private static final int TEETH = 6;
    private static final float TOOTH_ANGLE = 3.0f*PI/(2.0f*TEETH);
    private static final float TOOTH_SPACE_ANGLE = TOOTH_ANGLE /3.0f;
    private static final float TOOTH_LEN = (float) (2*INNER_RAD*sin(TOOTH_ANGLE*0.5f));
    private static final float TOOTH_HEIGHT = OUTER_RAD-INNER_RAD;
    public static Vector[] VERTS;
    public static Face[] FACES;

    public static void ADD_GEAR_ICON_ASSETS(){
        int nVerts = (TEETH*2*2 + TEETH*2) + 16;
        VERTS = new Vector[nVerts];
        //int nFaces = 2 + TEETH*3;
        FACES = new Face[2];
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
        int[] indices0 = new int[nVerts-16], indices1 = new int[16];
        for(int i=0;i< indices0.length;++i){
            indices0[i]=i;
        }
        for(int i=0;i< 16;++i){
            indices1[i]=nVerts-16+i;
            VERTS[indices1[i]] = VX(AXIS_RAD*cos(PI/8.0f * (float)(i+1)),SCR_Y,AXIS_RAD*sin(PI/8.0f * (float)(i+1)));
        }
        FACES[1] = FC(Color.BLACK,Color.WHITE,indices0);
        FACES[0] = FC(Color.BLACK,Color.WHITE,indices1);
    }
    public GearIcon() {
        super(VERTS, FACES,false);
        is_obs=true;
        oneColorAndFace=true;
    }

}
