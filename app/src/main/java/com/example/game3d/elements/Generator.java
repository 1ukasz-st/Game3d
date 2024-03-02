package com.example.game3d.elements;

import static com.example.game3d.elements.Player.PLR_SX;
import static com.example.game3d.elements.Player.PLR_SY;
import static com.example.game3d.elements.Player.PLR_SZ;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.sub;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import com.example.game3d.engine3d.FixedMaxSizeDeque;
import com.example.game3d.engine3d.Object3D;

import java.io.IOException;

import static com.example.game3d.engine3d.Util.*;

import android.graphics.Color;

public class Generator{

    public Generator(int color){
        this.tileColor = color;
    }
    public static final int MAX_TILES = 120, MIN_TILES = 70;
    public static class WorldElement extends Object3D{
        public WorldElement(String filename, int color, int ecolor, Vector mid, float sx, float sy, float sz, float init_yaw, float init_pitch, float init_roll) throws IOException {
            super(filename, color, ecolor, mid, sx, sy, sz, init_yaw, init_pitch, init_roll);
        }
        public WorldElement(Vector[] verts, Face[] faces) {
            super(verts, faces);
        }
        public WorldElement(Vector[] verts, Face[] faces, boolean facesSorted) {
           super(verts, faces, facesSorted);
        }
    }

    public int tileColor;
    public float a=1400,b=500;
    public class Tile extends WorldElement{
        public int color = Color.RED;
        public boolean speedup = false;
        public boolean retarded = false;
        public Tile(Vector a, Vector b, Vector c, Vector d){
            super(
                VXS(a,b,c,d),
                FCS(FC(tileColor,tileColor,0,1,2,3))
            );;
        }

        public boolean isFrontHill(){
            return verts[0].z-verts[3].z > 15;
        }
        public boolean isBackHill(){
            return verts[3].z-verts[0].z > 15;
        }
        public boolean isHill(){
            return isFrontHill() || isBackHill();
        }

        @Override
        public void calculate() {
            super.calculate();
            if(color==Color.WHITE || color==Color.YELLOW){
                return;
            }
            double s = min(3,-getSlope());
                double brightness_mul = min(1.5, 1 + s*s / 9.5);
                if(retarded){
                    brightness_mul = min(1.5, 1 + s*s / 1.1);
                }
            int curr_r = (tileColor >> 16) & 0xFF;
            int curr_g = (tileColor >> 8) & 0xFF;
            int curr_b = tileColor & 0xFF;
            if(isBackHill()){
                if(vertex(0).y > 5000) {
                    brightness_mul *= 0.91;
                }else{
                    brightness_mul = 1;
                }
            }
            curr_r = (int) (min(255,(double)(curr_r)*brightness_mul));
            curr_g = (int) (min(255,(double)(curr_g)*brightness_mul));
            curr_b = (int) (min(255,(double)(curr_b)*brightness_mul));
            color = Color.rgb(curr_r, curr_g, curr_b);
            faces[0].color = color;
            faces[0].ecolor = color;
        }

        public Vector getDirection(){
            Vector dir = sub(vertex(3),vertex(0));
            dir.z=0;
            return dir;
        }
        public Vector getOtherDirection(){
            Vector dir = sub(vertex(1),vertex(0));
            dir.z=0;
            return dir;
        }

        public Vector vert(int i){
            return verts[i];
        }

        public boolean collidesPlayer(Player player){
            Vector pc = player.centroid();
            float minx = pc.x-PLR_SX*0.5f, miny = pc.y, minz = pc.z+PLR_SZ*0.5f - 30;
            float maxx =  pc.x+PLR_SX*0.5f, maxy = pc.y+PLR_SY*0.5f + 50, maxz = pc.z+PLR_SZ*0.5f + 20;
            maxy += player.baseSpeed *2;
            float minx2 = 10000.0f, miny2 = 10000.0f, minz2 = 10000.f;
            float maxx2 = -10000.0f, maxy2 = -10000.0f, maxz2 = -10000.f;
            for(int i = 0;i<nVerts();++i){
                Vector v = vertex(i);
                minx2 = min(minx2,v.x);
                miny2 = min(miny2,v.y);
                minz2 = min(minz2,v.z);
                maxx2 = max(maxx2,v.x);
                maxy2 = max(maxy2,v.y);
                maxz2 = max(maxz2,v.z);
            }
         //   maxz += 50;
            if(player.move.z>40 || player.move.z < 0) {
                maxz += player.move.z;
            }
            if(minx > maxx2 || maxx < minx2){
                return false;
            }
            if(miny > maxy2 || maxy < miny2){
                return false;
            }
            if(minz > maxz2 || maxz < minz2){
                return false;
            }
            return true;
        }
        public float getSlope() {
            return min(0,(float) ((verts[3].z - verts[0].z) / Math.sqrt(getDirection().sqlen())));
        }
    }
    public FixedMaxSizeDeque<WorldElement> elements = new FixedMaxSizeDeque<>(MAX_TILES + 100);
    private Vector startPos = VX(0,0,750);
    private float lYaw=0,lPitch=0,lastY=0;
    private Tile lastTile=null;

    private void addTile(){
        Vector closeLeft = add(startPos, VX(-a/2,-b/2,0));//lastTile.vert(0);
        Vector closeRight = add(startPos,VX(a/2,-b/2,0));//lastTile.vert(1);
        Vector farRight = add(startPos,VX(a/2,b/2,0));//lastTile.vert(2);
        Vector farLeft = add(startPos,VX(-a/2,b/2,0));//lastTile.vert(3);

        closeLeft = yaw(closeLeft,OBS,lYaw);
        closeRight = yaw(closeRight,OBS,lYaw);
        farRight = yaw(farRight,OBS,lYaw);
        farLeft = yaw(farLeft,OBS,lYaw);

        farRight.z += b*tan(lPitch);
        farLeft.z += b*tan(lPitch);

        Vector diff = sub(lastTile.vert(3),closeLeft);

        closeLeft = add(closeLeft,diff);
        closeRight = add(closeRight,diff);
        farRight = add(farRight,diff);
        farLeft = add(farLeft,diff);
        closeRight = lastTile.vert(2);

        lastTile = new Tile(closeLeft,closeRight,farRight,farLeft);
        elements.pushBack(lastTile);
        ++tilesSinceTurn;
    }

    float f(float x){
        return 0.6f*(0.1f*x*x - 1.5f*x + 1);
    }
    float g(float x){
        double v = 1 - sin(x * 0.5f) * sin(x * 0.5f) * sin(x * 0.5f) * sin(x * 0.5f);
        return 0.5f*(float) (1- v * v);}

    void addAddons(){
            float z = lastTile.vert(0).z - Feather.FEATHER_SZ;
           Vector c = getCentroid(lastTile.vert(0),lastTile.vert(1),lastTile.vert(2),lastTile.vert(3));
            try {
                elements.pushBack(new Feather(c.x,c.y,z));
            }catch (Exception e){e.printStackTrace();}
    }
    private int tilesSinceTurn = 0;
    public void generate(int n, int difficulty){
        if(elements.isEmpty()){
            lastTile = new Tile(
                    add(startPos, VX(-a/2,-b/2,0)), // close left
                    add(startPos,VX(a/2,-b/2,0)), // close right
                    add(startPos,VX(a/2,b/2,0)), // far right
                    add(startPos,VX(-a/2,b/2,0))); // far left
            lastTile.color = Color.GREEN;
            elements.pushBack(lastTile);
            --n;
        }
        int step = 0;
        while(n>0){
            int diceRoll = randInt(0,11);
            if(diceRoll>9 && n>=17 && step > 1){
                int nSteps = randInt(16,min(n-1,32));
                float dYaw = randDoubleRanges(3,-PI/(1.3f*nSteps),-PI/(5*nSteps),PI/(5*nSteps),PI/(1.3f*nSteps));
                float descent = randIntRanges(-500,-300,400,700);
                double r = randFloat(1.2f,1.5f,2);
                b*=r;
                int tilesPerPlatform = randInt(2,4), tilesToDelete = randInt(0,tilesPerPlatform), total = tilesPerPlatform+tilesToDelete;
                for(int i=0;i<nSteps;++i){
                    lYaw+=dYaw;
                    addTile();
                    if(i % total < tilesPerPlatform){
                       //lastTile.color = Color.WHITE;
                        --n;
                        ++step;
                        if(randInt(1,50)==2){
                            addAddons();
                        }
                    }else{
                        if(i%total == tilesPerPlatform && i != nSteps-1) {
                            lastTile.move(VX(0, 0, descent));
                        }
                        //lastTile.color = Color.YELLOW;
                        elements.removeLast();
                    }
                }
                b/=r;
                addTile();
                --n;
                ++step;
            }
            else if(diceRoll > 5 && n >= 21 && step > 2){
                int nSteps = randInt(20,min(n-1,25));
                float minr = 0.2f, maxr = 0.6f;
                if(difficulty==1){
                    minr = 0.4f;
                    maxr=0.75f;
                }else if(difficulty==2){
                    minr = 0.6f;
                    maxr = 0.9f;
                }
                float r = randFloat(minr,maxr,2);
                b*=r;
                int k=0;
                int which = randInt(0,2);
                float len;
                if(which==0){
                    len = 20.0f / nSteps;
                }else{
                    len = (PI / nSteps);
                }

                for(float i=0;i<nSteps;++i){
                    if(which==0) {
                        lPitch = (float) -Math.atan((f((i + 1) * len) - f(i * len)) / len);
                    }else{
                        lPitch = (float) -Math.atan((g((i + 1) * len) - g(i * len)) / len);
                    }
                    addTile();
                    if(which!=0){
                        lastTile.retarded = true;
                    }else{
                        if(lastTile.isFrontHill()) {
                            lastTile.speedup = true;
                        }
                    }
                    ++step;
                    --n;
                }
                lPitch=0;
                addTile();
                ++step;
                --n;
                b/=r;
            }else if(diceRoll>2 && n>=6 && step > 2 && tilesSinceTurn > 10){
                int nSteps = randInt(6,min(n,14));
                double dYaw = randDoubleRanges(3,-PI/(2.5f*nSteps),-PI/(5.5f*nSteps),PI/(5.5f*nSteps),PI/(2.5f*nSteps));
                for(int i=0;i<nSteps;++i){
                    lYaw+=dYaw;
                    addTile();
                    if(randInt(1,20)==2 && step > 4){
                        addAddons();
                    }
                    ++step;
                    --n;
                }
                tilesSinceTurn=0;
            }else{
                int nSteps = min(n,randInt(4,8));
                for(int i=0;i<nSteps;++i) {
                    addTile();
                    if(randInt(1,20)==2 && step > 4){
                        addAddons();
                    }
                    ++step;
                    --n;
                }
             }
        }
    }
}