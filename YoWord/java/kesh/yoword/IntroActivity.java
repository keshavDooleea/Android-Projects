package kesh.yoword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private String username;
    private ViewPager screenPager;
    private IntroViewPagerAdapter introViewPagerAdapter;
    private TabLayout tabIndicator;
    private Button getStartedBtn;
    private int slidePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Intent intent = getIntent();
        username = intent.getStringExtra("name");

        // change status bar color
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor((R.color.white)));
        }

        tabIndicator = (TabLayout) findViewById(R.id.tab_indicator);
        getStartedBtn = (Button) findViewById(R.id.get_started_button);
        slidePosition = 0;

        // list containing slides
        final List<ScreenItem> list = new ArrayList<>();
        list.add(new ScreenItem("New", "Create a new fresh note", R.drawable.new_dim));
        list.add(new ScreenItem("User profile", "Modify your username", R.drawable.user_dim));
        list.add(new ScreenItem("View", "View your all time saved notes\n" +
                "Also saved as back up in Memory card\n" +
                "(Uninstalling the app erases the file in Memory card)", R.drawable.notes_dim));
        list.add(new ScreenItem("Quick Search", "Find your notes instantly", R.drawable.search_dim));
        list.add(new ScreenItem("Edit", "Click to edit your note", R.drawable.edit_dim));
        list.add(new ScreenItem("Swap", "Hold and drag to rearrange your notes in a different order", R.drawable.swap_dim));
        list.add(new ScreenItem("Delete", "Swipe to the right to discard your note", R.drawable.delete_dim));

        // viewpager
        screenPager = (ViewPager) findViewById(R.id.screem_pager);
        introViewPagerAdapter = new IntroViewPagerAdapter(IntroActivity.this, list);
        screenPager.setAdapter(introViewPagerAdapter);

        // tablayout
        tabIndicator.setupWithViewPager(screenPager);

        // tab layout change listener
        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == list.size() - 1) {

                    tabIndicator.setVisibility(View.GONE);
                    getStartedBtn.setVisibility(View.VISIBLE);
                    getStartedBtn.startAnimation(AnimationUtils.loadAnimation(IntroActivity.this, R.anim.down_to_up));
                } else {
                    tabIndicator.setVisibility(View.VISIBLE);
                    getStartedBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // listener
        getStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                intent.putExtra("name", username);
                startActivity(intent);
            }
        });

        // slide effect animation
        screenPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                final float normalizedPosition = Math.abs(Math.abs(position) - 1);
                page.setScaleX(normalizedPosition / 2 + 0.5f);
                page.setScaleY(normalizedPosition / 2 + 0.5f);
            }
        });

    }
}
