package com.example.game3d.engine3d;

import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Random;
import com.example.game3d.engine3d.Object3D.Face;
public class Util {
    public static float SCR_W, SCR_H;
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
    public static Vector VCPY(Vector v){
        return VX(v.x,v.y,v.z);
    }
    public static Vector VC(Vector v){
        return VX(v.x,v.y,v.z);
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
    public static Vector crossProduct(Vector v1, Vector v2) {
        float crossX = v1.y * v2.z - v1.z * v2.y;
        float crossY = v1.z * v2.x - v1.x * v2.z;
        float crossZ = v1.x * v2.y - v1.y * v2.x;
        return new Vector(crossX, crossY, crossZ);
    }
    public static double dotProduct(Vector v1, Vector v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }
    public static class Triangle{
        public Vector a,b,c;
        public Triangle(Vector i, Vector j, Vector k){
            a=i;
            b=j;
            c=k;
        }
    }
    public static double rayTriangleDistance(Vector rayOrigin,
                                               Vector rayVector,
                                               Triangle inTriangle) { // boring ahh linear algebra
        Vector vertex0 = inTriangle.a;
        Vector vertex1 = inTriangle.b;
        Vector vertex2 = inTriangle.c;
        Vector edge1 = sub(vertex1,vertex0);
        Vector edge2 = sub(vertex2,vertex0);
        double a, f, u, v;
        Vector h = crossProduct(rayVector, edge2);
        a = dotProduct(edge1,h);
        if (a > -0.0001 && a < 0.0001) {
            return 1e9;
        }
        f = 1.0 / a;
        Vector s = sub(rayOrigin, vertex0);
        u = f * (dotProduct(s,h));
        if (u < 0.0 || u > 1.0) {
            return 1e9;
        }
        Vector q = crossProduct(s, edge1);
        v = f * dotProduct(rayVector,q);
        if (v < 0.0 || u + v > 1.0) {
            return 1e9;
        }
        double res = f * dotProduct(edge2,q);
        return res >= 0 ? res : 1e9;
    }

    public static Vector getNormal(Vector point1, Vector point2, Vector point3) {
        Vector edge1 = sub(point2, point1);
        Vector edge2 = sub(point3, point1);
        float normalX = edge1.y * edge2.z - edge1.z * edge2.y;
        float normalY = edge1.z * edge2.x - edge1.x * edge2.z;
        float normalZ = edge1.x * edge2.y - edge1.y * edge2.x;
        Vector norm = new Vector(normalX, normalY, normalZ);
        double d = norm.sqlen();
        if(d==0.0){
            System.err.println("Normal length zero");
        }
        return div(norm, (float) sqrt((double)d));
    }


    public static double randDouble(double min, double max, int decimalDigits) {
        if (min > max || decimalDigits < 0) {
            throw new IllegalArgumentException("Invalid input values");
        }
        if(min==max){
            return min;
        }
        Random random = new Random();
        double randomValue = min + (random.nextDouble() * (max - min));
        double scaleFactor = Math.pow(10, decimalDigits);

        return Math.round(randomValue * scaleFactor) / scaleFactor;
    }

    public static int randInt(int min, int max) {
        return (int)(randDouble(min,max,0));
    }

    public static double randDoubleRanges(int decimalDigits, double... args){
        if((args.length & 1) == 1){
            throw new IllegalArgumentException("Odd number of args");
        }
        int n = args.length / 2;
        int ind = randInt(0,n-1);
        double l = args[ind*2], r = args[2*ind+1];
        return randDouble(l,r,decimalDigits);
    }

    public static int randIntRanges(int... args){
        if((args.length & 1) == 1){
            throw new IllegalArgumentException("Odd number of args");
        }
        int n = args.length / 2;
        int ind = randInt(0,n-1);
        int l = args[ind*2], r = args[2*ind+1];
        return randInt(l,r);
    }

    public static double getBrightness(int x, int y, int z){
        return 0.2126 * x + 0.7152 * y + 0.0722 * z;
    }

    public static double getBrightness(int color){
        return 0.2126*((color>>16)&255) + 0.7152*((color>>8)&255) + 0.0722*(color&255);
    }

    public static int adjustBrightness(int color, double mn, double mx){
        int x = ((color>>16)&255);
        int y = ((color>>8)&255);
        int z = (color&255);
        double brightness = getBrightness(x,y,z);
        if (brightness < mn) {
            double scale = mn / brightness;
            x = (int) ceil((double)(x) * scale);
            y = (int) ceil((double)(y) * scale);
            z = (int) ceil((double)(z) * scale);
        }
        if (mn != mx && brightness > mx) {
            double scale = mx / brightness;
            x = (int) floor((double)(x) * scale);
            y = (int) floor((double)(y) * scale);
            z = (int) floor((double)(z) * scale);
        }
        brightness = getBrightness(x,y,z);
        assert(brightness >=mn && brightness <=mx);
        return Color.rgb(x,y,z);
    }
    public static int adjustBrightness(int x, int y, int z, double mn, double mx){
        double brightness = getBrightness(x,y,z);
        if (brightness < mn) {
            double scale = mn / brightness;
            x = (int) ceil((double)(x) * scale);
            y = (int) ceil((double)(y) * scale);
            z = (int) ceil((double)(z) * scale);
        }
        if (mn != mx && brightness > mx) {
            double scale = mx / brightness;
            x = (int) floor((double)(x) * scale);
            y = (int) floor((double)(y) * scale);
            z = (int) floor((double)(z) * scale);
        }
        brightness = getBrightness(x,y,z);
        assert(brightness >=mn && brightness <=mx);
        return Color.rgb(x,y,z);
    }

    public static double maxAll(double ... args){
        double res=-1e9;
        for(double arg : args){
            res = max(res,arg);
        }
        return res;
    }

    public static double minAll(double ... args){
        double res=1e9;
        for(double arg : args){
            res = min(res,arg);
        }
        return res;
    }


    public static class Cuboid extends Object3D { // a cuboid, the edges of which are parallel to x,y,z axis
        public Vector cuboidMid;
        double a,b,c;  // dimensions: a - x axis, b - y, c - z

        public Cuboid(Vector mid, int a, int b, int c) {
            super(new Vector[]{
                    VX(mid.x-a/2,mid.y-b/2,mid.z-c/2),  // 0
                    VX(mid.x+a/2,mid.y-b/2,mid.z-c/2),  // 1
                    VX(mid.x+a/2,mid.y+b/2,mid.z-c/2),  // 2
                    VX(mid.x-a/2,mid.y+b/2,mid.z-c/2),  // 3

                    VX(mid.x-a/2,mid.y-b/2,mid.z+c/2),  // 4
                    VX(mid.x+a/2,mid.y-b/2,mid.z+c/2),  // 5
                    VX(mid.x+a/2,mid.y+b/2,mid.z+c/2),  // 6
                    VX(mid.x-a/2,mid.y+b/2,mid.z+c/2),  // 7

            },new Face[]{
                    FC(Color.TRANSPARENT,Color.RED,0,1,2,3),
                    FC(Color.TRANSPARENT,Color.GREEN,1,2,6,5),
                    FC(Color.TRANSPARENT,Color.WHITE,5,4,0,1),

                    FC(Color.TRANSPARENT,Color.CYAN,4,5,6,7),
                    FC(Color.TRANSPARENT,Color.YELLOW,0,3,7,4),
                    FC(Color.TRANSPARENT,Color.MAGENTA,2,6,7,3),

            });
            is_obs = true;
            this.cuboidMid=mid;
            this.a=a;
            this.b=b;
            this.c=c;
        }
        public boolean intersectsCuboid(Cuboid other) {
            if (Math.abs(this.cuboidMid.x - other.cuboidMid.x) > (this.a/2 + other.a/2)) {
                return false; // No intersection if there's separation along X
            }
            if (Math.abs(this.cuboidMid.y - other.cuboidMid.y) > (this.b/2 + other.b/2)) {
                return false; // No intersection if there's separation along Y
            }
            if (Math.abs(this.cuboidMid.z - other.cuboidMid.z) > (this.c/2 + other.c/2)) {
                return false; // No intersection if there's separation along Z
            }
            return true;
        }
    }

    public static Cuboid getBoundingCuboid(Vector... points) {
        if (points == null || points.length == 0) {
            return null;
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float maxZ = Float.MIN_VALUE;

        for (Vector point : points) {
            minX = (float) min(minX,point.x);
            minY = (float) min(minY,point.y);
            minZ = (float) min(minZ,point.z);

            maxX = (float) max(maxX,point.x);
            maxY = (float) max(maxY,point.y);
            maxZ = (float) max(maxZ,point.z);
        }
        float a = maxX - minX;
        float b = maxY - minY;
        float c = maxZ - minZ;
        Vector cuboidMid = new Vector(((maxX + minX) / 2), ((maxY + minY) / 2), ((maxZ + minZ) / 2));
        return new Cuboid(cuboidMid, (int) a, (int) b, (int) c);
    }

    public static double rayCuboidDistance(Vector rayOrigin, Vector rayVector, Cuboid cuboid){  // intersect ray and cuboid (edges of cuboid are parallel to x,y,z axes)
        Vector cuboidMid = sub(cuboid.cuboidMid,rayOrigin);
        double x0 = cuboidMid.x - cuboid.a/2, x1 = cuboidMid.x + cuboid.a/2;
        double y0 = cuboidMid.y - cuboid.b/2, y1 = cuboidMid.y + cuboid.b/2;
        double z0 = cuboidMid.z - cuboid.c/2, z1 = cuboidMid.z + cuboid.c/2;
        double t0 = maxAll(x0/(rayVector.x + 0.001*signum(rayVector.x)), y0/(rayVector.y + 0.001*signum(rayVector.y)), z0/(rayVector.z + 0.001*signum(rayVector.z)));
        double t1 = minAll(x1/(rayVector.x + 0.001*signum(rayVector.x)), y1/(rayVector.y + 0.001*signum(rayVector.y)), z1/(rayVector.z + 0.001*signum(rayVector.z)));
        if(t0>t1){
            return 1e9;
        }
        return t0;
    }

}
