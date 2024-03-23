package com.example.game3d.elements;

import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Util;

import java.io.IOException;

public class WorldElement extends Object3D {

    public WorldElement(String filename, int color, int ecolor, Util.Vector mid, float sx, float sy, float sz, float init_yaw, float init_pitch, float init_roll) throws IOException {
        super(filename, color, ecolor, mid, sx, sy, sz, init_yaw, init_pitch, init_roll);
    }

    public WorldElement(Util.Vector[] verts, Face[] faces) {
        super(verts, faces);
    }

    public WorldElement(Util.Vector[] verts, Face[] faces, boolean facesSorted) {
        super(verts, faces, facesSorted);
    }

    public boolean collidesPlayer(Player player) {
        return false;
    }

    public void interactWithPlayer(Player player) {

    }


}