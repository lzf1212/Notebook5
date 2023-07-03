package com.crdir.iMemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.crdir.iMemo.SettingActivity.PICK_PHOTO;

public class CreatorActivity extends AppCompatActivity {
    private MemoLabel memoLabel;
    int currentLabelIndex = -1;
    private ScrollView scrollView;
    private LinearLayout linearLayout;
    private String imageFilePath;
    private String parentText;
    private ImageView currentImageView;
    private int selectImageViewIndex = -1;
    private boolean stateIsRemove = false;//使用boolean确定编辑或新建
    private SharedPreferences sharedPreferences;
    private DBHelper dbHelper;
    private boolean isCreator = true;
    private EditText nameLabel;
    private Date createTime;
    public static String TABLENAME_LABEL = "memo_label";
    public static String TABLENAME_ITEM = "memo_item";
    private RelativeLayout currentView;
    private EditText currentEditText;
    private Map<RelativeLayout, String> map = new HashMap<>();
    private Map<EditText, String> mapOfEdit = new HashMap<>();
    public DisplayMetrics dm;
    private String recordLabelName;

    public void scrollToBottom() {
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creator);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        scrollView = findViewById(R.id.labelScrollView);
        linearLayout = findViewById(R.id.labelLinearLayout);
        nameLabel = findViewById(R.id.labelNameLabel);
        sharedPreferences = getSharedPreferences("usr_info", MODE_PRIVATE);
        dbHelper = new DBHelper(this);
        createTime = new Date(System.currentTimeMillis());

        Intent intent = getIntent();
        isCreator = intent.getBooleanExtra("isNewFlag", true);
        if (!isCreator) {
            String createTimeString = intent.getStringExtra("createTime");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss_SSS");
            try {
                createTime = simpleDateFormat.parse(createTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            recordLabelName = intent.getStringExtra("labelName");
            nameLabel.setText(recordLabelName);
            loadData(createTimeString);
        }
    }

    private void updateLabelName() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("label", nameLabel.getText().toString());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss_SSS");
        db.update(TABLENAME_LABEL, values, "_id=?", new String[]{simpleDateFormat.format(createTime)});
        db.close();
    }

    private void loadData(String timeString) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLENAME_ITEM,
                null,
                "label_id=?",
                new String[]{timeString},
                null,
                null,
                "item_id");

        while (cursor.moveToNext()) {
            String content = cursor.getString(cursor.getColumnIndex("content"));
            short type = cursor.getShort(cursor.getColumnIndex("type"));
            String item_id = cursor.getString(cursor.getColumnIndex("item_id"));
            if (type == 0) {
                toCreateEditText(true, content, item_id);
            } else {
                toCreateImageView(true, content, item_id);
            }
        }
        cursor.close();
        db.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateLabelName();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //updateLabelName();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view.getClass() == RelativeLayout.class) {
                RelativeLayout parent = (RelativeLayout) view;
                for (int j = 0; j < parent.getChildCount(); j++) {
                    if (parent.getChildAt(j).getClass() == EditText.class) {
                        EditText editor = (EditText) parent.getChildAt(j);
                        values.put("content", editor.getText().toString());
                        db.update(TABLENAME_ITEM, values, "item_id=?", new String[]{mapOfEdit.get(editor)});
                    }
                }
            }
        }
        db.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addTextLabel(View view) {
        parentText = nameLabel.getText().toString();
        if (parentText.isEmpty()) {
            Toast.makeText(this, "先给标签取个名字", Toast.LENGTH_SHORT).show();
            return;
        }
        stateIsRemove = false;
        changeEditView(false);
        toCreateEditText(false, "", "");

        addItem("XXX", (short) 0);
    }

    public void toCreateEditText(boolean autoContent, String content, String timeString) {//创建文本便条
        EditText editText = new EditText(this);
        editText.setBackground(getResources().getDrawable(R.drawable.label_style));

        editText.setWidth(dm.widthPixels);
        editText.setTextColor(getResources().getColor(R.color.titleColor));

        editText.setPadding(30, 40, 60, 40);
        if (autoContent) {
            editText.setText(content);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(60, 16, 60, 16);//设置便条布局
        //editText.setLayoutParams(layoutParams);

        RelativeLayout parentView = new RelativeLayout(this);
        //parentView.setOrientation(LinearLayout.HORIZONTAL);
        parentView.setLayoutParams(layoutParams);
        parentView.addView(editText);

        linearLayout.addView(parentView);
        currentView = parentView;
        currentEditText = editText;

        if (autoContent) {
            map.put(currentView, timeString);
            mapOfEdit.put(currentEditText, timeString);
        }
        else {
            scrollToBottom();
        }
    }


    public void addItem(String content, short type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();//销毁时保存用户所有信息
        db.setForeignKeyConstraintsEnabled(true);
        ContentValues values = new ContentValues();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss_SSS");

        Cursor cursor = db.query("memo_label",
                new String[]{"_id"},
                "_id=?",
                new String[]{simpleDateFormat.format(createTime)},
                null,
                null,
                null);
        String parentId = "";
        if (cursor.moveToNext())
            parentId = cursor.getString(cursor.getColumnIndex("_id"));

        if (isCreator && parentId.isEmpty()) {
            parentId = simpleDateFormat.format(createTime);
            values.put("label", nameLabel.getText().toString());
            values.put("_id", parentId);
            db.insert(TABLENAME_LABEL, null, values);
        }
        values.clear();

        Date date = new Date(System.currentTimeMillis());
        String id = simpleDateFormat.format(date);
        values.put("label_id", parentId);
        values.put("content", content);
        values.put("type", type);
        values.put("item_id", id);
        db.insert(TABLENAME_ITEM, null, values);

        map.put(currentView, id);
        if (type == 0) {
            mapOfEdit.put(currentEditText, id);
            //currentEditText.setOnEditorActionListener();
        }
        cursor.close();
        db.close();
    }

    public void declineLabel(View view) {
        parentText = nameLabel.getText().toString();
        if (parentText.isEmpty()) {
            Toast.makeText(this, "先给标签取个名字", Toast.LENGTH_SHORT).show();
            return;
        }
        stateIsRemove = !stateIsRemove;
        changeEditView(stateIsRemove);
    }

    public void changeEditView(boolean isRemove) {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View child = linearLayout.getChildAt(i);
            if (child.getClass() == RelativeLayout.class) {
                if (isRemove) {
                    ImageButton button = new ImageButton(this);
                    button.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_circle_red_36dp));
                    button.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    button.setBackgroundColor(getResources().getColor(R.color.transparency));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RelativeLayout parent = (RelativeLayout) v.getParent();
                            String targetId = map.get(parent);
                            linearLayout.removeView(parent);
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.delete(TABLENAME_ITEM, "item_id=?", new String[]{targetId});
                            db.close();
                        }
                    });
                    ((RelativeLayout) child).addView(button);
                } else {
                    RelativeLayout parent = ((RelativeLayout) child);
                    for (int j = 0; j < parent.getChildCount(); j++) {
                        if (parent.getChildAt(j).getClass() == ImageButton.class) {
                            parent.removeViewAt(j);
                        }
                    }
                }
            }
        }
    }

    public void addPhotoLabel(View view) {
        String parentName = nameLabel.getText().toString();
        if (parentName.isEmpty()) {
            Toast.makeText(this, "先给标签取个名字", Toast.LENGTH_SHORT).show();
            return;
        }
        stateIsRemove = false;
        changeEditView(false);

        OpenPhotosAlum();
        toCreateImageView(false, "", "");
    }

    public void toCreateImageView(boolean autoPath, String filePath, String timeString) {//动态创建ImageView
        ImageView imageView = new ImageView(this);
        imageView.setBackground(getResources().getDrawable(R.drawable.label_style));
        //imageView.setMaxWidth(linearLayout.getWidth());
        imageView.setPadding(30, 40, 60, 40);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(60, 16, 60, 16);
        //imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER);

        HorizontalScrollView scrollView = new HorizontalScrollView(this);// ImageView的布局设置
        scrollView.addView(imageView);
        //scrollView.setLayoutParams(layoutParams);

        RelativeLayout parentView = new RelativeLayout(this);// 添加ImageView的布局到CreatorActivity布局中
        //parentView.setOrientation(LinearLayout.HORIZONTAL);
        parentView.setLayoutParams(layoutParams);
        parentView.addView(scrollView);

        linearLayout.addView(parentView);
        currentImageView = imageView;
        currentView = parentView;

        if (autoPath) {//编辑状态下根据数据库读取到的值直接加载图片
            imageFilePath = filePath;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            currentImageView.setImageBitmap(bitmap);
            map.put(currentView, timeString);
        }
        else {
            scrollToBottom();
        }
    }

    public void OpenPhotosAlum() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    101);
        } else {
            //打开相册
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//Intent.ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT"
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PHOTO); // 打开相册
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PICK_PHOTO:
                if (resultCode == RESULT_OK) { // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }

                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        // 根据图片路径显示图片
        displayImage(imagePath);
    }

    /**
     * android 4.4以前的处理方式
     *
     * @param data
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            imageFilePath = imagePath;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            currentImageView.setImageBitmap(bitmap);
            addItem(imageFilePath, (short) 1);
        } else {
            Toast.makeText(this, "获取相册图片失败", Toast.LENGTH_SHORT).show();
        }
    }
}
