package com.mofa.metropolia.architectmuseo.POIListView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class Adapter_MyViewPagerAdapter extends FragmentPagerAdapter{

    private final int PAGE_COUNT = 3;
    private String locatStr;
    private String cateStr;

    private String tabTitles[] = new String[]{"Near", "Popular", "Suggest"};


    public Adapter_MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return Fragment_TabFragment.newInstance(0, getLocationStr(), getCateStr());
            case 1:
                return Fragment_TabFragment.newInstance(1, null, getCateStr());
            case 2:
                return Fragment_TabFragment.newInstance(2, null, getCateStr());
            default:
                return Fragment_TabFragment.newInstance(0,getLocationStr(), getCateStr());
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public CharSequence getPageTitle(int position){
        return tabTitles[position];
    }

    public String getLocationStr (){
        return locatStr;
    }

    public void setLocatStr(String locatStr) {
        this.locatStr = locatStr;
    }

    public String getCateStr() {
        return cateStr;
    }

    public void setCateStr(String cateStr) {
        this.cateStr = cateStr;
    }
}
