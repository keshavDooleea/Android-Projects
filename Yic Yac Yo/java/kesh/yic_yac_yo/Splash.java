package kesh.yic_yac_yo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class Splash extends AppCompatActivity {

    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // change status bar color
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(this.getResources().getColor((R.color.splash)));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // after 3 seconds
                startActivity(new Intent(Splash.this, MainActivity.class));

            }
        }, 3000);
    }
}
