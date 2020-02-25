package com.senior.arexplorer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.senior.arexplorer.Utils.Settings;

public class FilterFragment extends DialogFragment implements TextWatcher {
    private String prevFilter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filter, container, false);
        this.prevFilter = Settings.getInstance().getFilter();

        EditText filterInput = v.findViewById(R.id.filterInput);
        filterInput.setText(Settings.getInstance().getFilter());
        filterInput.addTextChangedListener(this);

        v.findViewById(R.id.filter_saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterFragment.this.dismiss();
            }
        });

        v.findViewById(R.id.filter_cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterInput.setText(FilterFragment.this.prevFilter);
                FilterFragment.this.dismiss();
                //filterInput.removeTextChangedListener(FilterFragment.this);
            }
        });

        return v;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Settings.getInstance().setFilter(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
