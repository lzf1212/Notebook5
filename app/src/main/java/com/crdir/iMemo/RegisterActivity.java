package com.crdir.iMemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText mEtAccount, mEtName, mEtPassword, mEtPasswordAgain;
    private Button mBtRegister;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register);
        initView();
        initClick();
    }

    @SuppressLint("CommitPrefEdits")
    private void initView() {
        mEtAccount = findViewById(R.id.et_account);
        mEtName = findViewById(R.id.et_name);
        mEtPassword = findViewById(R.id.et_password);
        mEtPasswordAgain = findViewById(R.id.et_passwordagain);
        mBtRegister = findViewById(R.id.bt_register);
        mEditor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
    }

    private void initClick() {
        mBtRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_register:
                if (mEtAccount.getText().toString().isEmpty() || mEtName.getText().toString().isEmpty()
                        || mEtPassword.getText().toString().isEmpty() || mEtPasswordAgain.getText().toString().isEmpty()) {
                    Toast.makeText(this, "所有信息不可为空！", Toast.LENGTH_SHORT).show();
                } else if (!mEtPassword.getText().toString().equals(mEtPasswordAgain.getText().toString())) {
                    Toast.makeText(this, "密码不一致！", Toast.LENGTH_SHORT).show();
                } else {
                    mEditor.putString("account", mEtAccount.getText().toString());
                    mEditor.putString("password", mEtPassword.getText().toString());
                    mEditor.putString("name", mEtName.getText().toString());
                    mEditor.putString("phone","");
                    mEditor.putString("icon","");
                    mEditor.putInt("money",0);
                    mEditor.putString("shoppingcart","[]");
                    mEditor.putString("address","广东省");
                    mEditor.commit();
                    Intent intent = new Intent();
                    intent.putExtra("account", mEtAccount.getText().toString());
                    intent.putExtra("password", mEtPassword.getText().toString());
                    setResult(RESULT_OK, intent);
                    Toast.makeText(this, "成功注册！", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}