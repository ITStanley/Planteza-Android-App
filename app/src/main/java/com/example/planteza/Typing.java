package com.example.planteza;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
public class Typing extends androidx.appcompat.widget.AppCompatTextView{
    private CharSequence myText;
    private int myIndex;
    private long myDelay = 80;

    public Typing(Context context) {
        super(context);
    }

    public Typing(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private final Handler myHandler = new Handler();

    private final Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(myText.subSequence(0,myIndex++));
            if (myIndex<= myText.length()){
                myHandler.postDelayed(characterAdder, myDelay);
            }
        }
    };

    public void animateText(CharSequence myTxt){
        myText = myTxt;
        myIndex = 0;

        setText("");

        myHandler.removeCallbacks(characterAdder);
        myHandler.postDelayed(characterAdder, myDelay);
    }

    public void setCharacterAdder(long n){
        myDelay = n;
    }
}
