package kesh.yoword;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spark.submitbutton.SubmitButton;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements BottomSheet.BottomSheetListener {

    // layout variables
    private CoordinatorLayout coordinatorLayout;
    private Button newButton, viewNoteButton, userBtn, backBtn;
    private int bottomSheetPosition, arrayListPosition, textListPosition, textPosition, titleHeight;
    private BottomSheet sheet;
    private BottomSheetBehavior bsh;
    private boolean firstTime, editClicked;
    private TextView usernameTxt;
    private String username;
    private EditText searchText;

    // recycle variables
    private RecyclerView recyclerView;
    private ItemsAdapter recycleAdapter;
    private ArrayList<Items> arrayList, textList;
    private boolean filterSwipped;
    private Button searchCloseBtn;
    private int filterListPosition;

    // broadcoast
    private static ActivityMain broadcast;

    // dialog
    private Dialog dialog;
    private Button confirmBtn;
    private ImageView close;
    private EditText dialogText;

    // load file
    private File file;
    private static final String FILE_NAME = "YoWord.txt";
    private TextView loadFileTxt;

    // prevent from going back to previous activity
    @Override
    public void onBackPressed() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // layout covers status bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        broadcast();
        titleHeight();
        loadPermanentData();
        recycleView();
        user();
        newButton();
        viewNotes();
        verifyFirstTime();
        writeToFilePermission();
    }

    private void verifyFirstTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("first time", MODE_PRIVATE);
        firstTime = sharedPreferences.getBoolean("boolean", true);

        if (firstTime) {
            welcomingNote();
        }
        saveFirstTime();

        Intent intent = getIntent();
        username = intent.getStringExtra("name");
        usernameTxt = (TextView) findViewById(R.id.usernameMain);
        usernameTxt.setText(username);
    }

    private void broadcast() {
        broadcast = new ActivityMain();
        registerReceiver(broadcast, new IntentFilter("Item Filter"));
    }

    private void titleHeight() {
        final TextView title = (TextView) findViewById(R.id.title);
        title.post(new Runnable() {
            @Override
            public void run() {
                titleHeight = title.getHeight(); //height is ready
            }
        });
    }

    private void saveFirstTime() {
        // first time
        SharedPreferences sharedPreferences = getSharedPreferences("first time", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("boolean", false);
        editor.apply();
    }

    private void newButton() {
        // init
        newButton = (Button) findViewById((R.id.new_button));
        newButton.setBackgroundResource(R.drawable.new_button);

        // add/position in layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.width = 135;
        params.height = 135;
        params.bottomMargin = 22;

        newButton.setLayoutParams(params);

        // listener
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newButton.setEnabled(false);
                userBtn.setEnabled(false);
                viewNoteButton.setEnabled(false);
                editClicked = false;

                // modify bool statement used in Note class
                SharedPreferences sharedPreferences = getSharedPreferences("edit", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("bool", false);
                editor.putInt("height", titleHeight);
                editor.apply();

                // save position and add new space
                Items newItem = new Items(0,"", "", "", "", username, "", false);
                arrayListPosition = arrayList.size();
                textListPosition = textList.size();
                arrayList.add(newItem);
                textList.add(newItem);
                recycleAdapter.notifyItemInserted(arrayList.size());

                searchText.setText("");

                startActivity(new Intent(MainActivity.this, Note.class));
            }
        });
    }

    private void user() {
        // init
        userBtn = (Button) findViewById(R.id.user_button);
        dialog = new Dialog(this);

        // position in layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.width = 115;
        params.height = 115;
        params.leftMargin = 30;
        params.bottomMargin = 25;

        userBtn.setLayoutParams(params);
        userBtn.setBackgroundResource(R.drawable.user);

        // listener
        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newButton.setEnabled(false);
                userBtn.setEnabled(false);
                viewNoteButton.setEnabled(false);
                dialog();
            }
        });
    }

    private void dialog() {
        dialog.setContentView(R.layout.user_dialog);
        close = (ImageView) dialog.findViewById(R.id.close_btn);
        confirmBtn = (Button) dialog.findViewById(R.id.dialog_confirm_btn);
        dialogText = (EditText) dialog.findViewById(R.id.newName);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                newButton.setEnabled(true);
                userBtn.setEnabled(true);
                viewNoteButton.setEnabled(true);
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogText.getText().toString().equals("")) {

                    newButton.setEnabled(true);
                    userBtn.setEnabled(true);
                    viewNoteButton.setEnabled(true);

                    final Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce);
                    BouncyInterpolator bouncyInterpolator = new BouncyInterpolator(0.8, 13);
                    animation.setInterpolator(bouncyInterpolator);
                    confirmBtn.startAnimation(animation);

                    username = dialogText.getText().toString();
                    usernameTxt.setText(username);

                    // save username
                    final Intent intent = new Intent("Name Filter");
                    intent.putExtra("name", username);
                    sendBroadcast(intent);
                }
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.85f);
        dialog.show();
    }

    private void viewNotes() {
        // init
        viewNoteButton = (Button) findViewById(R.id.view_notes_button);
        loadFileTxt = (TextView) findViewById(R.id.loadFileTxt);
        backBtn = (Button) findViewById(R.id.back_btn);
        View bottomSheet = findViewById(R.id.bottom_sheet_text);
        bsh = BottomSheetBehavior.from(bottomSheet);
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), FILE_NAME);

        // position in layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.width = 120;
        params.height = 120;
        params.rightMargin = 30;
        params.bottomMargin = 35;

        viewNoteButton.setLayoutParams(params);
        viewNoteButton.setBackgroundResource(R.drawable.view_notes);

        // listener
        viewNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsh.setState(BottomSheetBehavior.STATE_EXPANDED);
                backBtn.setVisibility(View.VISIBLE);
                newButton.setEnabled(false);
                userBtn.setEnabled(false);
                viewNoteButton.setEnabled(false);

                Toast toasty = Toast.makeText(MainActivity.this, "Scroll up to go back", Toast.LENGTH_SHORT);

                View toast = toasty.getView();
                toast.setBackgroundResource(R.drawable.scroll_up_toast);

                TextView toastTxt = (TextView) toasty.getView().findViewById(android.R.id.message);
                toastTxt.setTextColor(Color.parseColor("#defcfe"));

                toasty.show();

                loadFile();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsh.setState(BottomSheetBehavior.STATE_HIDDEN);
                backBtn.setVisibility(View.GONE);
                newButton.setEnabled(true);
                userBtn.setEnabled(true);
                viewNoteButton.setEnabled(true);
            }
        });

        // set visibility of back button
        bsh.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        backBtn.setVisibility(View.VISIBLE);
                        newButton.setEnabled(true);
                        userBtn.setEnabled(true);
                        viewNoteButton.setEnabled(true);
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_DRAGGING:
                        backBtn.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

    }

    private void loadFile() {
        String data = "";

        try {
            FileInputStream fis = new FileInputStream(file);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            data = new String(buffer);
            loadFileTxt.setText(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recycleView() {
        // variables initialisation
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recycleAdapter = new ItemsAdapter(arrayList, textList);

        // define list: vertical or horizontal
        recyclerView.setAdapter(recycleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // search
        searchText = (EditText) findViewById(R.id.search_edittext);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSwipped = true;

                // verify if search box is empty
                if (TextUtils.isEmpty(searchText.getText().toString())) {
                    filterSwipped = false;
                }

                recycleAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // search close button
        searchCloseBtn = (Button) findViewById(R.id.searh_close_btn);
        searchCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(searchText.getText().toString())) {
                    searchText.setText("");
                }
            }
        });

        // add Divider between items
        /*DividerItemDecoration dec = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dec);*/

        // on drag
        ItemTouchHelper.Callback dragCallback = new ItemTouchHelper.Callback() {

            int dragFrom = -1;
            int dragTo = -1;
            int fromPosition = -1;
            int toPosition = -1;
            int counter = 0;
            int firstPosition = 0;
            ArrayList<Items> filterList;

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                fromPosition = viewHolder.getAdapterPosition();
                toPosition = target.getAdapterPosition();

                if(dragFrom == -1) {
                    dragFrom =  fromPosition;
                }
                dragTo = toPosition;

                if (counter == 0) {
                    firstPosition = fromPosition;
                }
                counter++;

                if (!filterSwipped) {
                    Collections.swap(arrayList, fromPosition, toPosition);
                    recycleAdapter.notifyItemMoved(fromPosition, toPosition);
                } else {
                    filterList = recycleAdapter.getFilterList();
                    Collections.swap(filterList, fromPosition, toPosition);
                    recycleAdapter.notifyItemMoved(fromPosition, toPosition);
                }

                saveDataPermanently();

                return true;
            }

            private void reallyMoved() {
                counter = 0;

                if (filterSwipped) {
                    // retrieve item from filter list
                    Items fromItems = filterList.get(toPosition);

                    // insert item in arraylist
                    if (arrayList.contains(fromItems)) {
                        int temp = arrayList.indexOf(fromItems);
                        arrayList.remove(temp);
                        arrayList.add(toPosition, fromItems);
                        //recycleAdapter.notifyDataSetChanged();
                        recycleAdapter.notifyItemChanged(toPosition);
                        saveDataPermanently();
                    }
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                    reallyMoved();
                }
                dragFrom = dragTo = -1;
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(dragCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        // delete items
        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                if (filterSwipped) {
                    recycleAdapter.removeItem(viewHolder.getAdapterPosition());
                } else {
                    arrayList.remove(viewHolder.getAdapterPosition());
                    recycleAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                }

                // save updated data permanently
                saveDataPermanently();

                if (bottomSheetPosition == 0) {
                    saveFirstTime();
                }
            }
        });
        itemTouchHelper1.attachToRecyclerView(recyclerView);

        // click to edit
        recycleAdapter.setOnItemClickListener(new ItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // save data to send to read activity
                editClicked = true;

                if (filterSwipped) {
                    bottomSheetPosition = recycleAdapter.getListPosition(position);
                    filterListPosition = position;
                    ArrayList<Items> filterList;
                    filterList = recycleAdapter.getFilterList();

                    String title = filterList.get(filterListPosition).getTitlee();
                    String note = filterList.get(filterListPosition).getNote();
                    String date = filterList.get(filterListPosition).getDate();

                    // save text list position
                    for (int i = 0; i < textList.size(); i++) {
                        if (textList.get(i).getTitlee().equals(title) &&
                                textList.get(i).getNote().equals(note) &&
                                textList.get(i).getDate().equals(date) ) {

                            textPosition = i;
                        }
                    }

                } else {
                    bottomSheetPosition = position;

                    String title = arrayList.get(bottomSheetPosition).getTitlee();
                    String note = arrayList.get(bottomSheetPosition).getNote();
                    String date = arrayList.get(bottomSheetPosition).getDate();

                    // save text list position
                    for (int i = 0; i < textList.size(); i++) {
                        if (textList.get(i).getTitlee().equals(title) &&
                                textList.get(i).getNote().equals(note) &&
                                textList.get(i).getDate().equals(date)) {

                            textPosition = i;
                        }
                    }
                }

                // send data before starting activity
                SharedPreferences sharedPreferences = getSharedPreferences("edit", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("title", arrayList.get(bottomSheetPosition).getTitlee());
                editor.putString("note", arrayList.get(bottomSheetPosition).getNote());
                editor.putString("image", arrayList.get(bottomSheetPosition).getImageName());
                editor.putString("date", arrayList.get(bottomSheetPosition).getDate());
                editor.putString("photo", arrayList.get(bottomSheetPosition).getPhoto());
                editor.putBoolean("type", arrayList.get(bottomSheetPosition).getPhotoType());
                editor.putBoolean("bool", true);
                editor.putInt("height", titleHeight);
                editor.apply();
                startActivityForResult(new Intent(MainActivity.this, Note.class), 2);
            }
        });

        // long click listener
        recycleAdapter.setOnItemLongClickListener(new ItemsAdapter.OnItemLongClickListner() {
            @Override
            public void onItemLongClick(int position) {
                // save index
                /*bottomSheetPosition = position;

                // open bottom sheet
                sheet = new BottomSheet();
                sheet.show(getSupportFragmentManager(), "BottomSheet");*/
            }
        });
    }

    // when close button is pressed, we retrieve the data from Note class and set to Recycle view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // edit note
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

                newButton.setEnabled(true);
                userBtn.setEnabled(true);
                viewNoteButton.setEnabled(true);

                // get data
                String title = data.getStringExtra("title");
                String note = data.getStringExtra("note");
                String imageName = data.getStringExtra("image");
                String date = data.getStringExtra("date");
                String photo = data.getStringExtra("photo");
                boolean type = data.getBooleanExtra("type", false);
                int image = 0;

                // modify image
                if (imageName.equals("heart")) {
                    image = R.drawable.heart;
                } else if (imageName.equals("music")) {
                    image = R.drawable.music;
                } else if (imageName.equals("soccer")) {
                    image = R.drawable.soccer;
                } else if (imageName.equals("veg")) {
                    image = R.drawable.veg;
                } else if (imageName.equals("shop")) {
                    image = R.drawable.shopping;
                } else if (imageName.equals("phone")) {
                    image = R.drawable.phone;
                } else if (imageName.equals("birthday")) {
                    image = R.drawable.birthday;
                }

                if (filterSwipped) {
                    recycleAdapter.updateEditFilter(filterListPosition, title, note, date, photo, type);
                }

                // replace data to arraylist
                Items item = new Items(image, imageName, title, note, date, username, photo, type);
                arrayList.add(bottomSheetPosition, item);
                arrayList.remove(bottomSheetPosition + 1);
                recycleAdapter.notifyDataSetChanged();

               /* textList.remove(textPosition);
                textList.add(textPosition, new Items(image, imageName, title, note, date, username)); */
                textList.get(textPosition).setTitlee(title);
                textList.get(textPosition).setNote(note);
                textList.get(textPosition).setDate(date);
                textList.get(textPosition).setName(username);
                recycleAdapter.setTextList(textList);

                saveDataPermanently();

                // refresh recycle view
                recycleAdapter.notifyItemChanged(bottomSheetPosition);
            }
        }

        // save data permanently
        saveDataPermanently();
    }

    private void saveDataPermanently() {
        // array list
        SharedPreferences sharedPreferences = getSharedPreferences("save data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        String textJson = gson.toJson(textList);
        editor.putString("array liste", json);
        editor.putString("text list", textJson);
        editor.apply();
    }

    private String getDate() {
        Date today = Calendar.getInstance().getTime();//getting date
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        final String sdate = df.format(today);
        return sdate;
    }

    // load onCreate
    private void loadPermanentData() {
        SharedPreferences sharedPreferences = getSharedPreferences("save data", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("array liste", null);
        String textJson = sharedPreferences.getString("text list", null);
        Type type = new TypeToken<ArrayList<Items>>() {}.getType();
        arrayList = gson.fromJson(json, type);
        textList = gson.fromJson(textJson, type);

        // check if arraylist exists
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }

        // check if arraylist exists
        if (textList == null) {
            textList = new ArrayList<>();
        }
    }

    private void welcomingNote() {
        // add welcoming note once
        String note =
                "This is your dashboard\n\n" +
                "Click to edit this note\n\n" +
                "Long hold and drag to swap this note with another one\n\n" +
                        "Swipe to the right to delete the note\n\n" +
                        "Search bar allows to find notes quickly\n\n" +
                "The bottom right icon opens all your saved notes\n\n" +
                "Additionally, your notes are saved permanently in the root of your Memory card\n" +
                        "However, deleting the app erases them\n\n" +
                "Keshav Dooleea\n@YKD";

        //arrayList.add(new Items(R.drawable.heart, "heart", "Welcome", note,  getDate(), username, null, false));

        // refresh recycle view
        //recycleAdapter.notifyItemInserted(arrayList.size());

        // save updated data permanently
        //saveDataPermanently();
    }

    // interface method from Bottom sheet
    @Override
    public void onButtonClicked(String text) {

        // edit row
        if (text.equals("edit button")) {
            // send data before starting activity
            SharedPreferences sharedPreferences = getSharedPreferences("edit", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("title", arrayList.get(bottomSheetPosition).getTitlee());
            editor.putString("note", arrayList.get(bottomSheetPosition).getNote());
            editor.putString("image", arrayList.get(bottomSheetPosition).getImageName());
            editor.putString("date", arrayList.get(bottomSheetPosition).getDate());
            editor.putBoolean("bool", true);
            editor.apply();
            startActivityForResult(new Intent(MainActivity.this, Note.class), 2);
        }

        // delete row
        if (text.equals("delete button")) {
            // remove from arrayList
            arrayList.remove(bottomSheetPosition);

            // remove row from recycle view
            recycleAdapter.notifyItemRemoved(bottomSheetPosition);

            // save updated data permanently
            saveDataPermanently();

            if (bottomSheetPosition == 0) {
                saveFirstTime();
            }
        }

        // close bottom sheet
        sheet.dismiss();
    }

    private void writeToFilePermission() {
        // external permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)  {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 1000 :
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                    System.exit(0);
                }
        }
    }

    // broadcast
    class ActivityMain extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int position = -1;
            int positionText = -1;
            newButton.setEnabled(true);
            userBtn.setEnabled(true);
            viewNoteButton.setEnabled(true);

            // get data from saved button in note
            String title = intent.getStringExtra("title");
            String note = intent.getStringExtra("note");
            String imageName = intent.getStringExtra("image");
            String date = intent.getStringExtra("date");
            boolean isBlank = intent.getBooleanExtra("saved", false);
            String photo = intent.getStringExtra("photo");
            boolean type = intent.getBooleanExtra("type", false);

            if (filterSwipped) {
                recycleAdapter.updateEditFilter(filterListPosition, title, note, date, photo, type);
            }

            if (editClicked) {
                position = bottomSheetPosition;
                positionText = textPosition;
            } else {
                // new
                position = arrayListPosition;
                positionText = textListPosition;
            }

            // save contents
            arrayList.get(position).setDate(date);
            arrayList.get(position).setTitlee(title);
            arrayList.get(position).setNote(note);
            arrayList.get(position).setPhoto(photo);
            arrayList.get(position).setPhotoType(type);

            // save data to arraylist
            if (imageName.equals("heart")) {
                arrayList.get(position).setImageName("heart");
                arrayList.get(position).setImage(R.drawable.heart);
            } else if (imageName.equals("music")) {
                arrayList.get(position).setImageName("music");
                arrayList.get(position).setImage(R.drawable.music);
            } else if (imageName.equals("soccer")) {
                arrayList.get(position).setImageName("soccer");
                arrayList.get(position).setImage(R.drawable.soccer);
            } else if (imageName.equals("veg")) {
                arrayList.get(position).setImageName("veg");
                arrayList.get(position).setImage(R.drawable.veg);
            } else if (imageName.equals("shop")) {
                arrayList.get(position).setImageName("shop");
                arrayList.get(position).setImage(R.drawable.shopping);
            } else if (imageName.equals("phone")) {
                arrayList.get(position).setImageName("phone");
                arrayList.get(position).setImage(R.drawable.phone);
            } else if (imageName.equals("birthday")) {
                arrayList.get(position).setImageName("birthday");
                arrayList.get(position).setImage(R.drawable.birthday);
            }

            // refresh recycle view
            recycleAdapter.notifyItemChanged(position);

            if (isBlank) {
                arrayList.remove(position);
                textList.remove(positionText);
                recycleAdapter.notifyItemRemoved(position);
            } else {
                // save to textlist
                textList.get(positionText).setDate(date);
                textList.get(positionText).setTitlee(title);
                textList.get(positionText).setNote(note);
            }

            recycleAdapter.setTextList(textList);

            // save updated data permanently
            saveDataPermanently();
        }
    }
}