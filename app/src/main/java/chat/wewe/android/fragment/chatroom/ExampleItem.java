package chat.wewe.android.fragment.chatroom;

import android.graphics.Bitmap;

public class ExampleItem {
    private Bitmap mImageResource;
    private String mText1;

    public ExampleItem(Bitmap imageResource, String text1) {
        mImageResource = imageResource;
        mText1 = text1;
    }

    public Bitmap getImageResource() {
        return mImageResource;
    }

    public String getText1() {
        return mText1;
    }

}