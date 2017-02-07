package com.mofa.metropolia.architectmuseo.POIDetail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mofa.metropolia.architectmuseo.R;

public class Fragment_ImageFragment extends Fragment {

    private static final String PIC_URI = "PictureURI";
    private static final String PIC_Index = "PictureIndex";

    public static Fragment_ImageFragment newInstance(final String imgBase64){
        Bundle arguments = new Bundle();
        arguments.putString(PIC_URI, imgBase64);

        Fragment_ImageFragment fragment = new Fragment_ImageFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_poi_detail, container, false);
        //ImageView Poi_Image = (ImageView)rootView.findViewById(R.id.fragment_image);
        SimpleDraweeView sdv = (SimpleDraweeView)rootView.findViewById(R.id.fragment_image);

        Bundle arguments = getArguments();
        if (arguments != null){
            /*
            String imgbase64 = arguments.getString(PIC_URI);
            byte[] decodedString = Base64.decode(imgbase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            */

            /*
            //keep the ratio
            Bitmap idealBM = scaleBitmap(decodedByte, Poi_Image);
            Poi_Image.setImageBitmap(idealBM);
            */

            /*
            //dont keep the ratio
            BitmapDrawable ob = new BitmapDrawable(getResources(), decodedByte);
            Poi_Image.setBackground(ob);
            */

            Uri uri = Uri.parse(arguments.getString(PIC_URI));
            sdv.setImageURI(uri);
        }

       sdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getContext(), Activity_ImageFullView.class);
                startActivity(intent);
            }
        });

        return rootView;
    }


    private Bitmap scaleBitmap(Bitmap bitMap, ImageView imageView){
        int bmWidth=bitMap.getWidth();
        int bmHeight=bitMap.getHeight();

        int ivWidth=getActivity().getResources().getDisplayMetrics().widthPixels;
        int ivHeight=getActivity().getResources().getDisplayMetrics().heightPixels;

        int new_width=ivWidth;
        int new_height = (int) Math.floor((double) bmHeight *( (double) new_width / (double) bmWidth));



        Bitmap newbitMap = Bitmap.createScaledBitmap(bitMap,new_width,new_height, true);

        return newbitMap;
    }
}
