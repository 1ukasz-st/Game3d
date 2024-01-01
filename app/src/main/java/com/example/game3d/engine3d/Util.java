package com.example.game3d.engine3d;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Util {
    public static float SCR_W, SCR_H, CAM_YAW = 0.0f;
    public static final float SCR_Y = 1000.0f;
    public static final Vector PLAYER = new Vector(0,800,450), OBS = new Vector(0.0f,0.0f,0.0f);

    public static class Vector {
        public float x, y, z;
        public Vector(float x, float y, float z){
            this.x=x;
            this.y=y;
            this.z=z;
        }

        public double sqlen() {
            return x*x + y*y + z*z;
        }
    }
    public static Vector mult(Vector v, float k){
        return new Vector(v.x*k, v.y*k, v.z*k);
    }
    public static Vector div(Vector v, float k){
        return new Vector(v.x/k, v.y/k, v.z/k);
    }

    public static Vector add(Vector u, Vector v){
        return new Vector(u.x+v.x, u.y+v.y, u.z+v.z);
    }
    public static Vector sub(Vector u, Vector v){ return new Vector(u.x-v.x,u.y-v.y,u.z-v.z);}


    public static Vector VX(float x, float y, float z){
        return new Vector(x,y,z);
    }
    public static Vector[] VXS(Vector... args){
        return args;
    }

    public static Vector getCentroid(Vector... points) {
        Vector res = new Vector(0.0f,0.0f,0.0f);
        for(Vector v : points){
            res = add(res,v);
        }
        res = div(res, (float) points.length);
        return res;
    }

    public static Vector yaw(Vector u, Vector o, float ang){
        Vector u2 = sub(u,o);
        float x2 = (float) (u2.x*cos(ang) - u2.y*sin(ang));
        float y2 = (float) (u2.x*sin(ang) + u2.y*cos(ang));
        Vector u3 = new Vector(x2,y2,u2.z);
        return add(u3,o);
    }

    public static Vector pitch(Vector u, Vector o, float ang){
        Vector u2 = sub(u,o);
        float y2 = (float) (u2.y*cos(ang) - u2.z*sin(ang));
        float z2 = (float) (u2.y*sin(ang) + u2.z*cos(ang));
        Vector u3 = new Vector(u2.x,y2,z2);
        return add(u3,o);
    }

    public static Vector roll(Vector u, Vector o, float ang){
        Vector u2 = sub(u,o);
        float x2 = (float) (u2.x*cos(ang) - u2.z*sin(ang));
        float z2 = (float) (u2.x*sin(ang) + u2.z*cos(ang));
        Vector u3 = new Vector(x2,u2.y,z2);
        return add(u3,o);
    }

    public static Vector getNormal(Vector ...points) {
        Vector point1 = points[0], point2 = points[1], point3 = points[2];
        Vector edge1 = sub(point2, point1);
        Vector edge2 = sub(point3, point1);
        float normalX = edge1.y * edge2.z - edge1.z * edge2.y;
        float normalY = edge1.z * edge2.x - edge1.x * edge2.z;
        float normalZ = edge1.x * edge2.y - edge1.y * edge2.x;
        Vector norm = new Vector(normalX, normalY, normalZ);
        double d = norm.sqlen();
        if(d==0.0){
            System.exit(0);
        }
        return div(norm, (float)(Math.sqrt(d)));
    }
}
