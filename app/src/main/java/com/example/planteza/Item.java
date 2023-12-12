package com.example.planteza;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {

    private String itemId, itemImage, itemTitle,itemDescription, itemCategory, itemLightening, itemWatering, itemHeight, itemPrice, uid;

    public Item() {
        // Default constructor required for Firebase database
    }

    protected Item(Parcel in) {
        itemId = in.readString();
        itemImage = in.readString();
        itemTitle = in.readString();
        itemDescription = in.readString();
        itemCategory = in.readString();
        itemLightening = in.readString();
        itemWatering = in.readString();
        itemHeight = in.readString();
        itemPrice = in.readString();
        uid = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String getItemId() {
        return itemId;
    }

    public String getItemImage() {
        return itemImage;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(itemImage);
        dest.writeString(itemTitle);
        dest.writeString(itemDescription);
        dest.writeString(itemCategory);
        dest.writeString(itemLightening);
        dest.writeString(itemWatering);
        dest.writeString(itemHeight);
        dest.writeString(itemPrice);
        dest.writeString(uid);
    }
}