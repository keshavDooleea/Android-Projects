package kesh.yoword;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemsViewHolder> implements Filterable {

    // variable
    private ArrayList<Items> arrayList, arrayListFilter;
    private OnItemClickListener clickListener;
    private OnItemLongClickListner longClickListner;
    private Context context;

    // .txt
    private File file;
    private static final String FILE_NAME = "YoWord.txt";
    private ArrayList<Items> textList;

    // search
    @Override
    public Filter getFilter() {


        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                String key = constraint.toString();

                if (key.isEmpty()) {
                    arrayListFilter = arrayList;
                } else {
                    ArrayList<Items> listFiltered = new ArrayList<>();

                    for (Items item : arrayList) {
                        if (item.getTitlee().toLowerCase().contains(key.toLowerCase()) ||
                            item.getNote().toLowerCase().contains(key.toLowerCase())) {
                            listFiltered.add(item);
                        }
                    }
                    arrayListFilter = listFiltered;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = arrayListFilter;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                arrayListFilter = (ArrayList<Items>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // interface for click listeners
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // interface for long click
    public interface OnItemLongClickListner {
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListner listener) {
        this.longClickListner = listener;
    }

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView titleTxt, noteTxt;
        public CardView itemsRelative;

        public ItemsViewHolder(@NonNull View itemView, final OnItemClickListener clickListener, final OnItemLongClickListner longListener) {
            super(itemView);
            itemsRelative = itemView.findViewById(R.id.items_card);
            image = itemView.findViewById(R.id.imageView);
            titleTxt = itemView.findViewById(R.id.TitleTextView);
            noteTxt = itemView.findViewById(R.id.NoteTextView);

            // click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            clickListener.onItemClick(position);
                        }
                    }
                }
            });

            // on long click listener
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (longListener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            longListener.onItemLongClick(position);
                        }
                    }

                    return true;
                }
            });
        }
    }

    public ItemsAdapter(ArrayList<Items> itemList, ArrayList<Items> textList) {
        this.arrayList = itemList;
        this.textList = textList;
        this.arrayListFilter = itemList;
    }

    public int getListPosition(int position) {
        // find content
        String title = arrayListFilter.get(position).getTitlee();
        String note = arrayListFilter.get(position).getNote();
        String date = arrayListFilter.get(position).getDate();

        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).getTitlee().equals(title) && arrayList.get(i).getNote().equals(note)
                && arrayList.get(i).getDate().equals(date)) {

                return i;
            }
        }

        // never the case
        return -1;
    }

    public void updateEditFilter(int position, String title, String note, String date, String photo, boolean photoType) {
        arrayListFilter.get(position).setTitlee(title);
        arrayListFilter.get(position).setNote(note);
        arrayListFilter.get(position).setDate(date);
        arrayListFilter.get(position).setPhoto(photo);
        arrayListFilter.get(position).setPhotoType(photoType);
        notifyDataSetChanged();
    }

    public ArrayList<Items> getFilterList() {
        return arrayListFilter;
    }

    public void setArrayListFilter(ArrayList<Items> filter) {
        this.arrayListFilter = filter;
    }

    public void removeItem(int position) {

        // find content
        String title = arrayListFilter.get(position).getTitlee();
        String note = arrayListFilter.get(position).getNote();

        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).getTitlee().equals(title) && arrayList.get(i).getNote().equals(note)) {
                arrayList.remove(i);
                notifyItemRemoved(i);
            }
        }

        arrayListFilter.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);
        ItemsViewHolder iVH = new ItemsViewHolder(v, clickListener, longClickListner);
        context = parent.getContext();

        return iVH;
    }

    public void setTextList(ArrayList<Items> textList) {
        this.textList = textList;
        saveData();
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder holder, int position) {

        Items currentItem = arrayListFilter.get(position);

        // layout
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 8;
        layoutParams.leftMargin = 8;
        layoutParams.rightMargin = 8;
        holder.itemView.setLayoutParams(layoutParams);

        // animations
        holder.itemsRelative.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_scale_anim));
        holder.image.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition_anim));

        // add card
        holder.image.setImageResource(currentItem.getImage());
        holder.titleTxt.setText(currentItem.getTitlee());
        holder.noteTxt.setText(currentItem.getNote());

        // write to .txt file
        saveData();
    }

    @Override
    public int getItemCount() {
        return arrayListFilter.size();
    }

    private void saveData() {
        // create file
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), FILE_NAME);

        // write
        try {
            FileOutputStream fos = new FileOutputStream(file);

            for (int i  = 0; i < textList.size(); i++) {
                String text = textList.get(i).getDate() + "\n" + textList.get(i).getName() + "\n"
                        + "Title: " + textList.get(i).getTitlee() + "\n"
                        + textList.get(i).getNote() + "\n\n";

                // write to bytes
                fos.write(text.getBytes());
            }
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
