package com.crdir.iMemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Fragment> fragments;
    private ViewPager viewPager;
    private RadioGroup radioGroup;
    private ActionBar actionBar;
    public static String[] SexValueSet = {"♀", "♂"};
    private UsrInfo usrInfo = new UsrInfo();
    private SharedPreferences sharedPreferences;
    private Calendar calendar;

    private class UsrInfo {

        private String name;
        private String sexValue;
        private int sexNumber;
        private String star;
        private String ImageFilePath;

        public UsrInfo() {
            this.name = "";
            this.sexValue = "";
            this.sexNumber = 0;
            this.star = "";
            ImageFilePath = "";
        }

        public String getName() {
            return name;
        }

        public String getSexValue() {
            return sexValue;
        }

        public int getSexNumber() {
            return sexNumber;
        }

        public String getStar() {
            return star;
        }

        public String getImageFilePath() {
            return ImageFilePath;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSexValue(String sexValue) {
            this.sexValue = sexValue;
        }

        public void setSexNumber(int sexNumber) {
            this.sexNumber = sexNumber;
        }

        public void setStar(String star) {
            this.star = star;
        }

        public void setImageFilePath(String imageFilePath) {
            ImageFilePath = imageFilePath;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        RadioButton addButton = (RadioButton) findViewById(R.id.addButton);
        Drawable[] drawables = addButton.getCompoundDrawables();
        drawables[1].setBounds(0, 0, 120, 120);
        addButton.setCompoundDrawables(null, drawables[1], null, null);

        initView();
        initData();
        sharedPreferences = getSharedPreferences("usr_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isUpdateMemoFlag", false);
        editor.putBoolean("isRenameMemoItemFlag", false);
        editor.apply();

        startService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPreferences = getSharedPreferences("usr_info", MODE_PRIVATE);
        usrInfo.setName(sharedPreferences.getString("usrName", "你的名字"));
        usrInfo.setStar(sharedPreferences.getString("usrStar", "XX座"));
        usrInfo.setImageFilePath(sharedPreferences.getString("imageFilePath", ""));
        if (sharedPreferences.getBoolean("usrSex", false))
            usrInfo.setSexNumber(1);
        else usrInfo.setSexNumber(0);
        usrInfo.setSexValue(SexValueSet[usrInfo.getSexNumber()]);
        updateUsrInfo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent i =  new Intent( this, SimpleContentActivity.class)
                    .putExtra(SimpleContentActivity.EXTRA_FILE,
                            "file:///android_asset/misc/about.html");
                startActivity(i);
                return (true);

            case R.id.help:
                i =  new Intent( this, SimpleContentActivity.class)
                        .putExtra(SimpleContentActivity.EXTRA_FILE,
                                "file:///android_asset/misc/help.html");
                startActivity(i);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    public void openLabel(String timeString, String nameSting) {
        Intent intent = new Intent(this, CreatorActivity.class);//在编辑模式下启动CreatorActivity
        intent.putExtra("isNewFlag", false);
        intent.putExtra("createTime", timeString);
        intent.putExtra("labelName", nameSting);
        startActivity(intent);
        sharedPreferences = getSharedPreferences("usr_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isUpdateMemoFlag", true);
        editor.apply();
    }

    public void add(View v) {
        Intent intent = new Intent(this, CreatorActivity.class);//在创建模式下启动CreatorActivity
        intent.putExtra("isNewFlag", true);
        startActivity(intent);
        sharedPreferences = getSharedPreferences("usr_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isUpdateMemoFlag", true);
        editor.apply();
    }

    public void openMemo() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.barColorOfMemo));
            actionBar.setTitle(getResources().getString(R.string.app_name));
        }
        viewPager.setCurrentItem(0);
        changeCheckedColor(0);
    }

    @SuppressLint("SetTextI18n")
    public void openUsr() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.barColorOfUsr));
            actionBar.setTitle(getResources().getString(R.string.usrTitle));
        }
        viewPager.setCurrentItem(1);
        changeCheckedColor(2);
        updateUsrInfo();
    }

    @SuppressLint("SetTextI18n")
    public void updateUsrInfo() {
        TextView nameLabel = findViewById(R.id.usrNameLabel);
        TextView otherInfoLabel = findViewById(R.id.usrOtherInfoLabel);
        ImageView imageView = findViewById(R.id.imageViewOfUsr);
        if (nameLabel != null)
            nameLabel.setText(usrInfo.getName());
        if (otherInfoLabel != null)
            otherInfoLabel.setText(usrInfo.getStar() + " " + usrInfo.getSexValue());
        if (imageView != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(usrInfo.getImageFilePath());
            imageView.setImageBitmap(bitmap);
        }
    }

    private void initData() {//viewPager绑定Adapter
        fragments = new ArrayList<Fragment>();
        fragments.add(new MemoDisplayFragment());
        fragments.add(new UsrInfoDiaplayFragment());

        MainActPagerAdapter adapter = new MainActPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        ((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.vp);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int index = (position == 1) ? 2 : position;
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(index);
                radioButton.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.rg);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.memoButton:
                        openMemo();
                        break;
                    case R.id.addButton:
                        changeCheckedColor(1);
                        break;
                    case R.id.usrButton:
                        openUsr();
                        break;
                }
            }
        });
    }

    private void changeCheckedColor(int position) {
        RadioButton radioButton = null;
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioButton = (RadioButton) radioGroup.getChildAt(i);
            if (position == i) {
                radioButton.setTextColor(Color.rgb(46, 139, 87));
            } else {
                radioButton.setTextColor(Color.BLACK);
            }
        }
    }

    private class MainActPagerAdapter extends FragmentPagerAdapter {//创建内部类
        public MainActPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public void setUsrInfo(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("usrName", usrInfo.getName());
        intent.putExtra("usrSex", usrInfo.getSexNumber());
        intent.putExtra("usrStar", usrInfo.getStar());
        intent.putExtra("imageFilePath", usrInfo.getImageFilePath());
        startActivity(intent);
    }

    public void openCurrentTransaction(View view) {
        sharedPreferences = getSharedPreferences("usr_info", MODE_PRIVATE);
        String label_id = sharedPreferences.getString("currentTransactionId", "");
        String label_name = sharedPreferences.getString("TransactionContent", "");
        openLabel(label_id, label_name);
    }

    public void updateTransaction() {
        ((UsrInfoDiaplayFragment) fragments.get(1)).setTransaction();
    }

    public void startService() {
        if (!NotificationService.isRunning(this)) {
            Intent startIntent = new Intent(this, NotificationService.class);
            startService(startIntent);
        }
    }
}