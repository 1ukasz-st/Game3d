package com.example.game3d.engine3d;

import static com.example.game3d.GameView.ASSET_MANAGER;
import static com.example.game3d.elements.Player.CAM_YAW;
import static com.example.game3d.engine3d.Util.PLAYER;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.SCR_Y;
import static com.example.game3d.engine3d.Util.VC;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.VXS;
import static com.example.game3d.engine3d.Util.Vector;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.getCentroid;
import static com.example.game3d.engine3d.Util.mult;
import static com.example.game3d.engine3d.Util.pitch;
import static com.example.game3d.engine3d.Util.roll;
import static com.example.game3d.engine3d.Util.sub;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Object3D {
    public static float MAX_Y = 25000;
    public static Paint strokePaint = new Paint();
    public static Paint fillPaint = new Paint();
    public ObjectFace[] faces;
    public float yaw = 0.0f, pitch = 0.0f, roll = 0.0f;
    protected Vector[] verts, tVerts;
    protected boolean facesSorted;
    private Vector centerMass = null, rotatedCenter = null;
    private float rectLeft,rectRight,rectTop, rectBottom;
    public float getRectLeft() {
        return rectLeft;
    }
    public float getRectRight() {
        return rectRight;
    }
    public float getRectTop() {
        return rectTop;
    }
    public float getRectBottom() {
        return rectBottom;
    }
    protected boolean is_obs=false;

    public int nVerts(){
        return verts.length;
    }

    public void move(Vector v){
        for(int i=0;i<verts.length;++i){
            verts[i].x+=v.x;
            verts[i].y+=v.y;
            verts[i].z+=v.z;
        }
    }

    public static Pair<Vector[], Face[]> loadFromFile(String filename, int color, int ecolor, Vector mid, float sx, float sy, float sz, float init_yaw, float init_pitch, float init_roll) throws IOException {
        try {
            InputStream inputStream = ASSET_MANAGER.open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            ArrayList<Vector> vertexList = new ArrayList<>();
            ArrayList<Face> faceList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() < 2) {
                    continue;
                }
                if (line.charAt(0) == 'v') {
                    String[] args = line.split(" ");
                    if (line.charAt(1) == ' ') {
                        Vector curr = VX(Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3]));
                        vertexList.add(curr);
                    }
                } else if (line.charAt(0) == 'f') {
                    String[] args = line.split(" ");
                    int[] inds = new int[args.length - 1];
                    Vector norm = null;
                    for (int ind = 1; ind < args.length; ++ind) {
                        String[] info = args[ind].split("/");
                        inds[ind - 1] = Integer.parseInt(info[0]) - 1;
                    }
                    faceList.add(new Face(color, ecolor, inds));
                }
            }
            Vector[] verts = vertexList.toArray(new Vector[0]);

            float maxx = -1000000.0f, minx = 1000000.0f;
            float maxy = -1000000.0f, miny = 1000000.0f;
            float maxz = -1000000.0f, minz = 1000000.0f;
            Vector currMid = getCentroid(verts);
            for (int i = 0; i < verts.length; ++i) {
                verts[i] = sub(verts[i],currMid);
            }
            for (int i = 0; i < verts.length; ++i) {
                if (init_yaw != 0.0f) {
                    verts[i] = yaw(verts[i], VX(0,0,0), init_yaw);
                }
                if (init_pitch != 0.0f){
                    verts[i] = pitch(verts[i], VX(0,0,0), init_pitch);
                }
                if (init_roll != 0.0f) {
                    verts[i] = roll(verts[i], VX(0,0,0), init_roll);
                }
                maxx = max(maxx, verts[i].x);
                minx = min(minx, verts[i].x);
                maxy = max(maxy, verts[i].y);
                miny = min(miny, verts[i].y);
                maxz = max(maxz, verts[i].z);
                minz = min(minz, verts[i].z);
            }
            float x_ratio = sx / (maxx - minx), y_ratio = sy / (maxy - miny), z_ratio = sz / (maxz - minz);
            for (int i = 0; i < verts.length; ++i) {
                verts[i].x *= x_ratio;
                verts[i].y *= y_ratio;
                verts[i].z *= z_ratio;
                verts[i] = add(verts[i], mid);
            }
            reader.close();
            return new Pair<>(verts,faceList.toArray(new Face[0]));
        } catch (IOException e) {
            throw new IOException();
        }
    }

    public Object3D(String filename, int color, int ecolor, Vector mid, float sx, float sy, float sz, float init_yaw, float init_pitch, float init_roll) throws IOException {
        Pair<Vector[], Face[]> data = loadFromFile(filename,color,ecolor,mid,sx,sy,sz,init_yaw,init_pitch,init_roll);
        verts = data.first;
        faces = new ObjectFace[data.second.length];
        for(int i=0;i<faces.length;++i){
            faces[i] = new ObjectFace(data.second[i].color,data.second[i].ecolor,data.second[i].inds);
        }
        facesSorted = true;
        tVerts = new Vector[verts.length];
        path.setFillType(Path.FillType.EVEN_ODD);
    }
    public Object3D(Vector[] verts, Face[] faces) {
        this.verts = new Vector[verts.length];
        for(int i=0;i<verts.length;++i){
            this.verts[i] = VC(verts[i]);
        }
        tVerts = new Vector[verts.length];
        this.faces = new ObjectFace[faces.length];
        for (int i = 0; i < faces.length; ++i) {
            this.faces[i] = new ObjectFace(faces[i].color, faces[i].ecolor, faces[i].inds);
        }
        this.facesSorted = false;
        path.setFillType(Path.FillType.EVEN_ODD);

    }

    public Object3D(Vector[] verts, Face[] faces, boolean facesSorted) {
        this.verts = new Vector[verts.length];
        for(int i=0;i<verts.length;++i){
            this.verts[i] = VC(verts[i]);
        }
        tVerts = new Vector[verts.length];
        this.faces = new ObjectFace[faces.length];
        for (int i = 0; i < faces.length; ++i) {
            this.faces[i] = new ObjectFace(faces[i].color, faces[i].ecolor, faces[i].inds);
        }
        this.facesSorted = facesSorted;
        path.setFillType(Path.FillType.EVEN_ODD);

    }

    public static Face FC(int color, int ecolor, int... inds) {
        return new Face(color, ecolor, inds);
    }

    public static Face[] FCS(Face... args) {
        return args;
    }

    /*
         6         7
           +--------+     ^ Z
        4 /|     5 /|     |
         +-|------+ |     |   ^ Y
         | +-2----|-+ 3   |  /
         |/       |/      | /
         +--------+       |/-----> X
         0       1
    */
    public static Object3D makeCube(Vector mid, float lx, float ly, float lz, int ecolor) {
        float x = mid.x;
        float y = mid.y;
        float z = mid.z;
        return new Object3D(
                VXS(
                        VX(x - lx / 2f, y - ly / 2f, z + lz / 2f),
                        VX(x + lx / 2f, y - ly / 2f, z + lz / 2f),
                        VX(x - lx / 2f, y + ly / 2f, z + lz / 2f),
                        VX(x + lx / 2f, y + ly / 2f, z + lz / 2f),
                        VX(x - lx / 2f, y - ly / 2f, z - lz / 2f),
                        VX(x + lx / 2f, y - ly / 2f, z - lz / 2f),
                        VX(x - lx / 2f, y + ly / 2f, z - lz / 2f),
                        VX(x + lx / 2f, y + ly / 2f, z - lz / 2f)
                ),
                FCS(
                        FC(Color.TRANSPARENT, ecolor, 0, 1, 3, 2),
                        FC(Color.TRANSPARENT, ecolor, 4, 5, 7, 6),
                        FC(Color.TRANSPARENT, ecolor, 0, 1, 5, 4),
                        FC(Color.TRANSPARENT, ecolor, 2, 3, 7, 6),
                        FC(Color.TRANSPARENT, ecolor, 0, 2, 6, 4),
                        FC(Color.TRANSPARENT, ecolor, 1, 3, 7, 5)
                ));
    }

    public static Vector project(Vector vert) {
        if(vert.y<0){
            return VX(-10000,-10000,-10000);
        }
        return mult(vert,  SCR_Y / vert.y);
    }

    public static Vector moveToScreen = VX(0,0,0);
    public Vector vertex(int ind) {
        return tVerts[ind];
    }
    public Vector pVertex(int ind) {
        return add(project(tVerts[ind]),moveToScreen);
    }

    public boolean outOfScreen(){
        float maxx=-1e9f,maxz=-1e9f;
        float minx=1e9f,minz=1e9f;
        for(int i=0;i<nVerts();++i){
            Vector v = pVertex(i);
            maxx = max(maxx,v.x);
            minx = min(minx,v.x);
            maxz = max(maxz,v.z);
            minz = min(minz,v.z);
        }
        return maxx<0 || minx>SCR_W || maxz<0 || minz>SCR_H;
    }
    public boolean slightlyOutOfScreen(){
        float maxx=-1e9f,maxz=-1e9f;
        float minx=1e9f,minz=1e9f;
        for(int i=0;i<nVerts();++i){
            Vector v = pVertex(i);
            maxx = max(maxx,v.x);
            minx = min(minx,v.x);
            maxz = max(maxz,v.z);
            minz = min(minz,v.z);
        }
        return minx<0 || maxx>SCR_W || minz<0 || minz>SCR_H;
    }
    public Vector centroid() {
        return VX(rotatedCenter.x, rotatedCenter.y, rotatedCenter.z);
    }

    private Vector rotVert(Vector vert){
        Vector res = VX(vert.x, vert.y, vert.z);
        if (roll != 0.0f)  res= roll(res, centerMass, roll);
        if (pitch != 0.0f) res = pitch(res, centerMass, pitch);
        if (yaw != 0.0f) res = yaw(res, centerMass, yaw);
        if (CAM_YAW != 0.0f && !is_obs) res = yaw(res, PLAYER, CAM_YAW);
        return res;
    }
    public int partitionFaces() {
        int i = 0;
        int j = faces.length-1;
        while (i <= j) {
            while (i <= j && !_faceSkipped(faces[i])) {
                i++;
            }
            while (i <= j && _faceSkipped(faces[j])) {
                j--;
            }
            if (i <= j) {
                ObjectFace temp = faces[i];
                faces[i] = faces[j];
                faces[j] = temp;
                i++;
                j--;
            }
        }
        return i-1;
    }

    int lastToDraw = -1;

    public void calculate(){
        if(centerMass==null){
            centerMass = getCentroid(verts);
        }
        rectLeft = 1e9f;
        rectRight = -1e9f;
        rectTop = 1e9f;
        rectBottom = -1e9f;

        for (int i = 0; i < verts.length; ++i) {
            tVerts[i] = rotVert(verts[i]);
            Vector p = pVertex(i);
            rectLeft = min(rectLeft,p.x);
            rectRight = max(rectRight,p.x);
            rectTop = min(rectTop,p.z);
            rectBottom = max(rectBottom,p.z);
        }
        rotatedCenter = rotVert(centerMass);
        lastToDraw = partitionFaces();
        if (facesSorted && !oneColorAndFace) {
            Arrays.sort(faces, 0, lastToDraw+1, new Painter());
        }
        valid = true;
    }

    protected boolean valid = false;
    public boolean isValid(){
        return valid;
    }
    private final Path path = new Path();



    protected boolean faceSkipped(ObjectFace face){
        return false;
    }
    private boolean _faceSkipped(ObjectFace face){
        boolean sk =faceSkipped(face);
        if(sk){
            ++facesSkipped;
        }
        return sk;
    }

    public int facesSkipped = 0;

    protected boolean oneColorAndFace = false;
    public boolean draw(Canvas canvas) {
        centerMass = getCentroid(verts);
        facesSkipped=0;
        if(rotVert(verts[0]).y>MAX_Y){
            return false;
        }
        if(!isValid()) {
            calculate();
        }
        if(outOfScreen()){
            return false;
        }
        if(oneColorAndFace && faces[0].color != Color.TRANSPARENT){
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(faces[0].color);
            path.rewind();
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(faces[0].ecolor);
            path.setFillType(Path.FillType.WINDING);
            for (int i = 0; i <= lastToDraw; ++i) {
                ObjectFace face = faces[i];
                boolean first = true;
                for (int ind : face.inds) {
                    if (tVerts[ind].y < 0) {
                        continue;
                    }
                    Vector projected = project(tVerts[ind]);
                    if (first) {
                        path.moveTo(projected.x + SCR_W / 2f, projected.z + SCR_H / 2f);
                        first = false;
                    } else {
                        path.lineTo(projected.x + SCR_W / 2f, projected.z + SCR_H / 2f);
                    }
                }
                path.close();
            }
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
        }else if(!oneColorAndFace){
            for (int i = 0; i <= lastToDraw; ++i) {
                ObjectFace face = faces[i];
                if (face.ecolor != Color.TRANSPARENT) {
                    strokePaint.setStyle(Paint.Style.STROKE);
                    strokePaint.setColor(face.ecolor);
                }
                path.rewind();
                fillPaint.setStyle(Paint.Style.FILL);
                fillPaint.setColor(face.color);
                boolean first = true;
                for (int ind : face.inds) {
                    if (tVerts[ind].y < 0) {
                        continue;
                    }
                    Vector projected = project(tVerts[ind]);
                    if (first) {
                        path.moveTo(projected.x + SCR_W / 2f, projected.z + SCR_H / 2f);
                        first = false;
                    } else {
                        path.lineTo(projected.x + SCR_W / 2f, projected.z + SCR_H / 2f);
                    }
                }
                path.close();
                canvas.drawPath(path, fillPaint);
                if (face.ecolor != Color.TRANSPARENT) {
                    canvas.drawPath(path, strokePaint);
                }
            }
        }
        return true;
    }

   /* public boolean collides(Object3D other){
    }*/

    public void invalidate() {
        valid = false;
        centerMass = null;
        rotatedCenter = null;
        for (int i = 0; i < verts.length; ++i) {
            tVerts[i] = null;
        }
    }

    public static class Face {
        public int color, ecolor;
        public int[] inds;

        public Face(int color, int ecolor, int... inds) {
            this.color = color;
            this.ecolor = ecolor;
            this.inds = inds;
        }
    }

    public class ObjectFace extends Face {

        public ObjectFace(int color, int ecolor, int... inds) {
            super(color, ecolor, inds);
        }

        public double getDepth() {
            double total = 0.0;
            double inum = 1.0f/((float)(inds.length));
            for (int ind : inds) {
                double x = tVerts[ind].x, y = tVerts[ind].y, z = tVerts[ind].z;
                total += (x * x) + (y * y)  + (z * z) ;
            }
            return total*inum;
        }
    }

    private class Painter implements Comparator<ObjectFace> {
        @Override
        public int compare(ObjectFace f0, ObjectFace f1) {
            return (int) -Math.signum(f0.getDepth() - f1.getDepth());
        }
    }
}