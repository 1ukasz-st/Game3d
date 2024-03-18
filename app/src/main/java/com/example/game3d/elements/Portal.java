package com.example.game3d.elements;

import static com.example.game3d.elements.Generator.WorldElement;
import static com.example.game3d.elements.Player.CAM_YAW;
import static com.example.game3d.engine3d.Util.Cuboid;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PI;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.VXS;
import static com.example.game3d.engine3d.Util.Vector;
import static com.example.game3d.engine3d.Util.multBrightness;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.graphics.Color;

import com.example.game3d.GameView;


public class Portal extends WorldElement {
    private Cuboid cuboid;
    private static final int N_PORTAL_VERTS = 6;
    public static final float PORTAL_INNER_RAD = 1000, PORTAL_OUTER_RAD = 1200;
    private static Vector[] PORTAL_VERTS;
    private static Face[] PORTAL_FACES;

    public static void ADD_PORTAL_ASSETS(){
        PORTAL_VERTS = new Vector[N_PORTAL_VERTS*2];
        PORTAL_FACES = new Face[2];
        int[] even = new int[N_PORTAL_VERTS], odd = new int[N_PORTAL_VERTS];
        for(int i=0;i<N_PORTAL_VERTS;++i){
            float ang = ((float)(i)/((float)(N_PORTAL_VERTS))) * 2.0f*PI;
            PORTAL_VERTS[2*i] = VX(PORTAL_OUTER_RAD*cos(ang),0,PORTAL_OUTER_RAD*sin(ang));
            PORTAL_VERTS[2*i+1] = VX(PORTAL_INNER_RAD*cos(ang),0,PORTAL_INNER_RAD*sin(ang));
            even[i] = 2*i;
            odd[i] = 2*i+1;
           // PORTAL_FACES[i] = FC(Color.BLUE,Color.BLUE,(2*i+2)%(2*N_PORTAL_VERTS),(2*i+3)%(2*N_PORTAL_VERTS), 2*i+1,2*i);
        }

        PORTAL_FACES[0] = FC(multBrightness(Color.rgb(255,0,255),0.35f),0,even);
        PORTAL_FACES[1] = FC(multBrightness(Color.rgb(255,0,255),0.35f),0,odd);
    }

    public Portal(Vector midP, GameView game) {
        super(PORTAL_VERTS,PORTAL_FACES,false,game);
        oneColorAndFace=true;
        pitch = PI/2;
        move(midP);
    }
    @Override
    public void calculate(){
       //for(Face fc : faces){
          //  faces[1].color = multBrightness(Color.rgb(255,0,255),0.35f);
          //  faces[1].ecolor = Color.WHITE;
         //   faces[0].color = multBrightness(Color.rgb(255,0,255),0.35f);
            //faces[0].ecolor = Color.WHITE;
        //}
        yaw+=0.025f;
        super.calculate();
        cuboid = new Cuboid(centroid(),PORTAL_INNER_RAD,PORTAL_INNER_RAD,150+abs(game.getPlayer().move.z));
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
        //player.baseSpeed = 300;
       /* player.move.x = (float) (sin(CAM_YAW)*180.0f);
        player.move.y = (float) (cos(CAM_YAW)*180.0f);
        player.move.z -= 60;*/
        player.baseSpeed = player.expectedSpeed*1.8f;
        player.move = VX(0, player.baseSpeed, -80);
        player.move = yaw(player.move, OBS, -CAM_YAW);
        player.portalMagic = true;
    }
}