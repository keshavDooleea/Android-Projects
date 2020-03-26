package kesh.yic_yac_yo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Random;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private Button btn3x3, btnRestart, btnMultiplayer;
    private TextView yourScore, AIScore, winnerTxt, turnTxt, yyyTxt, ttyTitle;
    private BottomSheetBehavior bsh;
    private GifImageView goldFish, bird, coin, smallBird, seaHorse, dolphin;
    private Handler handler;
    private GridLayout grid;
    private int you, ai, starter, height, width;
    private boolean isTaken[], hasWon, isY, isAI;
    private Window window;
    private ArrayList<Button> listBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // variables
        variables();
        playBkgMusic();
        moveCoin(false);

        // click listeners
        listeners();
    }

    private void variables() {
        handler = new Handler();
        btn3x3 = (Button) findViewById(R.id.three_btn);
        btnMultiplayer = (Button) findViewById(R.id.multiplayer_btn);
        btnRestart = (Button) findViewById(R.id.restart);
        yourScore = (TextView) findViewById(R.id.your_score);
        AIScore = (TextView) findViewById(R.id.ai_score);
        winnerTxt = (TextView) findViewById(R.id.winnerText);
        turnTxt = (TextView) findViewById(R.id.turn);
        btn3x3.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce));
        btnMultiplayer.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce));
        coin = (GifImageView) findViewById(R.id.coin);
        seaHorse = (GifImageView) findViewById(R.id.horse);
        dolphin = (GifImageView) findViewById(R.id.dolphin);
        goldFish = (GifImageView) findViewById(R.id.gold_fish);
        goldFish.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.righttoleft_gold_fish));

        ttyTitle = (TextView) findViewById(R.id.tty_title);
        String title = "<font color=#efdecd>YIC</font> <font color=#a3d4c4>YAC</font> <font color=#f498ad>YO</font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ttyTitle.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT));
        } else {
            ttyTitle.setText(Html.fromHtml(title));
        }

        bird = (GifImageView) findViewById(R.id.bird);
        bird.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.lefttoright_bird));

        smallBird = (GifImageView) findViewById(R.id.small_bird);
        smallBird.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.lefttoright_smallbird));
        grid = (GridLayout) findViewById(R.id.gridLayout);
        isY = false;
        isAI = false;


        // change status bar color
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        View bottomSheet = (View) findViewById(R.id.board);
        bsh = BottomSheetBehavior.from(bottomSheet);

        for (int i = 0; i < 10000; i++) {
            System.out.println("I LOVE YOU YOSH!");
        }

        // title
        yyyTxt = (TextView) findViewById(R.id.YoHouseText);
        yyyTxt.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein));

        // multiplayer button
        btnMultiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPlayBtn();
                isAI = false;
                handler.postDelayed(actionMultiplayer, 400);
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
    }

    private void initialisation() {
        listBtn = new ArrayList<>();
        isTaken = new boolean[9];
        hasWon = false;
        btnRestart.setEnabled(false);
        btnRestart.setAlpha(0.4f);
        Random random = new Random();
        starter = random.nextInt(2);
    }

    private void restartInitialisation() {
        you = 0;
        ai = 0;
        winnerTxt.setText("");
    }

    private void listeners() {
        btn3x3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPlayBtn();
                isAI = true;
                handler.postDelayed(action, 400);
            }
        });

        btnMultiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPlayBtn();
                isAI = false;
                handler.postDelayed(actionMultiplayer, 400);
            }
        });

        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);
                btnRestart.startAnimation(anim);
                winnerTxt.setText("");
                restart();
            }
        });

        // bottom sheet
        bsh.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        disableButton();
                        bsh.setHideable(false);
                        bsh.setPeekHeight(200);
                        placeDolphin(false);
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        placeDolphin(true);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        disableButton();
                        reSetup();
                        bsh.setHideable(false);
                        bsh.setPeekHeight(200);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        coin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playYBtn();
                startActivity(new Intent(MainActivity.this, Info.class));
            }
        });

        seaHorse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBubble();
                Toast.makeText(getApplicationContext(), "Je t'aime Yoshou!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableButton() {
        if (isAI) {
            btn3x3.setAlpha(0.2f);
            btn3x3.setEnabled(false);
            btnMultiplayer.setEnabled(true);
            btnMultiplayer.setAlpha(1f);
        } else {
            btn3x3.setAlpha(1f);
            btn3x3.setEnabled(true);
            btnMultiplayer.setEnabled(false);
            btnMultiplayer.setAlpha(0.2f);
        }
    }

    private void reSetup() {
        changeStatusColor();

        for (int i = 0 ; i < 9; i++) {
            if(listBtn.get(i).equals("Y")) {
                listBtn.get(i).setText("Y");
            }
        }
    }

    private void changeStatusColor() {
        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(this.getResources().getColor((R.color.fish)));
        }
    }

    private Runnable action = new Runnable() {
        @Override
        public void run() {
            bsh.setState(BottomSheetBehavior.STATE_EXPANDED);
            moveCoin(true);
            placeDolphin(true);
            grid.removeAllViews();
            initialisation();
            restartInitialisation();
            setCase();
            setGrid(true);
            btnListener();
        }
    };

    private Runnable actionMultiplayer = new Runnable() {
        @Override
        public void run() {
            bsh.setState(BottomSheetBehavior.STATE_EXPANDED);
            moveCoin(true);
            placeDolphin(true);
            grid.removeAllViews();
            initialisation();
            restartInitialisation();
            Random random = new Random();
            starter = random.nextInt(2);
            setCase();
            setGrid(false);
            btnListenerMultiplayer();
        }
    };

    private void moveCoin(boolean isPressed) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        if (isPressed) {
            params.bottomMargin = 180;
        }

        params.rightMargin = -100;

        params.width = 380;
        params.height = 180;

        coin.setLayoutParams(params);
    }

    private void placeDolphin(boolean intact) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        if (intact) {
            params.width = 400;
            params.height = 230;
        } else {
            params.width = 0;
            params.height = 0;
        }

        params.topMargin = 165;

        dolphin.setLayoutParams(params);
        dolphin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein));
    }

    private void restart() {
        initialisation();
        grid.removeAllViews();
        turnTxt.setText("");
        setCase();

        if (isAI) {
            setGrid(true);
            if (starter == 1) {
                AIsSmartTurn();
                starter = 0;
            }
            btnListener();
        }

        else {
            setGrid(false);
            btnListenerMultiplayer();
        }
    }

    private void setCase() {
        // boolean table setup
        for (int i = 0; i < 9; i++) {
            isTaken[i] = false;
        }
    }

    private void setGrid(boolean isAI) {
        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        // add a rule to align to the right
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_END);

        // add a rule to align to the left
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        // grid initialisation
        grid.setColumnCount(3);
        grid.setRowCount(3);

        for (int i = 0; i < 9; i++) {
            final Button btn = new Button(MainActivity.this);
            btn.setHeight(grid.getHeight() / 3);
            btn.setWidth(grid.getWidth() / 3);
            btn.setText("");
            btn.setTextSize(32);
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.buttons);
            grid.addView(btn, i);
            grid.setBackgroundResource(R.drawable.tty_panel);
            listBtn.add(btn);
        }

        if (isAI) {

            // score
            yourScore.setText(" You : " + you);
            AIScore.setText(ai + " : AI ");

            // who started
            if (starter == 0) {
                turnTxt.setText("You start");
                turnTxt.setTextColor(Color.parseColor("#b2dfdc"));
                turnTxt.setLayoutParams(leftParams);
                turnTxt.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.lefttoright));

            } else {
                turnTxt.setText("AI started");
                turnTxt.setTextColor(Color.parseColor("#d5b5de"));

                // make sure the rule was applied
                turnTxt.setLayoutParams(rightParams);
                turnTxt.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.righttoleft));
                AIsSmartTurn();
                starter = 0;
            }
        }

        else {

            // score
            yourScore.setText(" Y : " + you);
            AIScore.setText(ai + " : O ");

            // who started
            if (starter == 0) {
                turnTxt.setText("Y's turn");
                turnTxt.setTextColor(Color.parseColor("#b2dfdc"));
                turnTxt.setLayoutParams(leftParams);
                turnTxt.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.lefttoright));
            } else {
                turnTxt.setTextColor(Color.parseColor("#d5b5de"));
                turnTxt.setText("O's turn");
                turnTxt.setLayoutParams(rightParams);
                turnTxt.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.righttoleft));
            }
        }
    }

    private void btnListener() {
        for (int i = 0 ; i < 9; i++) {
            final int position = i;

            // click listener of each button
            listBtn.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Y
                    if(starter == 0) {
                        if(countUntakenCases() < 9 && !winnerFound()) {
                            playYBtn();
                            listBtn.get(position).setEnabled(false);
                            listBtn.get(position).setText("Y");
                            listBtn.get(position).setTextColor(Color.parseColor("#b2dfdc"));
                            listBtn.get(position).setTextSize(32);
                            turnTxt.setText("");
                            isTaken[position] = true;
                            starter = 1;
                        }
                    }

                    // O
                    if (starter == 1) {

                        // ensure no overflow
                        if(countUntakenCases() < 9 && !winnerFound()) {
                            AIsSmartTurn();
                        }
                        starter = 0;
                    }

                    applyWinnerEffects();
                    verifyDraw();
                }
            });
        }
    }

    private void btnListenerMultiplayer() {
        final RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        // add a rule to align to the right
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_END);


        for (int i = 0 ; i < 9; i++) {
            final int position = i;

            // click listener of each button
            listBtn.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playYBtn();
                    listBtn.get(position).setBackgroundResource(R.drawable.buttons);

                    // Y
                    if(starter == 0) {
                        if(countUntakenCases() < 9 && !winnerFound()) {
                            listBtn.get(position).setEnabled(false);
                            listBtn.get(position).setText("Y");
                            listBtn.get(position).setTextColor(Color.parseColor("#b2dfdc"));
                            listBtn.get(position).setTextSize(32);
                            isTaken[position] = true;

                            if (!isAI) {
                                turnTxt.setText("O's turn");
                                turnTxt.setTextColor(Color.parseColor("#d5b5de"));
                                turnTxt.setLayoutParams(rightParams);
                                turnTxt.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.righttoleft));
                            }

                            starter = 1;
                        }
                    }

                    // O
                    else {

                        // ensure no overflow
                        playO(position);
                        starter = 0;
                    }

                    applyWinnerEffects();
                    verifyDraw();
                }
            });
        }
    }

    private void playBkgMusic() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.yyy_music_bkg);
        mp.start();
        mp.setLooping(true);
    }

    private void playBubble() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.water_bubble);

        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null) {
                    mp.release();
                }
            }
        });
    }

    private void playPlayBtn() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.play_btn);

        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null) {
                    mp.release();
                }
            }
        });
    }

    private void playYBtn() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.y_button);

        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null) {
                    mp.release();
                }
            }
        });
    }

    private int countUntakenCases() {
        int empty = 0;
        for (int i = 0; i < 9; i++) {
            if(isTaken[i]) {
                empty++;
            }
        }
        return empty;
    }

    private void AIsSmartTurn() {
        Random random = new Random();
        int position;

        // o wins first
        position = findPosition("O");

        // no 2 o's detected -> block Y
        if (position == 99) {
            position = findPosition("Y");
        }

        // no neighbours found -> random position
        if(position == 99) {
            do {
                position = random.nextInt(9);
            } while (!listBtn.get(position).isEnabled());
        }

        // o's turn now
        playO(position);
    }

    private int findPosition(String letter) {
        // 0 1 2
        // 3 4 5
        // 6 7 8

        // x-1-2
        if ((listBtn.get(1).getText().equals(letter) && listBtn.get(2).getText().equals(letter))
                && !isTaken[0]) {
            return 0;
        }
        // 0-x-2
        else if ((listBtn.get(0).getText().equals(letter) && listBtn.get(2).getText().equals(letter))
                && !isTaken[1]) {
            return 1;
        }
        // 0-1-x
        else if ((listBtn.get(0).getText().equals(letter) && listBtn.get(1).getText().equals(letter))
                && !isTaken[2]) {
            return 2;
        }

        // ************************************************************************************ //

        // x
        // 3
        // 6
        else if ((listBtn.get(3).getText().equals(letter) && listBtn.get(6).getText().equals(letter))
                && !isTaken[0]) {
            return 0;
        }
        // 0
        // x
        // 6
        else if ((listBtn.get(0).getText().equals(letter) && listBtn.get(6).getText().equals(letter))
                && !isTaken[3]) {
            return 3;
        }
        // 0
        // 3
        // x
        else if ((listBtn.get(0).getText().equals(letter) && listBtn.get(3).getText().equals(letter))
                && !isTaken[6]) {
            return 6;
        }

        // ************************************************************************************ //

        // x
        //   4
        //     8
        else if ((listBtn.get(4).getText().equals(letter) && listBtn.get(8).getText().equals(letter))
                && !isTaken[0]) {
            return 0;
        }
        // 0
        //   x
        //     8
        else if ((listBtn.get(0).getText().equals(letter) && listBtn.get(8).getText().equals(letter))
                && !isTaken[4]) {
            return 4;
        }
        // 0
        //   4
        //     x
        else if ((listBtn.get(0).getText().equals(letter) && listBtn.get(4).getText().equals(letter))
                && !isTaken[8]) {
            return 8;
        }

        // ************************************************************************************ //
        // ************************************************************************************ //

        //     x
        //     5
        //     8
        else if ((listBtn.get(5).getText().equals(letter) && listBtn.get(8).getText().equals(letter))
                && !isTaken[2]) {
            return 2;
        }
        //     2
        //     x
        //     8
        else if ((listBtn.get(2).getText().equals(letter) && listBtn.get(8).getText().equals(letter))
                && !isTaken[5]) {
            return 5;
        }
        //     2
        //     5
        //     x
        else if ((listBtn.get(2).getText().equals(letter) && listBtn.get(5).getText().equals(letter))
                && !isTaken[8]) {
            return 8;
        }

        // ************************************************************************************ //

        //     x
        //   4
        // 6
        else if ((listBtn.get(4).getText().equals(letter) && listBtn.get(6).getText().equals(letter))
                && !isTaken[2]) {
            return 2;
        }
        //     2
        //   x
        // 6
        else if ((listBtn.get(2).getText().equals(letter) && listBtn.get(6).getText().equals(letter))
                && !isTaken[4]) {
            return 4;
        }
        //     2
        //   4
        // x
        else if ((listBtn.get(2).getText().equals(letter) && listBtn.get(4).getText().equals(letter))
                && !isTaken[6]) {
            return 6;
        }

        // ************************************************************************************ //
        // ************************************************************************************ //

        // x 7 8
        else if ((listBtn.get(7).getText().equals(letter) && listBtn.get(8).getText().equals(letter))
                && !isTaken[6]) {
            return 6;
        }
        // 6 x 8
        else if ((listBtn.get(6).getText().equals(letter) && listBtn.get(8).getText().equals(letter))
                && !isTaken[7]) {
            return 7;
        }
        // 6 7 x
        else if ((listBtn.get(6).getText().equals(letter) && listBtn.get(7).getText().equals(letter))
                && !isTaken[8]) {
            return 8;
        }

        // ************************************************************************************ //
        // ************************************************************************************ //

        // x 4 5
        else if ((listBtn.get(4).getText().equals(letter) && listBtn.get(5).getText().equals(letter))
                && !isTaken[3]) {
            return 3;
        }
        // 3 x 5
        else if ((listBtn.get(3).getText().equals(letter) && listBtn.get(5).getText().equals(letter))
                && !isTaken[4]) {
            return 4;
        }
        // 3 4 x
        else if ((listBtn.get(3).getText().equals(letter) && listBtn.get(4).getText().equals(letter))
                && !isTaken[5]) {
            return 5;
        }

        // ************************************************************************************ //

        // x
        // 4
        // 7
        else if ((listBtn.get(4).getText().equals(letter) && listBtn.get(7).getText().equals(letter))
                && !isTaken[1]) {
            return 1;
        }
        // 1
        // x
        // 7
        else if ((listBtn.get(1).getText().equals(letter) && listBtn.get(7).getText().equals(letter))
                && !isTaken[4]) {
            return 4;
        }
        // 1
        // 4
        // x
        else if ((listBtn.get(1).getText().equals(letter) && listBtn.get(4).getText().equals(letter))
                && !isTaken[7]) {
            return 7;
        }

        // ************************************************************************************ //
        // ************************************************************************************ //

        return 99;
    }

    private void playO(int position) {
        final RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        // add a rule to align to the left
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        if (!hasWon) {
            Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.zoomout);
            listBtn.get(position).startAnimation(anim);
            listBtn.get(position).setEnabled(false);
            listBtn.get(position).setText("O");
            listBtn.get(position).setTextColor(Color.parseColor("#d5b5de"));
            listBtn.get(position).setTextSize(32);
            isTaken[position] = true;

            if (!isAI) {
                turnTxt.setTextColor(Color.parseColor("#b2dfdc"));
                turnTxt.setText("Y's turn");
                turnTxt.setLayoutParams(leftParams);
                turnTxt.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.lefttoright));
            }
        }
    }

    private void applyWinnerEffects() {
        String winner = "";

        for (int i = 0; i < 9 && !hasWon; i++) {

            // squares color
            if(listBtn.get(i).getText().equals("Y")) {
                isY = true;
            } else if(listBtn.get(i).getText().equals("O")) {
                isY = false;
            }

            if(!listBtn.get(i).getText().equals("") && !hasWon) {

                // top left
                if(i == 0) {
                    if(listBtn.get(i).getText().equals(listBtn.get(1).getText()) && listBtn.get(i).getText().equals(listBtn.get(2).getText())) {
                        hasWon = true;
                        winner = listBtn.get(i).getText().toString();
                        buttons(i, 1, 2);

                    } else if(listBtn.get(i).getText().equals(listBtn.get(3).getText()) && listBtn.get(i).getText().equals(listBtn.get(6).getText())) {
                        hasWon = true;
                        winner = listBtn.get(i).getText().toString();
                        buttons(i, 3, 6);

                    } else if(listBtn.get(i).getText().equals(listBtn.get(4).getText()) && listBtn.get(i).getText().equals(listBtn.get(8).getText())) {
                        hasWon = true;
                        winner = listBtn.get(i).getText().toString();
                        buttons(i, 4, 8);
                    }
                }

                // top right
                if(i == 2) {
                    if(listBtn.get(i).getText().equals(listBtn.get(4).getText()) && listBtn.get(i).getText().equals(listBtn.get(6).getText())) {
                        hasWon = true;
                        winner = listBtn.get(i).getText().toString();
                        buttons(i, 4, 6);

                    } else if(listBtn.get(i).getText().equals(listBtn.get(5).getText()) && listBtn.get(i).getText().equals(listBtn.get(8).getText())) {
                        hasWon = true;
                        winner = listBtn.get(i).getText().toString();
                        buttons(i, 5,8);
                    }
                }

                // middle
                if(i == 4) {
                    if(listBtn.get(i).getText().equals(listBtn.get(1).getText()) && listBtn.get(i).getText().equals(listBtn.get(7).getText())) {
                        hasWon = true;
                        winner = listBtn.get(i).getText().toString();
                        buttons(i, 1, 7);

                    } else if(listBtn.get(i).getText().equals(listBtn.get(3).getText()) && listBtn.get(i).getText().equals(listBtn.get(5).getText())) {
                        hasWon = true;
                        winner = listBtn.get(i).getText().toString();
                        buttons(i, 3,5);
                    }
                }

                // bottom left
                if(i == 6) {
                    if(listBtn.get(i).getText().equals(listBtn.get(7).getText()) && listBtn.get(i).getText().equals(listBtn.get(8).getText())) {
                        hasWon = true;
                        winner = listBtn.get(i).getText().toString();
                        buttons(i, 7, 8);
                    }
                }
            }
        }

        // disable other buttons
        for (int i = 0; i < 9 && hasWon; i++) {
            if(listBtn.get(i).getText().equals("")) {
                listBtn.get(i).setEnabled(false);
            }
        }

        // winner found
        if(hasWon) {

            turnTxt.setText("");

            // score
            if(winner.equals("Y")) {
                you++;
                starter = 0;
                btnRestart.setEnabled(true);
                btnRestart.setAlpha(1f);

                if (isAI) {
                    AIScore.setText(ai + " : AI ");
                    winnerTxt.setText("You Win!");
                    yourScore.setText(" You : " + you);
                } else {
                    AIScore.setText(ai + " : O ");
                    winnerTxt.setText("Y Wins!");
                    yourScore.setText(" Y : " + you);
                }

                yourScore.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.blink_anim));
                winnerTxt.setTextColor(Color.parseColor("#b2dfdc"));

            } else if (winner.equals("O")) {
                ai++;
                starter = 1;
                btnRestart.setEnabled(true);
                btnRestart.setAlpha(1f);

                if (isAI) {
                    yourScore.setText(" You : " + you);
                    winnerTxt.setText("AI Wins!");
                    AIScore.setText(ai + " : AI ");
                } else {
                    yourScore.setText(" Y : " + you);
                    winnerTxt.setText("O Wins!");
                    AIScore.setText(ai + " : O ");
                }

                AIScore.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.blink_anim));
                winnerTxt.setTextColor(Color.parseColor("#d5b5de"));
            }
        }
    }

    private void buttons(int i, int a, int b) {
        if(isY) {
            listBtn.get(i).setBackgroundResource(R.drawable.buttons_y_won);
            listBtn.get(a).setBackgroundResource(R.drawable.buttons_y_won);
            listBtn.get(b).setBackgroundResource(R.drawable.buttons_y_won);
            listBtn.get(i).setTextColor(Color.parseColor("#09828d"));
            listBtn.get(a).setTextColor(Color.parseColor("#09828d"));
            listBtn.get(b).setTextColor(Color.parseColor("#09828d"));
        } else {
            listBtn.get(i).setBackgroundResource(R.drawable.buttons_o_won);
            listBtn.get(i).setTextColor(Color.parseColor("#c2c2c2"));
            listBtn.get(a).setTextColor(Color.parseColor("#c2c2c2"));
            listBtn.get(b).setTextColor(Color.parseColor("#c2c2c2"));
            listBtn.get(a).setBackgroundResource(R.drawable.buttons_o_won);
            listBtn.get(b).setBackgroundResource(R.drawable.buttons_o_won);
        }
    }

    private boolean winnerFound() {

        for (int i = 0; i < 9 && !hasWon; i++) {

            if(!listBtn.get(i).getText().equals("") && !hasWon) {

                // top left
                if(i == 0) {
                    if(listBtn.get(i).getText().equals(listBtn.get(1).getText()) && listBtn.get(i).getText().equals(listBtn.get(2).getText())) {
                        return true;

                    } else if(listBtn.get(i).getText().equals(listBtn.get(3).getText()) && listBtn.get(i).getText().equals(listBtn.get(6).getText())) {
                        return true;

                    } else if(listBtn.get(i).getText().equals(listBtn.get(4).getText()) && listBtn.get(i).getText().equals(listBtn.get(8).getText())) {
                        return true;
                    }
                }

                // top right
                if(i == 2) {
                    if(listBtn.get(i).getText().equals(listBtn.get(4).getText()) && listBtn.get(i).getText().equals(listBtn.get(6).getText())) {
                        return true;

                    } else if(listBtn.get(i).getText().equals(listBtn.get(5).getText()) && listBtn.get(i).getText().equals(listBtn.get(8).getText())) {
                        return true;
                    }
                }

                // middle
                if(i == 4) {
                    if(listBtn.get(i).getText().equals(listBtn.get(1).getText()) && listBtn.get(i).getText().equals(listBtn.get(7).getText())) {
                        return true;

                    } else if(listBtn.get(i).getText().equals(listBtn.get(3).getText()) && listBtn.get(i).getText().equals(listBtn.get(5).getText())) {
                        return true;
                    }
                }

                // bottom left
                if(i == 6) {
                    if(listBtn.get(i).getText().equals(listBtn.get(7).getText()) && listBtn.get(i).getText().equals(listBtn.get(8).getText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void verifyDraw() {
        boolean isEmpty = true;
        Random random = new Random();

        // verify if grid is full
        for (int i = 0; i < 9 && !hasWon; i++) {
            if (!isTaken[i]) {
                return;
            } else {
                isEmpty = false;
            }
        }

        if (!isEmpty) {
            starter = random.nextInt(2);
            btnRestart.setEnabled(true);
            btnRestart.setAlpha(1f);
            winnerTxt.setText("Issa draw!");
            winnerTxt.setTextColor(Color.parseColor("#c2c2c2"));
            turnTxt.setText("");
        }
    }
}