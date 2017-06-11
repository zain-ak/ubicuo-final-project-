package com.example.airport;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/**
 * Created by Zain on 4/23/17.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;
    int color, darkColor;
    private String tabNames[] = {"Tutorials", "Labs"};
    private String subjectName, studentID, subjectCode;

    public ViewPagerAdapter(FragmentManager fm, int NumOfTabs, int color, int darkColor, String subjName, String stuID, String subjectCode) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.color = color;
        this.darkColor = darkColor;
        subjectName = subjName;
        studentID = stuID;
        this.subjectCode = subjectCode;

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabNames[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TutorialFragment tabOne = new TutorialFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("Color", color);
                bundle.putInt("DarkColor", darkColor);
                bundle.putString("subjectName", subjectName);
                bundle.putString("ID", studentID);
                bundle.putString("subjectID", subjectCode);
                tabOne.setArguments(bundle);

                return tabOne;
            case 1:
                LabFragment tabTwo = new LabFragment();
                Bundle bundleTwo = new Bundle();
                bundleTwo.putInt("Color", color);
                bundleTwo.putInt("DarkColor", darkColor);
                bundleTwo.putString("subjectName", subjectName);
                bundleTwo.putString("ID", studentID);
                bundleTwo.putString("subjectID", subjectCode);
                tabTwo.setArguments(bundleTwo);
                return tabTwo;
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
