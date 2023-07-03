package com.crdir.iMemo;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class UsrInfoDiaplayFragment extends Fragment {

    private DBHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private TextView currentLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.usr_fragment, container, false);
        dbHelper = new DBHelper(getActivity());
        currentLabel = view.findViewById(R.id.CurrentLabel);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setTransaction();
    }

    public void setTransaction(){
        sharedPreferences = getActivity().getSharedPreferences("usr_info", MODE_PRIVATE);
        String label_id = sharedPreferences.getString("currentTransactionId", "");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String transaction;
        Cursor cursor = db.query(CreatorActivity.TABLENAME_LABEL,
                new String[]{"label"},
                "_id=?",
                new String[]{label_id},
                null,
                null,
                null);

        if (cursor.moveToNext()) {
            transaction = cursor.getString(cursor.getColumnIndex("label"));
            currentLabel.setText(transaction);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("TransactionContent", transaction);
            editor.apply();
        }
        else{
            currentLabel.setText(getString(R.string.currentLabel));
        }
        db.close();
    }
}
