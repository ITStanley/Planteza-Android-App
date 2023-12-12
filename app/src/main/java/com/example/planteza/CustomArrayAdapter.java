package com.example.planteza;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
public class CustomArrayAdapter extends ArrayAdapter<CharSequence> {
    private final int hintTextColor;

    public CustomArrayAdapter(Context context, int resource, CharSequence[] objects) {
        super(context, resource, objects);
        this.hintTextColor = ContextCompat.getColor(context, R.color.hint_text_color);
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        // Set the text color for the disabled item
        if (!isEnabled(position)) {
            TextView textView = (TextView) view;
            textView.setTextColor(hintTextColor);
        }
        return view;

    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        // Set the text color for the disabled item in the dropdown
        if (!isEnabled(position)) {
            TextView textView = (TextView) view;
            textView.setTextColor(hintTextColor);
        }
        return view;

    }
}


