package com.mofa.metropolia.architectmuseo.POIDetail;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mofa.metropolia.architectmuseo.R;

import java.util.List;

public class Adapter_ImageFullScreenAdapter extends PagerAdapter {

    private Activity activity;
    private List<String> picList;
    private LayoutInflater inflater;

    public Adapter_ImageFullScreenAdapter(Activity activity, List<String> picList) {
        this.activity = activity;
        this.picList = picList;
    }

    @Override
    public int getCount() {
        return this.picList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view ==((RelativeLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_image_full_screent, container, false);

        /*
        byte[] decodedString = Base64.decode(picList.get(position), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        imgDisplay = (ImageView)viewLayout.findViewById(R.id.ImageView_ImageFullScreen);
        imgDisplay.setImageBitmap(decodedByte);
        */

        SimpleDraweeView sdv = (SimpleDraweeView)viewLayout.findViewById(R.id.ImageView_ImageFullScreen);
        Uri uri = Uri.parse(picList.get(position));
        sdv.setImageURI(uri);


        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager)container).removeView((RelativeLayout)object);
    }
}
