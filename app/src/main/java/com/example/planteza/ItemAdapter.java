package com.example.planteza;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
public class ItemAdapter extends BaseAdapter {
    private final Context context;
    private List<Item> itemList;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }
    public void setItems(List<Item> items) {
        itemList = items;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cell, parent, false);
        }
        convertView.setBackgroundColor(Color.TRANSPARENT);

        ImageView itemIconImageView = convertView.findViewById(R.id.itemImage);
        TextView itemTitleTextView = convertView.findViewById(R.id.itemName);
        TextView itemCategoryTextView = convertView.findViewById(R.id.itemCat);
        TextView itemPriceTextView = convertView.findViewById(R.id.itemPrice);

        Item item = itemList.get(position);

        Picasso.get().load(item.getItemImage()).into(itemIconImageView);
        itemTitleTextView.setText(item.getItemTitle());
        itemCategoryTextView.setText(item.getItemCategory());
        itemPriceTextView.setText("Rs. " + item.getItemPrice());

        return convertView;
    }
}

