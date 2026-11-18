package com.example.firestickmessage;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

/** A deliberately minimal Fire TV screen with one inert, button-shaped control. */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        window.setStatusBarColor(Color.rgb(17, 18, 21));
        window.setNavigationBarColor(Color.rgb(17, 18, 21));

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(17, 18, 21));

        Button messageButton = new Button(this);
        messageButton.setText("This app runs on firestick");
        // Keep the requested lower-case wording on all Android themes.
        messageButton.setTransformationMethod(null);
        messageButton.setTextColor(Color.WHITE);
        messageButton.setTextSize(28);
        messageButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        messageButton.setGravity(Gravity.CENTER);
        messageButton.setPadding(dp(36), dp(22), dp(36), dp(22));
        messageButton.setMinWidth(0);
        messageButton.setMinimumWidth(0);
        messageButton.setMinHeight(0);
        messageButton.setMinimumHeight(0);

        GradientDrawable buttonBackground = new GradientDrawable();
        buttonBackground.setColor(Color.rgb(43, 48, 57));
        buttonBackground.setCornerRadius(dp(12));
        buttonBackground.setStroke(dp(1), Color.rgb(76, 83, 95));
        messageButton.setBackground(buttonBackground);
        messageButton.setStateListAnimator(null);

        // The control looks like a button but cannot be focused or activated.
        messageButton.setClickable(false);
        messageButton.setFocusable(false);
        messageButton.setLongClickable(false);
        messageButton.setSoundEffectsEnabled(false);

        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        root.addView(messageButton, buttonParams);
        setContentView(root);

        root.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
