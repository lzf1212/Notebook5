package com.crdir.iMemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mEtAccount, mEtPassword;
    private Button mBtEnter;
    private TextView mTvRegister, mTvForgetPwd;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);
        initView();
        initClick();
    }

    private void initView() {
        mPref = getSharedPreferences("data", MODE_PRIVATE);
        mEtAccount = findViewById(R.id.et_account);
        mEtPassword = findViewById(R.id.et_password);
        mBtEnter = findViewById(R.id.bt_enter);
        mTvForgetPwd = findViewById(R.id.tv_forgepwd);
        mTvRegister = findViewById(R.id.tv_register);
    }

    private void initClick() {
        mBtEnter.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
        mTvForgetPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_enter:
                if (mEtAccount.getText().toString().isEmpty() || mEtPassword.getText().toString().isEmpty()) {
                    Toast.makeText(this, "账号或密码不可为空！", Toast.LENGTH_SHORT).show();
                } else if (!mEtAccount.getText().toString().equals(mPref.getString("account", null))) {
                    Toast.makeText(this, "账号不存在！", Toast.LENGTH_SHORT).show();
                } else if (!mEtPassword.getText().toString().equals(mPref.getString("password", null))) {
                    Toast.makeText(this, "密码不正确！", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.tv_forgepwd:
                Intent intentForgetPwd = new Intent(this, ForgetpwdActivity.class);
                startActivityForResult(intentForgetPwd, 1);
                break;
            case R.id.tv_register:
                Intent intentRegister = new Intent(this, RegisterActivity.class);
                startActivityForResult(intentRegister, 2);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
            case 2:
                if (resultCode == RESULT_OK) {
                    System.out.println("requestCode:" + requestCode);
                    mEtAccount.setText(data.getStringExtra("account"));
                    mEtPassword.setText(data.getStringExtra("password"));
                }
                break;
        }

    }
}