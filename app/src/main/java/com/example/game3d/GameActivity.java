package com.example.game3d;
import static com.example.game3d.engine3d.Util.SCR_H;
import static com.example.game3d.engine3d.Util.SCR_W;
import static com.example.game3d.engine3d.Util.VX;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowInsets;
import android.view.WindowMetrics;

import com.example.game3d.engine3d.Object3D;
import com.example.game3d.engine3d.Util;

import java.io.IOException;

public class GameActivity extends Activity {
    public static GameView drawView;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            Util.SCR_W = windowMetrics.getBounds().width() - insets.left - insets.right;
            Util.SCR_H = windowMetrics.getBounds().height() - insets.top - insets.bottom;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            Util.SCR_W = displayMetrics.widthPixels;
            Util.SCR_H = displayMetrics.heightPixels;
        }
        Object3D.moveToScreen = VX(SCR_W/2,0,SCR_H/2);
        try {
            drawView = new GameView(this);
            drawView.setBackgroundColor(Color.BLACK);
            setContentView(drawView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}