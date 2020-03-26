package kesh.yoword;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import static android.content.Context.MODE_PRIVATE;

public class BottomSheet extends BottomSheetDialogFragment {

    private Button editButton, deleteButton;
    private BottomSheetListener listener;

    // custom layout
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        editButton(v);
        deleteButton(v);

        return v;
    }

    // define listener
    // can add more variables
    public interface BottomSheetListener {
        void onButtonClicked(String text);
    }

    // assign listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement BottomSheetListener");
        }
    }

    private void editButton(View v) {
        // init
        editButton = v.findViewById(R.id.edit_button);

        // edit button
        editButton.setBackgroundResource(R.drawable.edit);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.width = 135;
        params.height = 135;
        params.bottomMargin = 22;
        params.leftMargin = 150;
        editButton.setLayoutParams(params);

        // listener
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonClicked("edit button");
            }
        });
    }

    private void deleteButton(View v) {
        // init
        deleteButton = v.findViewById(R.id.delete_button);

        // delete button
        deleteButton.setBackgroundResource(R.drawable.delete);
        RelativeLayout.LayoutParams paramsDelete = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsDelete.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        paramsDelete.width = 135;
        paramsDelete.height = 135;
        paramsDelete.bottomMargin = 22;
        paramsDelete.rightMargin = 150;
        deleteButton.setLayoutParams(paramsDelete);

        // listener
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonClicked("delete button");
            }
        });
    }
}
