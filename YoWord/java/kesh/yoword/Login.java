package kesh.yoword;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Login extends AppCompatActivity {

    private Window window;
    private boolean firstTime;
    private RelativeLayout relativeLayout, ykdRelative;
    private EditText name;
    private String username;
    private Button confirmBtn;
    private long delay;

    // broadcoast
    private static ActivityLogin broadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // layout covers status bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        broadcast();
        loadData();

        ykdRelative = (RelativeLayout) findViewById(R.id.ykd_relative);

        if (!firstTime) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ykdRelative.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            ykdRelative.setLayoutParams(layoutParams);
            ykdRelative.setVisibility(View.VISIBLE);
            ykdRelative.startAnimation(AnimationUtils.loadAnimation(Login.this, R.anim.splash_anim));
            delay = 2600;
        } else {
            delay = 600;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // after 2 seconds
                if (firstTime) {
                    init();
                } else {
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    intent.putExtra("name", username);
                    startActivity(intent);
                }
            }
        }, delay);
    }

    private void broadcast() {
        broadcast = new ActivityLogin();
        registerReceiver(broadcast, new IntentFilter("Name Filter"));
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        firstTime = sharedPreferences.getBoolean("bool", true);
        username = sharedPreferences.getString("name", null);
    }

    private void init() {
        relativeLayout = (RelativeLayout) findViewById(R.id.username);
        confirmBtn = (Button) findViewById(R.id.confirm);
        name = (EditText) findViewById(R.id.usernameEditText);

        ykdRelative.setVisibility(View.VISIBLE);
        relativeLayout.setVisibility(View.VISIBLE);
        confirmBtn.setVisibility(View.VISIBLE);

        ykdRelative.startAnimation(AnimationUtils.loadAnimation(Login.this, R.anim.splash_anim));
        relativeLayout.startAnimation(AnimationUtils.loadAnimation(Login.this, R.anim.splash_anim));
        confirmBtn.startAnimation(AnimationUtils.loadAnimation(Login.this, R.anim.splash_anim));

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = name.getText().toString();
                confirmBtn.startAnimation(AnimationUtils.loadAnimation(Login.this, R.anim.rotate));

                if (username != null) {
                    firstTime = false;

                    saveInfo();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Login.this, IntroActivity.class);
                            intent.putExtra("name", username);
                            startActivity(intent);
                        }
                    }, 900);
                }
            }
        });
    }

    private void saveInfo() {
        SharedPreferences sharedPreferences2 = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor2.putBoolean("bool", false);
        editor2.putString("name", username);
        editor2.apply();
    }

    class ActivityLogin extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            username = intent.getStringExtra("name");
            saveInfo();
        }

    }
}
