package kesh.yoword;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Note extends AppCompatActivity {

    private Button closeBtn, saveBtn;
    private EditText titleTxt, noteTxt;
    private TextView dateTxt;
    private String title, note, date;
    private boolean editBool, savedButtonPressed, sendFirstData;
    private int titleHeight;

    // Image
    private ScrollView scrollView;
    private ImageView imageView;
    private Button imageBtn, camera, imageCloseBtn, scrollDownBtn, scrollUpBtn;
    private String imgDecodableString;
    private boolean isFromGallery;
    private RelativeLayout imageRelativeLayout;
    private int editTextHeight;
    /*
    https://androidclarified.com/pick-image-gallery-camera-android/
     */

    // spinner and images
    private Spinner spinner;
    private ArrayList<ImageItem> imageList;
    private ImageAdapter imageAdapter;
    private String clickedImageName;

    // dialog
    private Dialog dialog;
    private Button yesBtn, noBtn;
    private ImageView closeDialog;

    // prevent from going back to previous activity
    @Override
    public void onBackPressed() {}

    // prevent window leaked warning
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        initiateVariables();
        closeButton();
        setDate();
        spinner();
        saveButton();
        imageIcon();
        retrieveData();
        buildActivity();
    }

    private void initiateVariables() {
        titleTxt = (EditText) findViewById(R.id.title_text);
        noteTxt = (EditText) findViewById(R.id.note_text);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        imageRelativeLayout = (RelativeLayout) findViewById(R.id.image_relative_layout);
        savedButtonPressed = false;

        noteTxt.post(new Runnable() {
            @Override
            public void run() {
                editTextHeight = noteTxt.getHeight(); //height is ready
            }
        });

        noteTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.note_text) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_DOWN:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    private void buildActivity() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, (int) (height - titleHeight));
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: " + titleHeight);


        // add margin
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        this.getWindow().setAttributes(params);
    }

    private void retrieveData() {
        // get data
        SharedPreferences sharedPreferences = getSharedPreferences("edit", MODE_PRIVATE);
        String title = sharedPreferences.getString("title", null);
        String note = sharedPreferences.getString("note", null);
        String imageName = sharedPreferences.getString("image", null);
        date = sharedPreferences.getString("date", null);
        editBool = sharedPreferences.getBoolean("bool", false);
        imgDecodableString = sharedPreferences.getString("photo", null);
        boolean type = sharedPreferences.getBoolean("type", false);
        titleHeight = sharedPreferences.getInt("height", 0);
        imageRelativeLayout.setVisibility(View.GONE);
        scrollDownBtn.setVisibility(View.GONE);
        scrollUpBtn.setVisibility(View.GONE);
        loadHeight();

        // apply to views
        if (editBool) {
            titleTxt.setText(title);
            noteTxt.setText(note);
            dateTxt.setText(date);
            defineArrayList(imageName);

            if (imgDecodableString != null) {
                imageRelativeLayout.setVisibility(View.VISIBLE);
                scrollDownBtn.setVisibility(View.VISIBLE);
                scrollUpBtn.setVisibility(View.VISIBLE);
                if (type == true) {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                    adjustNoteText(true);
                    return;
                } else {
                    imageView.setImageURI(Uri.parse(imgDecodableString));
                    adjustNoteText(true);
                    return;
                }
            }

            adjustNoteText(false);

        } else {
            adjustNoteText(false);
            titleTxt.setText("");
            noteTxt.setText("");
            imgDecodableString = null;
        }
    }

    private void closeButton() {
        // init
        closeBtn = (Button) findViewById((R.id.close));
        closeBtn.setBackgroundResource(R.drawable.close_button);
        dialog = new Dialog(this);

        // position in layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.width = 80;
        params.height = 80;
        params.topMargin = 8;
        params.rightMargin = 8;
        closeBtn.setLayoutParams(params);

        // listeners
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verifyNewInput();

                // not saved
                if (!savedButtonPressed && (!titleTxt.getText().toString().equals("") || !(noteTxt.getText().toString().equals("")))) {
                    dialog();

                    // saved button pressed
                } else {
                    if ((titleTxt.getText().toString().equals("") && (noteTxt.getText().toString().equals("")))) {
                        if (editBool) {
                            saveEdit();
                        } else {
                            Intent intent = new Intent("Item Filter");
                            intent.putExtra("title", "");
                            intent.putExtra("note", "");
                            intent.putExtra("image", "");
                            intent.putExtra("date", "");
                            intent.putExtra("saved", true);
                            // add intent for null photo ??
                            sendBroadcast(intent);
                        }
                    }
                    finish();
                }
            }
        });

        // image close button
        imageCloseBtn = (Button) findViewById((R.id.image_close_btn));
        imageCloseBtn.setBackgroundResource(R.drawable.remove);

        // position in layout
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params2.width = 80;
        params2.height = 80;
        params2.topMargin = 8;
        params2.rightMargin = 8;
        imageCloseBtn.setLayoutParams(params2);

        imageCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StyleableToast.makeText(Note.this, "Photo removed", Toast.LENGTH_LONG, R.style.photoremoved).show();
                imgDecodableString = null;
                imageRelativeLayout.setVisibility(View.GONE);
                scrollUpBtn.setVisibility(View.GONE);
                scrollDownBtn.setVisibility(View.GONE);
            }
        });

        // scroll down
        scrollDownBtn = (Button) findViewById((R.id.scroll_button));
        scrollDownBtn.setBackgroundResource(R.drawable.scroll_down);

        // position in layout
        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params3.width = 130;
        params3.height = 130;
        params3.bottomMargin = 20;
        params3.rightMargin = 45;
        scrollDownBtn.setLayoutParams(params3);

        scrollDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.smoothScrollTo(0, scrollView.getScrollY() + 500);
            }
        });

        // scroll down
        scrollUpBtn = (Button) findViewById((R.id.scroll_up_button));
        scrollUpBtn.setBackgroundResource(R.drawable.scroll_up);

        // position in layout
        RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params4.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params4.width = 130;
        params4.height = 130;
        params4.bottomMargin = 20;
        params4.leftMargin = 45;
        scrollUpBtn.setLayoutParams(params4);

        scrollUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.smoothScrollTo(0, scrollView.getScrollY() - 500);
            }
        });
    }

    private void dialog() {
        dialog.setContentView(R.layout.user_dialog_note_close);
        yesBtn = (Button) dialog.findViewById(R.id.dialog_yes);
        noBtn = (Button) dialog.findViewById(R.id.dialog_no);
        closeDialog = (ImageView) dialog.findViewById(R.id.close_btn_dialog);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editBool) {
                    saveEdit();
                } else {
                    Intent intent = new Intent("Item Filter");
                    intent.putExtra("title", titleTxt.getText().toString());
                    intent.putExtra("note", noteTxt.getText().toString());
                    intent.putExtra("image", clickedImageName);
                    intent.putExtra("date", dateTxt.getText().toString());
                    intent.putExtra("saved", false);
                    intent.putExtra("photo", imgDecodableString);
                    intent.putExtra("type", isFromGallery);
                    sendBroadcast(intent);
                }
                finish();
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // new note
                if (!editBool) {
                    if (sendFirstData) {
                        saveItem();
                    } else {
                        Intent intent = new Intent("Item Filter");
                        intent.putExtra("title", "");
                        intent.putExtra("note", "");
                        intent.putExtra("image", "");
                        intent.putExtra("date", "");
                        intent.putExtra("saved", true);
                        sendBroadcast(intent);
                    }
                    finish();
                }

                // edit
                else {
                    finish();
                }
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.85f);
        dialog.show();
    }

    private void verifyNewInput() {
        if (savedButtonPressed) {
            if (!titleTxt.getText().toString().equals(title) || !noteTxt.getText().toString().equals(note)) {
                savedButtonPressed = false;
                sendFirstData = true;
            }
        }
    }

    private void setDate() {
        dateTxt = (TextView) findViewById(R.id.date);
        Date today = Calendar.getInstance().getTime();//getting date
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        final String sdate = df.format(today);
        dateTxt.setText(sdate);
    }

    private void spinner() {
        // init
        spinner = (Spinner) findViewById(R.id.spinner);

        // add items
        imageList = new ArrayList<>();
        defineArrayList("heart");

        // set adapter
        imageAdapter = new ImageAdapter(this, imageList);
        spinner.setAdapter(imageAdapter);

        // listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ImageItem clickedImage = (ImageItem) parent.getItemAtPosition(position);
                clickedImageName = clickedImage.getImageName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void defineArrayList(String imageName) {
        imageList.clear();

        // recycler image appears on top of spinner
        if (imageName.equals("heart")) {
            imageList.add(new ImageItem(R.drawable.heart, "heart"));
            imageList.add(new ImageItem(R.drawable.music, "music"));
            imageList.add(new ImageItem(R.drawable.soccer, "soccer"));
            imageList.add(new ImageItem(R.drawable.veg, "veg"));
            imageList.add(new ImageItem(R.drawable.shopping, "shop"));
            imageList.add(new ImageItem(R.drawable.phone, "phone"));
            imageList.add(new ImageItem(R.drawable.birthday, "birthday"));
        } else if (imageName.equals("shopping")) {
            imageList.add(new ImageItem(R.drawable.shopping, "shop"));
            imageList.add(new ImageItem(R.drawable.heart, "heart"));
            imageList.add(new ImageItem(R.drawable.music, "music"));
            imageList.add(new ImageItem(R.drawable.soccer, "soccer"));
            imageList.add(new ImageItem(R.drawable.veg, "veg"));
            imageList.add(new ImageItem(R.drawable.phone, "phone"));
            imageList.add(new ImageItem(R.drawable.birthday, "birthday"));
        } else if (imageName.equals("soccer")) {
            imageList.add(new ImageItem(R.drawable.soccer, "soccer"));
            imageList.add(new ImageItem(R.drawable.heart, "heart"));
            imageList.add(new ImageItem(R.drawable.music, "music"));
            imageList.add(new ImageItem(R.drawable.veg, "veg"));
            imageList.add(new ImageItem(R.drawable.shopping, "shop"));
            imageList.add(new ImageItem(R.drawable.phone, "phone"));
            imageList.add(new ImageItem(R.drawable.birthday, "birthday"));
        } else if (imageName.equals("veg")) {
            imageList.add(new ImageItem(R.drawable.veg, "veg"));
            imageList.add(new ImageItem(R.drawable.heart, "heart"));
            imageList.add(new ImageItem(R.drawable.music, "music"));
            imageList.add(new ImageItem(R.drawable.soccer, "soccer"));
            imageList.add(new ImageItem(R.drawable.shopping, "shop"));
            imageList.add(new ImageItem(R.drawable.phone, "phone"));
            imageList.add(new ImageItem(R.drawable.birthday, "birthday"));
        } else if (imageName.equals("shop")) {
            imageList.add(new ImageItem(R.drawable.shopping, "shop"));
            imageList.add(new ImageItem(R.drawable.heart, "heart"));
            imageList.add(new ImageItem(R.drawable.music, "music"));
            imageList.add(new ImageItem(R.drawable.soccer, "soccer"));
            imageList.add(new ImageItem(R.drawable.veg, "veg"));
            imageList.add(new ImageItem(R.drawable.phone, "phone"));
            imageList.add(new ImageItem(R.drawable.birthday, "birthday"));
        } else if (imageName.equals("phone")) {
            imageList.add(new ImageItem(R.drawable.phone, "phone"));
            imageList.add(new ImageItem(R.drawable.birthday, "birthday"));
            imageList.add(new ImageItem(R.drawable.heart, "heart"));
            imageList.add(new ImageItem(R.drawable.music, "music"));
            imageList.add(new ImageItem(R.drawable.soccer, "soccer"));
            imageList.add(new ImageItem(R.drawable.veg, "veg"));
            imageList.add(new ImageItem(R.drawable.shopping, "shop"));
        } else if (imageName.equals("birthday")) {
            imageList.add(new ImageItem(R.drawable.birthday, "birthday"));
            imageList.add(new ImageItem(R.drawable.shopping, "shop"));
            imageList.add(new ImageItem(R.drawable.heart, "heart"));
            imageList.add(new ImageItem(R.drawable.music, "music"));
            imageList.add(new ImageItem(R.drawable.soccer, "soccer"));
            imageList.add(new ImageItem(R.drawable.veg, "veg"));
            imageList.add(new ImageItem(R.drawable.phone, "phone"));
        }
    }

    private void imageIcon() {
        imageBtn = (Button) findViewById(R.id.image_iconn);
        imageView = (ImageView) findViewById(R.id.image_icon);
        camera = (Button) findViewById(R.id.camera_icon);
        imageRelativeLayout.setVisibility(View.GONE);

        // listener
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageRelativeLayout.setVisibility(View.VISIBLE);
                isFromGallery = true;
                pickImageFromGallery();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageRelativeLayout.setVisibility(View.VISIBLE);
                isFromGallery = false;
                captureFromCamera();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Note.this, GalleryImage.class);
                intent.putExtra("image", imgDecodableString);
                intent.putExtra("isGallery", isFromGallery);
                startActivity(intent);
            }
        });
    }

    private void captureFromCamera() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
            startActivityForResult(intent, 5);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        //This is the directory in which the file will be created. This is the default location of Camera photos
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for using again
        imgDecodableString = "file://" + image.getAbsolutePath();

        return image;
    }

    private void pickImageFromGallery() {
        // intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, 4);
    }

    // handle result of picked image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 4) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imgDecodableString = cursor.getString(columnIndex);
            cursor.close();
            imageView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
            scrollDownBtn.setVisibility(View.VISIBLE);
            scrollUpBtn.setVisibility(View.VISIBLE);
            adjustNoteText(true);
            saveNoteHeight();
            scrollToast();
        }

        // camera
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            imageView.setImageURI(Uri.parse(imgDecodableString));
            scrollDownBtn.setVisibility(View.VISIBLE);
            scrollUpBtn.setVisibility(View.VISIBLE);
            adjustNoteText(true);
            saveNoteHeight();
            scrollToast();
        }
    }

    private void adjustNoteText(boolean imageClicked) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.topMargin = 8;
        params.rightMargin = 25;
        params.leftMargin = 25;
        titleTxt.setLayoutParams(params);

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        if (imageClicked) {
            params2.height = editTextHeight;
        }

        params2.topMargin = 25;
        params2.rightMargin = 25;
        params2.leftMargin = 25;
        noteTxt.setLayoutParams(params2);
    }

    private void scrollToast() {
        StyleableToast.makeText(Note.this, "Scroll using finger or button\nto view your photo", Toast.LENGTH_LONG, R.style.scrolltoast).show();
    }

    private void saveNoteHeight() {
        SharedPreferences sharedPreferences = getSharedPreferences("Height", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("height", editTextHeight);
        editor.apply();
    }

    private void loadHeight() {
        SharedPreferences sharedPreferences = getSharedPreferences("Height", MODE_PRIVATE);
        editTextHeight = sharedPreferences.getInt("height", 0);
        noteTxt.setMaxHeight(editTextHeight);
    }

    private void saveItem() {
        // declare object to send to MainActivity class
        Intent intent = new Intent("Item Filter");
        intent.putExtra("title", title);
        intent.putExtra("note", note);
        intent.putExtra("image", clickedImageName);
        intent.putExtra("date", date);
        intent.putExtra("saved", false);
        intent.putExtra("photo", imgDecodableString);
        intent.putExtra("type", isFromGallery);
        sendBroadcast(intent);
    }

    private void saveButton() {
        // init
        saveBtn = (Button) findViewById((R.id.save));
        saveBtn.setBackgroundResource(R.drawable.save);

        // position in layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.width = 200;
        params.height = 200;

        saveBtn.setLayoutParams(params);

        // listeners
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // save data
                title = titleTxt.getText().toString();
                note = noteTxt.getText().toString();
                setDate();
                date = dateTxt.getText().toString();

                // set borderline
                if (note.equals("")) {
                    noteTxt.setBackgroundResource(R.drawable.textview_red_borderline);
                } else {
                    noteTxt.setBackgroundResource(R.drawable.textview_black_borderline);
                }

                if (title.equals("")) {
                    titleTxt.setBackgroundResource(R.drawable.textview_red_borderline);
                } else {
                    titleTxt.setBackgroundResource(R.drawable.textview_black_borderline);
                }

                // toast
                if ((title.equals("") && note.equals(""))) {
                    errorAnimation(v, "You must enter a title and a note!");
                } else if (title.equals("")) {
                    errorAnimation(v, "You must enter a title!");
                } else if (note.equals("")) {
                    errorAnimation(v, "You must enter a note!");
                }

                // if both is not empty
                if (!(title.equals("") && note.equals(""))) {
                    savedButtonPressed = true;
                }

                // new button
                if (!editBool) {
                    if (!(title.equals("") || note.equals(""))) {
                        // send data
                        savedButtonPressed = true;
                        saveBtn.startAnimation(AnimationUtils.loadAnimation(Note.this, R.anim.bounce));
                        noteSavedAnimation();
                        saveItem();
                    }
                }

                // edit button
                else {
                    if (!(title.equals("") || note.equals(""))) {
                        // send data
                        saveBtn.startAnimation(AnimationUtils.loadAnimation(Note.this, R.anim.bounce));
                        savedButtonPressed = true;
                        noteSavedAnimation();
                        saveEdit();
                        finish();
                    }
                }
            }
        });
    }

    private void saveEdit() {
        Intent intent = new Intent();
        intent.putExtra("title", titleTxt.getText().toString());
        intent.putExtra("note", noteTxt.getText().toString());
        intent.putExtra("image", clickedImageName);
        intent.putExtra("date", dateTxt.getText().toString());
        intent.putExtra("photo", imgDecodableString);
        intent.putExtra("type", isFromGallery);
        setResult(RESULT_OK, intent);
    }

    private void noteSavedAnimation() {
        StyleableToast.makeText(Note.this, "Your note has been saved!", Toast.LENGTH_LONG, R.style.mytoast).show();
    }

    private void errorAnimation(View v, String message) {
        Snackbar snackbar = Snackbar.make(v, message, Snackbar.LENGTH_LONG);
        View snackView = snackbar.getView();
        snackView.startAnimation(AnimationUtils.loadAnimation(Note.this, R.anim.fade_transition_anim));
        snackView.setBackgroundColor(Color.parseColor("#ff4c4c"));
        snackbar.show();
    }
}