package com.example.game3d.engine3d;

import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.NonNull;

import java.util.Random;

public class Util {
    public static float SCR_W, SCR_H;
    public static final Vector PLAYER = new Vector(0,800,450), OBS = new Vector(0.0f,0.0f,0.0f);

    public static Typeface GAMEFONT;
    public static int DEFAULT_COLOR = Color.rgb(100,255,255);

    public static float PI = (float)(Math.PI);

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
    public static Vector VX(double x, double y, double z){
        return new Vector((float) x, (float) y, (float) z);
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
    public static float dotProduct(Vector v1, Vector v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }
    public static Vector getNormal(Vector point1, Vector point2, Vector point3) {
        Vector edge1 = sub(point2, point1);
        Vector edge2 = sub(point3, point1);
        float normalX = edge1.y * edge2.z - edge1.z * edge2.y;
        float normalY = edge1.z * edge2.x - edge1.x * edge2.z;
        float normalZ = edge1.x * edge2.y - edge1.y * edge2.x;
        Vector norm = new Vector(normalX, normalY, normalZ);
        double d = norm.sqlen();
        if(d<1e-9){
            System.err.println("Normal length zero");
        }
        return div(norm, (float) sqrt(d));
    }
    public static float randFloat(float min, float max, int decimalDigits) {
        if (min > max || decimalDigits < 0) {
            throw new IllegalArgumentException("Invalid input values");
        }
        if(min==max){
            return min;
        }
        Random random = new Random();
        double randomValue = min + (random.nextDouble() * (max - min));
        double scaleFactor = pow(10, decimalDigits);

        return (float) (Math.round(randomValue * scaleFactor) / scaleFactor);
    }
    public static int randInt(int min, int max) {
        return (int)(randFloat(min,max,0));
    }
    public static float randFloatRanges(int decimalDigits, float... args){
        if((args.length & 1) == 1){
            throw new IllegalArgumentException("Odd number of args");
        }
        int n = args.length / 2;
        int ind = randInt(0,n-1);
        float l = args[ind*2], r = args[2*ind+1];
        return randFloat(l,r,decimalDigits);
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
    public static float getBrightness(int x, int y, int z){
        return (float) floor(0.2126f * x + 0.7152f * y + 0.0722f * z);
    }
    public static int red(int color){
        return (color>>16)&255;
    }
    public static int green(int color){
        return (color>>8)&255;
    }
    public static int blue(int color){
        return (color )&255;
    }
    public static float getBrightness(int color){
        return (float) floor(0.2126f*red(color) + 0.7152f*green(color) + 0.0722f*blue(color));
    }
    public static int multBrightness(int color, float f){
        int x = red(color);
        int y = green(color);
        int z = blue(color);
        x = (int) min(255,ceil(x * f));
        y = (int) min(255,ceil(y * f));
        z = (int) min(255,ceil(z * f));
        return Color.rgb(x,y,z);
    }
    public static int adjustBrightness(int color, float mn, float mx){
        float brightness = getBrightness(color);
        if(brightness < mn){
            ++mn;
            color = multBrightness(color,mn/brightness);
            brightness = getBrightness(color);
            if(brightness < mn) {
                int r = red(color), g = green(color), b = blue(color);
                if(r != 0){
                    r = (int) min(255,(mn-0.7152f*g-0.0722f*b)/0.2126f);
                    color = Color.rgb(r,g,b);
                    brightness = getBrightness(color);
                }
                if(brightness<mn){
                    if(g != 0){
                        g = (int) min(255,(mn-0.2126f*r-0.0722f*b)/0.7152f);
                        color = Color.rgb(r,g,b);
                        brightness = getBrightness(color);
                    }
                    if(brightness<mn){
                        if(b != 0){
                            g = (int) min(255,(mn-0.2126f*r-0.7152f*g)/0.0722f);
                            color = Color.rgb(r,g,b);
                        }
                    }
                }
            }
        }
        brightness = getBrightness(color);
        if(brightness > mx){
            color = multBrightness(color,mx/brightness);
        }
        brightness = getBrightness(color);
        return color;
    }
    public static int adjustBrightness(int x, int y, int z, float mn, float mx){
        double brightness = getBrightness(x,y,z);
        if (brightness < mn) {
            double scale = mn / brightness;
            x = (int) ceil(x * scale);
            y = (int) ceil(y * scale);
            z = (int) ceil(z * scale);
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
    public static int randomColor(int minBrightness,int maxBrightness){
        int x = randInt(75,255-30);
        int y = randInt(30,255-x);
        int z = 255 - x - y;

        int diceRoll = randInt(1,6);
        if(diceRoll <= 2){
            if(diceRoll == 1){
                return adjustBrightness(x,y,z,minBrightness,maxBrightness);
            }else{
                return adjustBrightness(x,z,y,minBrightness,maxBrightness);
            }
        }else if(diceRoll<=4){
            if(diceRoll == 3){
                return adjustBrightness(y,x,z,minBrightness,maxBrightness);
            }else{
                return adjustBrightness(y,z,x,minBrightness,maxBrightness);
            }
        }else{
            if(diceRoll == 5){
                return adjustBrightness(z,x,y,minBrightness,maxBrightness);
            }else{
                return adjustBrightness(z,y,x,minBrightness,maxBrightness);
            }
        }
    }
    public static int randomDistantColor(int currColor, int minBrightness, int maxBrightness){
        int r = (currColor >> 16) & 0xFF;
        int g = (currColor >> 8) & 0xFF;
        int b = currColor & 0xFF;

        int color = randomColor(minBrightness,maxBrightness);
        int r2 = (color >> 16) & 0xFF;
        int g2 = (color >> 8) & 0xFF;
        int b2 = color & 0xFF;
        if((r-r2)*(r-r2) + (g-g2)*(g-g2) + (b-b2)*(b-b2) < 75*75){ // default distance is 75
            return randomDistantColor(currColor,minBrightness,maxBrightness);
        }
        return color;
    }
    public static int randomDistantColor(int currColor, int minBrightness, int maxBrightness, int distance){
        int r = (currColor >> 16) & 0xFF;
        int g = (currColor >> 8) & 0xFF;
        int b = currColor & 0xFF;

        int color = randomColor(minBrightness,maxBrightness);
        int r2 = (color >> 16) & 0xFF;
        int g2 = (color >> 8) & 0xFF;
        int b2 = color & 0xFF;
        if((r-r2)*(r-r2) + (g-g2)*(g-g2) + (b-b2)*(b-b2) < distance*distance){
            return randomDistantColor(currColor,minBrightness,maxBrightness);
        }
        return color;
    }
    public static int getColorCloser(int currColor, int targetColor){
        int r = (currColor >> 16) & 0xFF;
        int g = (currColor >> 8) & 0xFF;
        int b = currColor & 0xFF;

        int r2 = (targetColor >> 16) & 0xFF;
        int g2 = (targetColor >> 8) & 0xFF;
        int b2 = targetColor & 0xFF;

        if (r < r2) {
            ++r;
        } else if (r > r2) {
            --r;
        }

        if (g < g2) {
            ++g;
        } else if (g > g2) {
            --g;
        }

        if (b < b2) {
            ++b;
        } else if (b > b2) {
            --b;
        }
        return (255<<24) | (r<<16) | (g<<8) | b;
    }

    public static double maxAll(@NonNull double ... args){
        double res=-1e9;
        for(double arg : args){
            res = max(res,arg);
        }
        return res;
    }

    public static double minAll(@NonNull double ... args){
        double res=1e9;
        for(double arg : args){
            res = min(res,arg);
        }
        return res;
    }


    public static class Cuboid extends Object3D { // a cuboid, the edges of which are parallel to x,y,z axis
        public Vector cuboidMid;
        double a,b,c;  // dimensions: a - x axis, b - y, c - z

        public Cuboid(Vector mid, float a, float b, float c) {
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
        public boolean intersectsCuboid(@NonNull Cuboid other) {
            if (abs(this.cuboidMid.x - other.cuboidMid.x) > (this.a/2 + other.a/2)) {
                return false; // No intersection if there's separation along X
            }
            if (abs(this.cuboidMid.y - other.cuboidMid.y) > (this.b/2 + other.b/2)) {
                return false; // No intersection if there's separation along Y
            }
            if (abs(this.cuboidMid.z - other.cuboidMid.z) > (this.c/2 + other.c/2)) {
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

    public static Vector randomPointInTriangle(Vector a, Vector b, Vector c) {
        float r1 = (float)Math.sqrt(Math.random());
        float r2 = (float)Math.random();

        float lambda1 = 1 - r1;
        float lambda2 = r1 * (1 - r2);
        float lambda3 = r1 * r2;

        return add(add(mult(a,lambda1),mult(b,lambda2)),mult(c,lambda3));
    }

    public static boolean isPointInTriangle(Vector a, Vector b, Vector c, Vector p) {
        // Compute vectors
        Vector v0 = sub(b, a);
        Vector v1 = sub(c, a);
        Vector v2 = sub(p, a);

        // Compute dot products
        float dot00 = dotProduct(v0, v0);
        float dot01 = dotProduct(v0, v1);
        float dot02 = dotProduct(v0, v2);
        float dot11 = dotProduct(v1, v1);
        float dot12 = dotProduct(v1, v2);

        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;
        return (u >= 0) && (v >= 0) && (u + v < 1);
    }


    public static int pointAndPlanePosition(Vector A, Vector B, Vector C, Vector P) {
        Vector AB = sub(B, A);
        Vector AC = sub(C, A);
        Vector AP = sub(P, A);

        Vector normal = crossProduct(AB, AC);
        double dotProduct = dotProduct(normal, AP);

        if (dotProduct > 0) {
            return 1; // P is to the right of the plane
        } else if (dotProduct < 0) {
            return -1; // P is to the left of the plane
        } else {
            return 0; // P is on the plane
        }
    }

    public static float choice(float... args){
        return args[randInt(0,args.length-1)];
    }

    public static float roundTo(float what, int digits){
        long mult = (long) (what*pow(10,digits));
        return (float)((float)(mult/pow(10,digits)));
    }

    public static class Rect{
        public final float x1,y1,x2,y2;
        public Rect(float x1, float y1, float x2, float y2){
            this.x1=x1;
            this.y1=y1;
            this.x2=x2;
            this.y2=y2;
        }
        public boolean contains(float x, float y){
            return x>=x1 && x<=x2 && y>=y1 && y<=y2;
        }
    }



}
