package com.example.game3d;

import static com.example.game3d.engine3d.Util.PLAYER;

import static java.lang.Math.PI;

import android.graphics.Color;

import com.example.game3d.engine3d.Object3D;

import java.io.IOException;

public class Player extends Object3D {
    public Player() throws IOException {
        super("opona.obj",Color.BLACK, Color.WHITE,PLAYER,84,296,296,(float)(PI/2.0),0.0f,0.0f);
    }
}
