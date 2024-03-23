package com.example.game3d.elements;

import static com.example.game3d.GameView.MAX_ADDONS;
import static com.example.game3d.GameView.getColorTheme;
import static com.example.game3d.elements.Player.PLR_SX;
import static com.example.game3d.elements.Player.PLR_SY;
import static com.example.game3d.elements.Player.PLR_SZ;
import static com.example.game3d.engine3d.Util.OBS;
import static com.example.game3d.engine3d.Util.PI;
import static com.example.game3d.engine3d.Util.VCPY;
import static com.example.game3d.engine3d.Util.VX;
import static com.example.game3d.engine3d.Util.VXS;
import static com.example.game3d.engine3d.Util.Vector;
import static com.example.game3d.engine3d.Util.add;
import static com.example.game3d.engine3d.Util.blue;
import static com.example.game3d.engine3d.Util.choice;
import static com.example.game3d.engine3d.Util.div;
import static com.example.game3d.engine3d.Util.getCentroid;
import static com.example.game3d.engine3d.Util.green;
import static com.example.game3d.engine3d.Util.isPointInTriangle;
import static com.example.game3d.engine3d.Util.mult;
import static com.example.game3d.engine3d.Util.pointAndPlanePosition;
import static com.example.game3d.engine3d.Util.randFloat;
import static com.example.game3d.engine3d.Util.randFloatRanges;
import static com.example.game3d.engine3d.Util.randInt;
import static com.example.game3d.engine3d.Util.randomPointInTriangle;
import static com.example.game3d.engine3d.Util.red;
import static com.example.game3d.engine3d.Util.sub;
import static com.example.game3d.engine3d.Util.yaw;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.tan;

import android.graphics.Color;

import com.example.game3d.engine3d.FixedMaxSizeDeque;
import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Util;

import java.io.IOException;

public class Generator {

    private final Vector startPos = VX(0, 0, 750);
    public float a = 1450, b = 500;
    //public FixedMaxSizeDeque<WorldElement> elements = new FixedMaxSizeDeque<>(MAX_TILES + MAX_ADDONS);
    private float lYaw = 0, lPitch = 0;
    private Tile lastTile = null;
    private int tilesSinceTurn = 0, currAddons=0;
    private int tilesSinceSpeedup = 0;
    private int maxAddons, maxTiles;

    public Generator(int maxTiles, int maxAddons){
        this.maxTiles = maxTiles;
        this.maxAddons = maxAddons;
    }


    private Tile newTile() {
        Vector closeLeft = add(startPos, VX(-a / 2, -b / 2, 0));//lastTile.vert(0);
        Vector closeRight = add(startPos, VX(a / 2, -b / 2, 0));//lastTile.vert(1);
        Vector farRight = add(startPos, VX(a / 2, b / 2, 0));//lastTile.vert(2);
        Vector farLeft = add(startPos, VX(-a / 2, b / 2, 0));//lastTile.vert(3);

        closeLeft = yaw(closeLeft, OBS, lYaw);
        closeRight = yaw(closeRight, OBS, lYaw);
        farRight = yaw(farRight, OBS, lYaw);
        farLeft = yaw(farLeft, OBS, lYaw);

        farRight.z += b * tan(lPitch);
        farLeft.z += b * tan(lPitch);

        Vector diff = sub(lastTile.vert(3), closeLeft);

        closeLeft = add(closeLeft, diff);
        closeRight = lastTile.vert(2);
        farRight = add(farRight, diff);
        farLeft = add(farLeft, diff);

        return new Tile(closeLeft, closeRight, farRight, farLeft);
        /*elements.pushBack(lastTile);
        ++tilesSinceTurn;
        ++tilesSinceSpeedup;*/
    }

    private void addTile(FixedMaxSizeDeque<WorldElement> elements){
        lastTile = newTile();
        elements.pushBack(lastTile);
        ++tilesSinceTurn;
        ++tilesSinceSpeedup;
        --tilesLeft;
        ++step;
    }

    float f(float x) {
        return 0.7f * (0.1f * x * x - 1.5f * x + 1);
    }

    float g(float x) {
        /*double v = 1 - sin(x * 0.5f) * sin(x * 0.5f) * sin(x * 0.5f) * sin(x * 0.5f);
        return 0.5f * (float) (1 - v * v);*/
        double u = x - (x * x * x / 3.0f) + (x * x * x * x * x / 2.0f);
        double v = 1.0f - u * u;
        return (float) (0.3f * (1.0f - v * v));
    }

    void addAddons(FixedMaxSizeDeque<WorldElement> elements) {
        int type = randInt(0, 10);
        Vector[] pos = new Vector[]{randomPointInTriangle(lastTile.vert(0), lastTile.vert(1), lastTile.vert(2)), randomPointInTriangle(lastTile.vert(0), lastTile.vert(3), lastTile.vert(2))};
        Vector where = pos[randInt(0, 1)]; //getCentroid(lastTile.vert(0),lastTile.vert(1),lastTile.vert(2),lastTile.vert(3));
        if (type <= 1) {
            elements.pushBack(new Feather(where.x, where.y, where.z - Feather.FEATHER_SZ));
            ++currAddons;
        } else if (type == 2) {
            elements.pushBack(new Potion(where.x, where.y, where.z - Potion.POTION_SZ - 40));
            ++currAddons;
        } else {
            int howMany = randInt(1, min(2,maxAddons-currAddons));
            Vector mv = yaw(div(sub(lastTile.vert(0), lastTile.vert(3)), b), OBS, PI / 2);
            for (int i = 0; i < howMany; ++i) {
                int height = randInt(150, 450);
                int width = randInt(200, 400);
                if (isPointInTriangle(lastTile.vert(0), lastTile.vert(1), lastTile.vert(2), where) || isPointInTriangle(lastTile.vert(0), lastTile.vert(3), lastTile.vert(2), where)) {
                    elements.pushBack(new DeathSpike(sub(where, VX(0, 0, 10)), width, height));
                ++currAddons;
                } else {
                    break;
                }
                where = add(where, mult(mv, -width * 1.33f));
            }
        }
    }

    private int step = 0, tilesLeft=0;


    public void generate(int n, int difficulty, FixedMaxSizeDeque<WorldElement> elements) {
        //  b *= max(1,difficulty*0.75);
        tilesLeft = n;
        if (lastTile == null) {
            lastTile = new Tile(add(startPos, VX(-a / 2, -b / 2, 0)), // close left
                    add(startPos, VX(a / 2, -b / 2, 0)), // close right
                    add(startPos, VX(a / 2, b / 2, 0)), // far right
                    add(startPos, VX(-a / 2, b / 2, 0))); // far left
            elements.pushBack(lastTile);
            --tilesLeft;
            ++step;
        }
        while (tilesLeft > 0) {
            int diceRoll = randInt(0, 11);
            if (diceRoll > 9 && tilesLeft >= 16 && step > 1 && tilesSinceSpeedup > 12) {
                int nSteps = randInt(16, min(tilesLeft, 37));
                float dYaw = choice(0, randFloatRanges(3, -PI / (2.5f * nSteps), -PI / (5 * nSteps), PI / (5 * nSteps), PI / (2.5f * nSteps)));
                float descent = choice(-500, -300, 400, 700);
                float r = 1.425f;//choice(1.35f, 1.5f);
                if (difficulty == 2) {
                    r = 1.55f;//choice(1.45f,1.6f);
                } else if (difficulty == 3) {
                    r = 1.675f;//choice(1.55f,1.8f);
                } else if (difficulty > 3) {
                    r = 1.8f;//choice(1.7f,2.0f);
                }
                b *= r;
                int tilesPerPlatform = (int) choice(3, 4), tilesToDelete = randInt(0, tilesPerPlatform), total = tilesPerPlatform + tilesToDelete;
                for (int i = 0; i < nSteps; ++i) {
                    lYaw += dYaw;
                    if (i % total < tilesPerPlatform) {
                        addTile(elements);
                        if (randInt(1, 17) == 2 && currAddons<MAX_ADDONS) {
                            addAddons(elements);
                        }
                    } else {
                        lastTile = newTile();
                        if (i % total == tilesPerPlatform && i != nSteps - 1) {
                            lastTile.move(VX(0, 0, descent));
                        }
                    }
                }
                b /= r;
            } else if (diceRoll > 5 && tilesLeft >= 20 && step > 2 && tilesSinceSpeedup > 4) {
                int nSteps = randInt(20, min(tilesLeft, 25));
                float minr = 0.2f, maxr = 0.6f;

                int which = randInt(0, 1);
                if (tilesSinceSpeedup < 12) {
                    which = 0;
                }
                float len;
                if (which == 0) {
                    if (difficulty == 1) {
                        minr = 0.4f;
                        maxr = 0.6f;
                    } else if (difficulty == 2) {
                        minr = 0.6f;
                        maxr = 0.75f;
                    }
                    len = randFloat(16, 22, 2) / nSteps;
                } else {
                    if (difficulty == 1) {
                        minr = 0.175f;
                        maxr = 0.3f;
                    } else if (difficulty == 2) {
                        minr = 0.3f;
                        maxr = 0.5f;
                    }
                    nSteps = randInt(10, 16);
                    len = (0.8f / nSteps);
                }

                float r = randFloat(minr, maxr, 2);
                b *= r;
                for (float i = 0; i < nSteps; ++i) {
                    if (which == 0) {
                        lPitch = (float) -Math.atan((f((i + 1) * len) - f(i * len)) / len);
                    } else {
                        lPitch = (float) -Math.atan((g((i + 1) * len) - g(i * len)) / len);
                    }
                    addTile(elements);
                    if (which != 0) {
                        lastTile.retarded = true;
                        lastTile.speedup = 1.125f + 1.5f * abs(lPitch / (0.5f * PI));
                    } else {
                        if (lastTile.isFrontHill()) {
                            lastTile.speedup = 1.05f + 1.25f * abs(lPitch / (0.5f * PI));
                        }
                    }
                }
                tilesSinceSpeedup = 0;
                lPitch = 0;
                b /= r;
            } else if (diceRoll > 2 && tilesLeft >= 6 && step > 2 && tilesSinceTurn > 8 && tilesSinceSpeedup > 12) {
                int nSteps = randInt(6, min(tilesLeft, 14));
                float dYaw = randFloatRanges(3, -PI / (2.5f * nSteps), -PI / (5.5f * nSteps), PI / (5.5f * nSteps), PI / (2.5f * nSteps));
                for (int i = 0; i < nSteps; ++i) {
                    lYaw += dYaw;
                    addTile(elements);
                    if (randInt(1, 8) == 2 && step > 10 && currAddons<MAX_ADDONS) {
                        addAddons(elements);
                    }
                }
                /*if(randInt(1,10)==1 && step>5){
                        addPortal();
                    }*/
                tilesSinceTurn = 0;
            } else {
                int nSteps = min(tilesLeft, randInt(4, 8));
                for (int i = 0; i < nSteps; ++i) {
                    addTile(elements);
                    if (randInt(1, 8) == 2 && step > 10 && currAddons<MAX_ADDONS) {
                        addAddons(elements);
                    }
                }
            }
        }
        // b/=max(1,difficulty*0.4);
    }

     /*void addPortal(FixedMaxSizeDeque<WorldElement> elements) {
        int type = randInt(0, 10);
        Vector where = getCentroid(lastTile.vert(0), lastTile.vert(1), lastTile.vert(2), lastTile.vert(3));
        Vector dir = yaw(VX(0, a * 1.8f, 0), OBS, 0.5f * PI + lYaw);
        where = add(where, dir);
        Portal portal = new Portal(sub(where, VX(0, 0, -200)));
        elements.pushBack(portal);
    }*/


}