package com.example.ccal_count_helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.ccal_count_helper.my_pack.ccalContract;
import com.example.ccal_count_helper.my_pack.ccalDbHelper;

import com.example.ccal_count_helper.MyDialogFragment;

public class EditSingleData extends AppCompatActivity {


    //vars
    private String new_cal;
    private String new_data;
    private String this_id;

    //edits
    private EditText edit_cal;
    private EditText edit_date;


    //edits
    private Button btn_ok;
    private Button btn_cancel;
    private Button btn_delete;


    ccalDbHelper calls_dbase_helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_single_data);
        calls_dbase_helper = new ccalDbHelper(this);
        db = calls_dbase_helper.getWritableDatabase();

        edit_cal = (EditText) findViewById(R.id.edit_cal);
        edit_date = (EditText) findViewById(R.id.edit_date);

        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_delete = (Button)findViewById(R.id.btn_delete);




        this_id =  getIntent().getStringExtra("id");
        String temp_cal = get_d();
        //String temp_cal = getIntent().getStringExtra("id");

        String temp_data = getIntent().getStringExtra("date");

        edit_cal.setText(temp_cal);
        edit_date.setText(temp_data);

        View.OnClickListener betn_ok_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    new_cal = edit_cal.getText().toString().trim();
                    new_data = edit_date.getText().toString().trim();

                    updateCalls();
                    // Закрываем активность

                }
                finally {
                    finish();
                }
            }
        };

        View.OnClickListener betn_cncl_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };

        View.OnClickListener betn_delete_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_this();
                finish();
            }
        };

        edit_cal.requestFocus();
        btn_ok.setOnClickListener(betn_ok_listener);
        btn_cancel.setOnClickListener(betn_cncl_listener);
        btn_delete.setOnClickListener(betn_delete_listener);

    }


    void updateCalls(){
        ContentValues cal_values = new ContentValues();

        cal_values.put(ccalContract.GuestEntry.COLUMN_CCAL, new_cal);
        cal_values.put(ccalContract.GuestEntry.COLUMN_DATE, new_data);
        // Обновляем колонку DESCRIPTION с новым значением "Clever"
        db.update(ccalContract.GuestEntry.TABLE_NAME,
                cal_values,
                "_id = ?" ,
                new String[] {this_id});


    }


    String get_d(){
        String currentCcal = "";


        Cursor cursor = db.query(
                ccalContract.GuestEntry.TABLE_NAME,   // таблица
                null,            // столбцы
                null,                  // столбцы для условия WHERE
                null,                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);


        cursor.moveToPosition(Integer.parseInt(this_id));
        //String sql_string = "SELECT * FROM " + ccalContract.GuestEntry.TABLE_NAME + " WHERE _id = " + this_id;
        //Cursor cursor = db.rawQuery(sql_string, null);
        if (cursor.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = cursor.getColumnIndex(ccalContract.GuestEntry._ID);
            int CallIndex = cursor.getColumnIndex(ccalContract.GuestEntry.COLUMN_CCAL);
            int DateIndex = cursor.getColumnIndex(ccalContract.GuestEntry.COLUMN_DATE);

            do {

                if (cursor.getInt(idColIndex) == Integer.parseInt(this_id)) {
                    currentCcal = cursor.getString(CallIndex);
                    break;
                }

                // получаем значения по номерам столбцов и пишем все в лог
                Log.d("MY_LOG",
                        "ID = " + cursor.getInt(idColIndex) + ", ccals = "
                                + cursor.getString(CallIndex) + ", date = "
                                + cursor.getString(DateIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false -
                // выходим из цикла

            } while (cursor.moveToNext());
        }

        //int nameCcalIndex = cursor.getColumnIndex(ccalContract.GuestEntry.COLUMN_CCAL);
        //String currentCcal = cursor.getString(nameCcalIndex);
        return currentCcal;
    }

    void delete_this(){

        int delCount = db.delete( ccalContract.GuestEntry.TABLE_NAME, "_id =?",new String[] {this_id});
    }
}
