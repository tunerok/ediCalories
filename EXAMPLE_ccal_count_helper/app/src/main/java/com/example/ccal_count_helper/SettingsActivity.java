package com.example.ccal_count_helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    private final static String FILE_NAME = "settings.txt";
    private EditText tv_call_per_day;
    private Button btn_oke;
    private Button btn_cancele;

    private SharedPreferences mSettings;
    public static final String APP_PREFERENCES_COUNTER = "counter";
    public static final String APP_PREFERENCES = "mysettings";
    private String temp_inp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            temp_inp = mSettings.getString(APP_PREFERENCES_COUNTER, "1200");

        }
        setContentView(R.layout.activity_settings);
        tv_call_per_day = (EditText) findViewById(R.id.edit_call_pd);
        btn_oke = (Button) findViewById(R.id.btn_ok);
        btn_cancele = (Button) findViewById(R.id.btn_cancel);

        tv_call_per_day.setText(temp_inp);

        View.OnClickListener betn_ok_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp_inp = tv_call_per_day.getText().toString().trim();
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_COUNTER, temp_inp);
                editor.apply();
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

        btn_oke.setOnClickListener(betn_ok_listener);
        btn_cancele.setOnClickListener(betn_cncl_listener);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            temp_inp = mSettings.getString(APP_PREFERENCES_COUNTER, "1200");
            // Выводим на экран данные из настроек

        }


    }


    public void saveText() {
        FileOutputStream fos = null;
        try {

            String text = tv_call_per_day.getText().toString();
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);

            fos.write(text.getBytes());
            Toast.makeText(this, "Data saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {

            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException ex) {

                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openText(){

        FileInputStream fin = null;

        try {
            fin = openFileInput(FILE_NAME);
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            String text = new String (bytes);
            tv_call_per_day.setText(text);
        }
        catch(IOException ex) {

            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{

            try{
                if(fin!=null)
                    fin.close();
            }
            catch(IOException ex){

                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



}
