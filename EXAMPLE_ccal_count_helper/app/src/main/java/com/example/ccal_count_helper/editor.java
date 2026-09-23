package com.example.ccal_count_helper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import com.example.ccal_count_helper.my_pack.ccalDbHelper;
import com.example.ccal_count_helper.my_pack.ccalContract.GuestEntry;
import com.example.ccal_count_helper.my_pack.ccalContract;


public class editor extends AppCompatActivity {


    private EditText inp_cclal_edit;
    private EditText inp_date_edit;
    Button btn_oke;
    Button btn_cancele;
    Date currentTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");
        currentTime = Calendar.getInstance().getTime();
        setContentView(R.layout.activity_editor);

        inp_cclal_edit = (EditText) findViewById(R.id.inp_ccals);
        inp_date_edit = (EditText)findViewById(R.id.inp_date);
        btn_oke = (Button)findViewById(R.id.btn_ok);
        btn_cancele = (Button)findViewById(R.id.btn_cancel);

        inp_date_edit.setText(simpleDate.format(currentTime));


        View.OnClickListener betn_ok_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertCalls();
                // Закрываем активность
                finish();
            }
        };

        View.OnClickListener betn_cncl_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        inp_cclal_edit.requestFocus();
        btn_oke.setOnClickListener(betn_ok_listener);
        btn_cancele.setOnClickListener(betn_cncl_listener);



    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertCalls() {
        String ccalls = inp_cclal_edit.getText().toString().trim();
        String date = inp_date_edit.getText().toString().trim();

        ccalDbHelper calls_dbase_helper = new ccalDbHelper(this);
        SQLiteDatabase db = calls_dbase_helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GuestEntry.COLUMN_CCAL, ccalls);
        values.put(GuestEntry.COLUMN_DATE, date);

        long newRowId = db.insert(GuestEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            // Если ID  -1, значит произошла ошибка
            Toast.makeText(this, "Ошибка при записи!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Готово! Номер записи: " + newRowId, Toast.LENGTH_SHORT).show();
        }


    }
}
