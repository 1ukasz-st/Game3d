package com.example.game3d.elements.interactables;

import static com.example.game3d.GameView.getColorTheme;
import static com.example.game3d.engine3d.Util.Cuboid;
import static com.example.game3d.engine3d.Util.DEFAULT_COLOR;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.VXS;
import static com.example.game3d.engine3d.Util.Vector;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.mult;
import static com.example.game3d.engine3d.Util.multBrightness;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.sub;
import static java.lang.Math.abs;

import android.graphics.Color;

import com.example.game3d.elements.Player;
import com.example.game3d.elements.WorldElement;


public class DeathSpike extends WorldElement {
    public int a, c;
    private Cuboid cuboid;

    public DeathSpike(Vector midP, int a, int c) {
        super(VXS(
                        new Vector(midP.x - a / 2, midP.y + a / 2, midP.z),
                        new Vector(midP.x + a / 2, midP.y + a / 2, midP.z),
                        new Vector(midP.x + a / 2, midP.y - a / 2, midP.z),
                        new Vector(midP.x - a / 2, midP.y - a / 2, midP.z),
                        new Vector(midP.x, midP.y, midP.z - c)
                ), FCS(
                        new Face(DEFAULT_COLOR, Color.WHITE, 0, 1, 4),
                        new Face(DEFAULT_COLOR, Color.WHITE, 1, 2, 4),
                        new Face(DEFAULT_COLOR, Color.WHITE, 2, 3, 4),
                        new Face(DEFAULT_COLOR, Color.WHITE, 3, 0, 4)
                )
                //new Face(multBrightness(game.getGenerator().tileColor,0.65f),Color.WHITE,0,1,2,3),
                , false);
        this.a = a;
        this.c = c;
    }

    @Override
    public void calculate() {
        for (Face fc : faces) {
            fc.color = multBrightness(getColorTheme(), 0.65f);
            // fc.ecolor = multBrightness(color,1.1f);
        }
        super.calculate();
        cuboid = new Cuboid(centroid(), a, a, c);

    }

    @Override
    public boolean collidesPlayer(Player player) {
        if (abs(vertex(0).y) > 2000) {
            return false;
        }
        return cuboid.intersectsCuboid(player.cuboid);
    }

    public Vector scaledV(int i){
        Vector v = vertex(i);
        Vector dist = sub(v,centroid());
        return add(v,mult(dist,1.5f));
    }

    @Override
    public void interactWithPlayer(Player player) {
        player.die();
    }

    @Override
    protected boolean faceSkipped(ObjectFace fc) {
        //Vector normal = getNormal(vertex(fc.inds[0]),vertex(fc.inds[1]),vertex(fc.inds[2]));
        //normal = div(normal, (float) sqrt(normal.sqlen()));
        //return normal.y > 0.1;
        return pointAndPlanePosition(vertex(fc.inds[0]), vertex(fc.inds[1]), vertex(fc.inds[2]), OBS) == -1;
    }
}