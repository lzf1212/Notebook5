package com.crdir.iMemo;

import android.app.Fragment;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SimpleContentActivity extends AppCompatActivity {
    public static final String EXTRA_FILE = "file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            String file = getIntent().getStringExtra(EXTRA_FILE);
            Fragment f = SimpleContentFragment.newInstance(file);
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, f).commit();
        }
    }
}
