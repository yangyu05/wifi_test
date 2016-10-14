package com.example.yang.wifi_test.com.example.yang.wifi_test.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by yang on 10/11/2016.
 */

public class MyFragmentAdapter extends FragmentPagerAdapter {
    List<Fragment> listFragements;

    public MyFragmentAdapter(FragmentManager fm, List<Fragment> listFragements) {
        super(fm);
        this.listFragements = listFragements;
    }

    @Override
    public Fragment getItem(int position) {
        return listFragements.get(position);
    }

    @Override
    public int getCount() {
        return listFragements.size();
    }
}
