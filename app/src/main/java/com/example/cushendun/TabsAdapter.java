package com.example.cushendun;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    HomeFragment home = new HomeFragment();
    MoonFragment moon = new MoonFragment();
    TideFragment tide = new TideFragment();
    PlanetFragment planet = new PlanetFragment();

    public TabsAdapter(FragmentManager fm, int NoOfTabs){
        super(fm);
        this.mNumOfTabs = NoOfTabs;
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return home;
            case 1:
                return moon;
            case 2:
                return tide;
            case 3:
                return planet;
            default:
                return null;
        }
    }
}