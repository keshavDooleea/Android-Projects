package kesh.yoword;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;

public class GalleryImage extends AppCompatActivity {

    private String image;
    private boolean isFromGallery;
    private Button closeBtn;
    private ImageView imageView;

    // prevent from going back to previous activity
    @Override
    public void onBackPressed() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_image);

        getAttributes();
        setLayout();
    }

    private void getAttributes() {
        Intent intent = getIntent();
        image = intent.getStringExtra("image");
        isFromGallery = intent.getBooleanExtra("isGallery", false);
    }

    private void setLayout() {
        // close button
        closeBtn = (Button) findViewById(R.id.gallery_close_button);
        closeBtn.setBackgroundResource(R.drawable.close_button);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params2.width = 80;
        params2.height = 80;
        params2.topMargin = 8;
        params2.rightMargin = 8;
        closeBtn.setLayoutParams(params2);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // image
        imageView = (ImageView) findViewById(R.id.gallery_image_view);
        Bitmap bm = null;

        if (isFromGallery) {
            bm = BitmapFactory.decodeFile(image);
        } else {
            try {
                bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bm.getWidth() > bm.getHeight()) {
            Bitmap bMapRotate = null;
            Matrix mat = new Matrix();
            mat.postRotate(90);
            bMapRotate = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mat, true);
            bm.recycle();
            bm = null;
            imageView.setImageBitmap(bMapRotate);
        } else {
            imageView.setImageBitmap(bm);
        }
    }
}
