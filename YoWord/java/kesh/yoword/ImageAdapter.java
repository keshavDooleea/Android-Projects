package kesh.yoword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<ImageItem> {

    // constructor
    public ImageAdapter(Context context, ArrayList<ImageItem> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView (int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.spinner_row, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.spinner_image);

        ImageItem current = getItem(position);

        if (current != null) {
            imageView.setImageResource(current.getImageSrc());
        }

        return convertView;
    }
}
