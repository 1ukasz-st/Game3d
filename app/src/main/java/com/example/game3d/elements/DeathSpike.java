package com.example.game3d.elements;

import android.graphics.Color;

import static com.example.game3d.elements.Generator.WorldElement;

import static com.example.game3d.engine3d.Util.*;


public class DeathSpike extends WorldElement {
    public int a,c;
    public int color;
    public DeathSpike(Vector midP, int a, int c, int color) {
        super(new Vector[]{
                new Vector(midP.x-a/2,midP.y+a/2,midP.z),
                new Vector(midP.x+a/2,midP.y+a/2,midP.z),
                new Vector(midP.x+a/2,midP.y-a/2,midP.z),
                new Vector(midP.x-a/2,midP.y-a/2,midP.z),
                new Vector(midP.x,midP.y,midP.z-c),
        },new Face[]{
                new Face(color,color,0,1,4),
                new Face(color,color,1,2,4),
                new Face(color,color,2,3,4),
                new Face(color,color,3,0,4),
        },true);
        this.color = color;
    }
    @Override
    public void calculate(){
        for(Face fc : faces){
            fc.color = color;
            fc.ecolor = multBrightness(color,1.2f);
        }
    }
}