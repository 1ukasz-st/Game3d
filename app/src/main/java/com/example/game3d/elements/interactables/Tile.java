package com.example.game3d.elements.interactables;

import static com.example.game3d.GameView.getColorTheme;
import static com.example.game3d.elements.Player.PLR_SX;
import static com.example.game3d.elements.Player.PLR_SY;
import static com.example.game3d.elements.Player.PLR_SZ;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.VCPY;
import static com.example.game3d.engine3d.Util.VXS;
import static com.example.game3d.engine3d.Util.blue;
import static com.example.game3d.engine3d.Util.green;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.red;
import static com.example.game3d.engine3d.Util.sub;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Color;

import com.example.game3d.elements.Player;
import com.example.game3d.elements.WorldElement;
import com.example.game3d.engine3d.Util;

public class Tile extends WorldElement {
    public float speedup = 1.0f;
    public boolean retarded = false;

    public Tile(Util.Vector a, Util.Vector b, Util.Vector c, Util.Vector d) {
        super(VXS(a, b, c, d), FCS(FC(Util.DEFAULT_COLOR, Util.DEFAULT_COLOR,0, 1, 2, 3)));
        oneColorAndFace = true;
    }

    public boolean isFrontHill() {
        return verts[0].z - verts[3].z > 15;
    }

    public boolean isBackHill() {
        return verts[3].z - verts[0].z > 15;
    }

    public boolean isHill() {
        return isFrontHill() || isBackHill();
    }

    @Override
    public void calculate() {
        super.calculate();
        double s = /*min(10,*/ -getSlope();/*)*/;
        double brightness_mul = min(1.5, 1 + s * s / 9.5);
        if (retarded) {
            brightness_mul = min(1.5, 1 + s * s / 1.1);
        }
        int curr_r = red(getColorTheme());
        int curr_g = green(getColorTheme());
        int curr_b = blue(getColorTheme());
        // if (isBackHill()) {
        if (pointAndPlanePosition(vertex(0), vertex(1), vertex(2), OBS) == 1) {
            brightness_mul *= 0.90;
        } else if (isBackHill()) {
            brightness_mul = 1;
        }
        //  }
        curr_r = (int) (min(255, (double) (curr_r) * brightness_mul));
        curr_g = (int) (min(255, (double) (curr_g) * brightness_mul));
        curr_b = (int) (min(255, (double) (curr_b) * brightness_mul));
        faces[0].color = Color.rgb(curr_r, curr_g, curr_b);
        faces[0].ecolor = Color.rgb(curr_r, curr_g, curr_b);
            /*if(vertex(0).y < 1000){
                faces[0].color = Color.YELLOW;
            }*/
    }

    public Util.Vector getDirection() {
        Util.Vector dir = sub(vertex(3), vertex(0));
        dir.z = 0;
        return dir;
    }

    public Util.Vector getOtherDirection() {
        Util.Vector dir = sub(vertex(1), vertex(0));
        dir.z = 0;
        return dir;
    }

    public Util.Vector vert(int i) {
        return VCPY(verts[i]);
    }

    @Override
    public boolean collidesPlayer(Player player) {
        if (abs(vertex(0).y) > 2000) {
            return false;
        }
        Util.Vector pc = player.centroid();
        float minx = pc.x - PLR_SX * 0.5f, miny = pc.y, minz = pc.z + PLR_SZ * 0.5f - 30;
        float maxx = pc.x + PLR_SX * 0.5f, maxy = pc.y + PLR_SY * 0.5f + 50, maxz = pc.z + PLR_SZ * 0.5f + 20;
        maxy += player.baseSpeed * 2;
        float minx2 = 10000.0f, miny2 = 10000.0f, minz2 = 10000.f;
        float maxx2 = -10000.0f, maxy2 = -10000.0f, maxz2 = -10000.f;
        for (int i = 0; i < nVerts(); ++i) {
            Util.Vector v = vertex(i);
            minx2 = min(minx2, v.x);
            miny2 = min(miny2, v.y);
            minz2 = min(minz2, v.z);
            maxx2 = max(maxx2, v.x);
            maxy2 = max(maxy2, v.y);
            maxz2 = max(maxz2, v.z);
        }
        //   maxz += 50;
        if (player.move.z > 40 || player.move.z < 0) {
            maxz += player.move.z;
        }
        if (minx > maxx2 || maxx < minx2) {
            return false;
        }
        if (miny > maxy2 || maxy < miny2) {
            return false;
        }
        return !(minz > maxz2) && !(maxz < minz2);
    }

    @Override
    public void interactWithPlayer(Player player) {
        player.chosenTile = this;
        player.canJump = true;
    }

    public float getSlope() {
        return min(0, (float) ((verts[3].z - verts[0].z) / Math.sqrt(getDirection().sqlen())));
    }
}