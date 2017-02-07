package com.mofa.metropolia.architectmuseo.POIListView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.BadTokenException;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.facebook.drawee.view.SimpleDraweeView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mofa.metropolia.architectmuseo.Constains_BackendAPI_Url;
import com.mofa.metropolia.architectmuseo.Object_POI;
import com.mofa.metropolia.architectmuseo.POIDetail.Activity_POIActivity;
import com.mofa.metropolia.architectmuseo.POIRecognition.CamActivity;
import com.mofa.metropolia.architectmuseo.R;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Fragment_TabFragment extends Fragment {

    private static final String ARG_PARM1 = "SortingMethodID";
    private static final String ARG_PARM2 = "LocationString";
    private static final String ARG_PARM3 = "CategoryString";
    private static final String TAG = "Fragment_Tab";

    protected StableArrayAdapter adapter;

    public static Fragment_TabFragment newInstance(int ID, String locatStr, String cateStr) {
        Fragment_TabFragment fragment = new Fragment_TabFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_PARM1, ID);
        args.putString(ARG_PARM2, locatStr);
        args.putString(ARG_PARM3, cateStr);

        fragment.setArguments(args);
        return fragment;
    }

    //Empty constructor is needed to implement newInstance
    public Fragment_TabFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final ArrayList<Object_POI> result = new ArrayList<>();

        final ProgressDialog progressDialog = createProgressDialog(getActivity());

        CoordinatorLayout myView = (CoordinatorLayout) inflater.inflate(R.layout.fragment_poi_list_tab, container, false);

        FloatingActionButton fab_cam = (FloatingActionButton)myView.findViewById(R.id.poi_list_fab_cam);
        fab_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(CamActivity.ARG_LOCATION, getArguments().getString(ARG_PARM2));
                intent.setClass(getContext(), CamActivity.class);
                intent.putExtra("mode", 1);
                startActivity(intent);
            }
        });

        final ListView listView = (ListView) myView.findViewById(R.id.POIlistview);

        String url = null;

        final int sortingMethodID = getArguments().getInt(ARG_PARM1);
        final String categoryStr;
        switch (sortingMethodID) {
            case 0:
                String locationStr = getArguments().getString(ARG_PARM2);
                // '&' here because it is the second parameter
                categoryStr = "&cata="+getArguments().getString(ARG_PARM3);
                if (!categoryStr.equals("&cata=All")){
                    url = Constains_BackendAPI_Url.URL_POIList_Distant +locationStr+categoryStr;
                }
                else {
                    url = Constains_BackendAPI_Url.URL_POIList_Distant +locationStr;
                }
                break;
            case 1:
                //setListViewByMostviewed();
                // '?' here because it is the second parameter
                categoryStr = "?cata="+getArguments().getString(ARG_PARM3);
                if (!categoryStr.equals("?cata=All")){
                    url = Constains_BackendAPI_Url.URL_POIList_Popular+categoryStr;
                }
                else {
                    url = Constains_BackendAPI_Url.URL_POIList_Popular;
                }
                break;
            case 2:
                //setListViewByRecomend();
                url = Constains_BackendAPI_Url.URL_POIList_Suggest;
                break;
            default:
                //url = "http://dev.mw.metropolia.fi/mofa/Wikitude_1/geoLocator/poi.json";
                url = Constains_BackendAPI_Url.URL_POIList_Distant;
                break;
        }

        Network_SerialQueue network_serialQueue = new Network_SerialQueue();
        RequestQueue queue = network_serialQueue.getSerialRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        for (int i = 0; i < response.length(); i++) {

                            String name = null, imgBase64 = null, reason = null;
                            int id = 0, disTo=0, rate_count=0;
                            Double rate_score=0.0;
                            switch (sortingMethodID){
                                case 0:
                                    try {
                                        disTo = response.getJSONObject(i).getInt("distance");
                                        name = response.getJSONObject(i).getString("poi_name");
                                        imgBase64 = response.getJSONObject(i).getString("image");
                                        id = response.getJSONObject(i).getInt("id");

                                    } catch (Exception e) {
                                      }
                                    break;
                                case 1:
                                    try {
                                        rate_count = response.getJSONObject(i).getInt("rate_count");
                                        rate_score = response.getJSONObject(i).getDouble("rate_score");
                                        name = response.getJSONObject(i).getString("poi_name");
                                        imgBase64 = response.getJSONObject(i).getString("image");
                                        id = response.getJSONObject(i).getInt("id");
                                    } catch (Exception e) {
                                        Log.e("ResponsePopError", e.toString());
                                    }
                                    break;
                                case 2:
                                    try {
                                        id = response.getJSONObject(i).getInt("id");
                                        name = response.getJSONObject(i).getString("poi_name");
                                        imgBase64 = response.getJSONObject(i).getString("image");
                                        reason = response.getJSONObject(i).getString("reason");
                                    } catch (Exception e) {

                                    }

                                default:
                                    break;

                            }
                            Object_POI temp = new Object_POI(0, 0, name, id, imgBase64,null,disTo,rate_score,
                                                            rate_count, null,0, reason,null,null);
                            result.add(temp);
                        }

                        adapter = new StableArrayAdapter(getContext(), result, sortingMethodID);
                        listView.setAdapter(adapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG + " error", error.toString());
                        Toast.makeText(getContext(), "No Internet", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        JsonArrayRequest modelNamesReq = new JsonArrayRequest(
                Request.Method.GET, Constains_BackendAPI_Url.URL_GetModelsName, null,
                new Response.Listener<JSONArray>() {
                    public void onResponse(JSONArray response) {
                        ArrayList<String> nameList = new ArrayList<>();
                        ArrayList<Long> dateList = new ArrayList<>();
                        for (int i=0; i<response.length(); i++){
                            String nameTemp = null;
                            Long dateTemp = 0l;
                            try {
                                nameTemp = response.getJSONObject(i).getString("poi_name");
                                dateTemp = response.getJSONObject(i).getLong("date");
                            } catch (Exception e) {
                                Log.e("Name List Json Error", e.toString());
                            }
                            nameList.add(nameTemp);
                            dateList.add(dateTemp);
                        }
                        /**/

                        for (int i = 0; i < nameList.size(); i++){
                            Download3dModels(nameList.get(i), dateList.get(i));
                        }

                        /*
                        Download3dModels("LauttasaariWaterTower");
                        */
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Name List Req Error", error.toString());
                        progressDialog.dismiss();
                    }
                }
        );


        if (url != null ) {
            queue.add(jsonArrayRequest);
            queue.add(modelNamesReq);
            progressDialog.show();
        }

        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(Activity_POIActivity.ARG_ID, result.get(position).getID());
                intent.setClass(getContext(), Activity_POIActivity.class);
                startActivity(intent);
            }
        };
        listView.setOnItemClickListener(onItemClickListener);

        return myView;
    }

    private Boolean Download3dModels(String fileName, long date) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        fileName= fileName.trim();

        File dir = new File(Environment.getExternalStorageDirectory()+"/3dModels"+"/");

        if (!dir.exists()){
            dir.mkdirs();
        }

        try {
            File file = new File (dir, fileName+".wt3");
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            //Log.e("Phone",sdf.format(file.lastModified()));
            //Log.e("DataBase",sdf.format(new Date(date*1000l)));
            if (file.exists() && file.lastModified() >= date*1000l){

                //Log.e("3d model not downloaded",sdf.format(file.lastModified()));
                return false;
            }
            else{
                String url = Constains_BackendAPI_Url.URL_3dModels+fileName+".wt3";
                URL downloadUrl = new URL(url);
                URLConnection ucon = downloadUrl.openConnection();
                ucon.connect();

                InputStream is = ucon.getInputStream();

                FileOutputStream fos = new FileOutputStream(file);

                byte data[] = new byte[1024];

                int current = 0;
                while ((current = is.read(data))!=-1){
                    fos.write(data,0,current);
                }
                is.close();
                fos.flush();
                fos.close();

                //Log.e("DownloadSucceed", fileName+".wt3 Downloaded");
                return true;
            }

        } catch (Exception e) {

            return false;
        }
    }


    private class StableArrayAdapter extends ArrayAdapter<Object_POI> {
        private final Context context;
        private int sortID;

        //list of the name of the poi
        private final List<Object_POI> values;


        public StableArrayAdapter(Context context, List<Object_POI> objects, int sortID) {
            super(context, -1, objects);
            this.context = context;
            this.values = objects;
            this.sortID = sortID;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.layout_poi_list_row_layout, parent, false);

            TextView Title = (TextView) rowView.findViewById(R.id.POIRowFriLine);
            TextView RowTwo = (TextView)rowView.findViewById(R.id.POIRowSecLine);
            //ImageView imageView = (ImageView) rowView.findViewById(R.id.POIRowImage);
            SimpleDraweeView sdv = (SimpleDraweeView)rowView.findViewById(R.id.POIRowImage);
            TextView rateScore = (TextView)rowView.findViewById(R.id.POIRowRateScore);
            RatingBar rateBar = (RatingBar)rowView.findViewById(R.id.POIRowRateBar);

            /*
            byte[] decodedString = Base64.decode(values.get(position).getImgBase64(), Base64.DEFAULT);
            if (decodedString!=null){
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                //imageView.setImageBitmap(decodedByte);
                BitmapDrawable ob = new BitmapDrawable(getResources(), decodedByte);
                imageView.setBackground(ob);
            }
            */
            Uri uri = Uri.parse(values.get(position).getImgBase64());
            sdv.setImageURI(uri);

            Title.setText(values.get(position).getName());

            switch (sortID){
                case 0:
                    double dis = values.get(position).getDisTo()/(float)1000;
                    String s = String.format("%.2f", dis);
                    RowTwo.setText(s + " km");
                    break;
                case 1:
                    rateBar.setEnabled(false);
                    rateBar.setVisibility(View.VISIBLE);
                    rateBar.setRating((float) values.get(position).getRate_score());
                    s = String.format("%.2f", values.get(position).getRate_score());
                    rateScore.setText(s+" / 5.00");
                    break;
                case 2:
                    RowTwo.setText(values.get(position).getReasonForSug());
                default:
                    break;
            }

            return rowView;
        }
    }

    public static ProgressDialog createProgressDialog(Context mContext){
        ProgressDialog result = new ProgressDialog(mContext);
        try {
            result.show();
        }
        catch (BadTokenException e){

        }
        result.setCancelable(true);
        result.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        result.setContentView(R.layout.layout_progress_dialog);
        result.dismiss();
        return result;
    }

}
