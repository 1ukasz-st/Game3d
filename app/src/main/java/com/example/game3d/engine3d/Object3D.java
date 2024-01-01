package com.example.game3d.engine3d;

import static com.example.game3d.GameView.ASSET_MANAGER;
import static com.example.game3d.engine3d.Util.CAM_YAW;
import static com.example.game3d.engine3d.Util.PLAYER;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.SCR_Y;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Object3D {
    public ObjectFace[] faces;
    public float yaw = 0.0f, pitch = 0.0f, roll = 0.0f;
    protected Vector[] verts, tVerts;
    protected boolean facesSorted;
    private Vector centerMass = null;

    public Object3D(String filename, int color, int ecolor, Vector mid, float sx, float sy, float sz, float init_yaw, float init_pitch, float init_roll) throws IOException {
        try {
            InputStream inputStream = ASSET_MANAGER.open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            ArrayList<Vector> vertexList = new ArrayList<>();
            ArrayList<ObjectFace> faceList = new ArrayList<>();
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
                    faceList.add(new ObjectFace(color, ecolor, inds));
                }
            }
            verts = vertexList.toArray(new Vector[0]);
            Vector currMid = getCentroid(verts);
            float maxx = -1000000.0f, minx = 1000000.0f;
            float maxy = -1000000.0f, miny = 1000000.0f;
            float maxz = -1000000.0f, minz = 1000000.0f;
            for (int i = 0; i < verts.length; ++i) {
                if (init_yaw != 0.0f) verts[i] = yaw(verts[i], currMid, init_yaw);
                if (init_pitch != 0.0f) verts[i] = pitch(verts[i], currMid, init_pitch);
                if (init_roll != 0.0f) verts[i] = roll(verts[i], currMid, init_roll);
                maxx = max(maxx, verts[i].x);
                minx = min(minx, verts[i].x);
                maxy = max(maxy, verts[i].y);
                miny = min(miny, verts[i].y);
                maxz = max(maxz, verts[i].z);
                minz = min(minz, verts[i].z);
            }
            float x_ratio = sx / (maxx - minx), y_ratio = sy / (maxy - miny), z_ratio = sz / (maxz - minz);
            for (int i = 0; i < verts.length; ++i) {
                verts[i] = sub(verts[i], currMid);
                verts[i].x *= x_ratio;
                verts[i].y *= y_ratio;
                verts[i].z *= z_ratio;
                verts[i] = add(verts[i], mid);
            }
            faces = faceList.toArray(new ObjectFace[0]);
            reader.close();
            tVerts = new Util.Vector[verts.length];
            facesSorted = true;
        } catch (IOException e) {
            throw new IOException();
        }
    }
    public Object3D(Vector[] verts, Face[] faces) {
        this.verts = verts;
        tVerts = new Vector[verts.length];
        this.faces = new ObjectFace[faces.length];
        for (int i = 0; i < faces.length; ++i) {
            this.faces[i] = new ObjectFace(faces[i].color, faces[i].ecolor, faces[i].inds);
        }
        this.facesSorted = false;
    }
    public Object3D(Vector[] verts, Face[] faces, boolean facesSorted) {
        this.verts = verts;
        tVerts = new Vector[verts.length];
        this.faces = new ObjectFace[faces.length];
        for (int i = 0; i < faces.length; ++i) {
            this.faces[i] = new ObjectFace(faces[i].color, faces[i].ecolor, faces[i].inds);
        }
        this.facesSorted = facesSorted;
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

    private Vector project(Vector vert) {
        return mult(vert, 1.0f - (vert.y - SCR_Y) / vert.y);
    }

    public Vector vertex(int ind) {
        return tVerts[ind];
    }

    public Vector centroid() {
        return VX(centerMass.x, centerMass.y, centerMass.z);
    }

    protected void update(Canvas canvas) {
    }

    public void draw(Canvas canvas) {
        centerMass = getCentroid(verts);
        for (int i = 0; i < verts.length; ++i) {
            tVerts[i] = VX(verts[i].x, verts[i].y, verts[i].z);
            if (yaw != 0.0f) tVerts[i] = yaw(tVerts[i], centerMass, yaw);
            if (pitch != 0.0f) tVerts[i] = pitch(tVerts[i], centerMass, pitch);
            if (roll != 0.0f) tVerts[i] = roll(tVerts[i], centerMass, roll);
            if (CAM_YAW != 0.0f) tVerts[i] = yaw(tVerts[i], PLAYER, CAM_YAW);
        }
        if (facesSorted) {
            Arrays.sort(faces, new Painter());
        }
        Paint strokePaint = new Paint();
        Paint fillPaint = new Paint();
        for (ObjectFace face : faces) {
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(face.ecolor);
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(face.color);
            Path path = new Path();
            boolean first = true;
            for (int ind : face.inds) {
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
            canvas.drawPath(path, strokePaint);
        }
    }

    public void invalidate() {
        centerMass = null;
        for (int i = 0; i < verts.length; ++i) {
            tVerts[i] = null;
        }
    }

    public static class Face {
        int color, ecolor;
        int[] inds;

        public Face(int color, int ecolor, int... inds) {
            this.color = color;
            this.ecolor = ecolor;
            this.inds = inds;
        }
    }

    private class ObjectFace extends Face {
        public ObjectFace(int color, int ecolor, int... inds) {
            super(color, ecolor, inds);
        }

        public double getDepth() {
            double total = 0.0;
            double num = inds.length;
            for (int ind : inds) {
                double x = tVerts[ind].x, y = tVerts[ind].y, z = tVerts[ind].z;
                total += (x * x) / num + (y * y) / num + (z * z) / num;
            }
            return total;
        }
    }

    private class Painter implements Comparator<ObjectFace> {
        @Override
        public int compare(ObjectFace f0, ObjectFace f1) {
            return (int) -Math.signum(f0.getDepth() - f1.getDepth());
        }
    }
}