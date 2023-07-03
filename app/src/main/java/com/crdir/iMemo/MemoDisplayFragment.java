package com.crdir.iMemo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class MemoDisplayFragment extends Fragment {

    private DBHelper dbHelper;
    private ListView listView;
    private List<MemoLabel> datas = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private Map<View, String> viewStringMap = new HashMap<>();
    private Map<View, Integer> viewIntegerMap = new HashMap<>();
    private String currentId;
    private View currentLabel;

    private MyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.memo_fragment, container, false);

        dbHelper = new DBHelper(getActivity());

        listView = (ListView) view.findViewById(R.id.memoList);//设置监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) getActivity()).openLabel(datas.get(position).getLabelTimeString(), datas.get(position).getLabelName());
            }//让MainActivity启动
        });
        readDatabase();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPreferences = getActivity().getSharedPreferences("usr_info", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isUpdateMemoFlag", false)) {
            reReadDatabase();
        }
    }

    public void reReadDatabase() {
        datas.clear();
        readDatabase();
    }

    private void readDatabase() {
        sharedPreferences = getActivity().getSharedPreferences("usr_info", MODE_PRIVATE);
        currentId = sharedPreferences.getString("currentTransactionId", "");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CreatorActivity.TABLENAME_LABEL,
                null,
                null,
                null,
                null,
                null,
                "_id");
        MemoLabel memoLabel = null;
        while (cursor.moveToNext()) {
            memoLabel = new MemoLabel();
            String name = cursor.getString(cursor.getColumnIndex("label"));
            String date = cursor.getString((cursor.getColumnIndex("_id")));
            memoLabel.setLabelName(name);
            memoLabel.setLabelTimeString(date);
            datas.add(memoLabel);
        }
        cursor.close();
        db.close();
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
    }

    private void showPopupMenu(final View view) {
        // View当前PopupMenu显示的相对View的位置
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        // menu布局
        popupMenu.getMenuInflater().inflate(R.menu.pop_menu, popupMenu.getMenu());
        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.transactionItemOfPopMenu:
                        sharedPreferences = getActivity().getSharedPreferences("usr_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("currentTransactionId", datas.get(viewIntegerMap.get(view)).getLabelTimeString());
                        editor.apply();

                        Calendar c = Calendar.getInstance();
                        Dialog dateDialog = new DatePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                Calendar time = Calendar.getInstance();
                                sharedPreferences = getActivity().getSharedPreferences("usr_info", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("targetYear", year);
                                editor.putInt("targetMonth", month + 1);
                                editor.putInt("targetDay", day);
                                editor.apply();
                                Dialog timeDialog = new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        sharedPreferences = getActivity().getSharedPreferences("usr_info", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putInt("targetHour", hourOfDay);
                                        editor.putInt("targetMinute", minute);
                                        editor.apply();
                                    }
                                }, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), true);
                                timeDialog.setTitle("请选择时间");
                                timeDialog.show();
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                        dateDialog.setTitle("请选择日期");
                        dateDialog.show();

                        Toast.makeText(getActivity(), "已将此项设置为待办事务", Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).updateTransaction();
                        TextView temporaryView = (TextView) listView.getChildAt(viewIntegerMap.get(view)).findViewById(R.id.labelNameOfList);
                        temporaryView.setTextColor(getResources().getColor(R.color.transactionColor));
                        if (currentLabel != temporaryView)
                            ((TextView) currentLabel).setTextColor(getResources().getColor(R.color.titleColor));
                        currentLabel = temporaryView;
                        break;
                    case R.id.shareItemOfPopMenu:
                        Intent share_intent = new Intent();
                        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
                        share_intent.setType("text/plain");//设置分享内容的类型
                        share_intent.putExtra(Intent.EXTRA_SUBJECT, "备忘录标签");//添加分享内容标题
                        share_intent.putExtra(Intent.EXTRA_TEXT, datas.get(viewIntegerMap.get(view)).getLabelName());//添加分享内容
                        //创建分享的Dialog
                        share_intent = Intent.createChooser(share_intent, "选择分享的方式");
                        startActivity(share_intent);
                        break;
                    case R.id.removeItemOfPopMenu:
                        removeLabel(view);
                        break;
                }
                return false;
            }
        });
        // PopupMenu关闭事件
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });

        popupMenu.show();
    }

    private void removeLabel(View view) {
        String timeString = viewStringMap.get(view);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.setForeignKeyConstraintsEnabled(true);
        db.delete(CreatorActivity.TABLENAME_LABEL,
                "_id=?",
                new String[]{timeString});
        db.close();
        reReadDatabase();
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.nameView = convertView.findViewById(R.id.labelNameOfList);
                holder.timeView = convertView.findViewById(R.id.labelDateTimeOfList);
                holder.imageView = convertView.findViewById(R.id.optionsOfList);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            viewStringMap.put(holder.imageView, datas.get(position).getLabelTimeString());
            viewIntegerMap.put(holder.imageView, position);

            holder.nameView.setText(datas.get(position).getLabelName());
            String newTimeString = datas.get(position).getLabelTimeString();

            //高亮当前事务
            if (newTimeString.equals(currentId)) {
                holder.nameView.setTextColor(getResources().getColor(R.color.transactionColor));
                currentLabel = holder.nameView;
            }

            //对原有时间字符串数据的处理
            newTimeString = newTimeString.substring(0, newTimeString.length() - 3);
            newTimeString = newTimeString.replace("_", " ");
            holder.timeView.setText(newTimeString);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(holder.imageView);
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView nameView;
            TextView timeView;
            ImageView imageView;
        }
    }
}
