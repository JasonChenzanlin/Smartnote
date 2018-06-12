package com.example.jasonchen.smartnote.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Jason Chen on 2017/10/26.
 */
public class MyPagerAdapter extends PagerAdapter{

    private List<View> viewList;
    private List<String> titleList;

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }

    public MyPagerAdapter(List<View> viewList, List<String> titleList){
        this.viewList=viewList;
        this.titleList=titleList;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(viewList.get(position));
        return viewList.get(position);
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }
}
