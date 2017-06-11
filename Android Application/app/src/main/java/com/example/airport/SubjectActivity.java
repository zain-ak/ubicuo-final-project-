package com.example.airport;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;


/**
 * Created by Zain on 4/23/17.
 */

public class SubjectActivity extends AppCompatActivity {

    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent thisIntent = getIntent();
        String subjectID = thisIntent.getStringExtra("subjectCode");
        final int subjectColor = thisIntent.getIntExtra("subjectColor", 0);
        final int darkSubjectColor = colorDarkener(subjectColor);
        String subjectName = thisIntent.getStringExtra("subjectName");
        String studentID = thisIntent.getStringExtra("ID");
        //final int lightSubjectColor = colorLightener(subjectColor);

        getSupportActionBar().setTitle(subjectID);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(subjectColor));
        Window window = getWindow();
        window.setStatusBarColor(darkSubjectColor);


        setContentView(R.layout.activity_subject);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setSelectedTabIndicatorColor(darkSubjectColor);
        tabLayout.addTab(tabLayout.newTab().setText("Tutorials"));
        tabLayout.addTab(tabLayout.newTab().setText("Labs"));


        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), subjectColor, darkSubjectColor, subjectName, studentID, subjectID);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                viewPager.getChildAt(0).setBackground(new ColorDrawable(subjectColor));
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
           }
        });
    }

    private int colorLightener(int subjectColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(subjectColor, hsv);
        hsv[2] = (float) Math.pow(hsv[2], 1/0.25f);  // value component
        subjectColor = Color.HSVToColor(hsv);

        return subjectColor;
    }

    private int colorDarkener(int subjectColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(subjectColor, hsv);
        hsv[2] *= 0.65f; // value component
        subjectColor = Color.HSVToColor(hsv);

        return subjectColor;
    }
}
