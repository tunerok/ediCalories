package com.example.ccal_count_helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ccal_count_helper.my_pack.ccalDbHelper;
import com.example.ccal_count_helper.my_pack.ccalContract.GuestEntry;
import com.example.ccal_count_helper.my_pack.ccalContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.ccal_count_helper.SettingsActivity.APP_PREFERENCES_COUNTER;

public class MainActivity extends AppCompatActivity {




    private ccalDbHelper mDbHelper;
    private int remaining_today;
    private int to_day;
    private Date currentTime;
    private String str_currentTime;
    private String current_date;
    private SharedPreferences mSettings;
    public static final String APP_PREFERENCES_COUNTER = "counter";
    public static final String APP_PREFERENCES = "mysettings";



    //boolean flag to know if main FAB is in open or closed state.
    private boolean fabExpanded = false;
    private FloatingActionButton fabSettings;

    private FloatingActionButton fab50;
    private FloatingActionButton fab100;
    private FloatingActionButton fab250;
    private FloatingActionButton fab500;
    private FloatingActionButton fabCustom;

    //Linear layout holding the submenu
    private LinearLayout layoutFab50;
    private LinearLayout layoutFab100;
    private LinearLayout layoutFab250;
    private LinearLayout layoutFab500;
    private LinearLayout layoutFabCustom;
    private ScrollView layoutScroller;


    private ArrayList<Button> arr_btn_edit;

    View.OnClickListener menu_items_listener;
    private boolean is_starting;
    //table

    private TableLayout layoutTable;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        arr_btn_edit = new ArrayList<Button>();
        fabSettings = (FloatingActionButton) this.findViewById(R.id.fab);
        fab50 = (FloatingActionButton) this.findViewById(R.id.fab50);
        fab100 = (FloatingActionButton) this.findViewById(R.id.fab100);
        fab250 = (FloatingActionButton) this.findViewById(R.id.fab250);
        fab500 = (FloatingActionButton) this.findViewById(R.id.fab500);
        fabCustom = (FloatingActionButton) this.findViewById(R.id.fabCustom);


        layoutScroller = (ScrollView)  this.findViewById(R.id.lay_scroller);
        layoutFab50 = (LinearLayout) this.findViewById(R.id.layoutFab50);
        layoutFab100 = (LinearLayout) this.findViewById(R.id.layoutFab100);
        layoutFab250 = (LinearLayout) this.findViewById(R.id.layoutFab250);
        layoutFab500 = (LinearLayout) this.findViewById(R.id.layoutFab500);
        layoutFabCustom = (LinearLayout) this.findViewById(R.id.layoutFabCustom);

        layoutTable = (TableLayout) this.findViewById(R.id.main_table);

        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");
        currentTime = Calendar.getInstance().getTime();
        str_currentTime = simpleDate.format(currentTime);
        current_date = str_currentTime;
        //listeners on fabs
        fab50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_to_calls("50");
                displayDatabaseInfo();
            }
        });

        fab100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_to_calls("100");
                displayDatabaseInfo();
            }
        });

        fab250.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_to_calls("250");
                displayDatabaseInfo();
            }
        });

        fab500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_to_calls("500");
                displayDatabaseInfo();
            }
        });

        fabCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSubMenusFab();
                Intent intent = new Intent(MainActivity.this, editor.class);
                startActivity(intent);
            }
        });

        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded == true){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });
        closeSubMenusFab();


        menu_items_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, EditSingleData.class);

                intent.putExtra("id", String.valueOf(v.getId()));
                intent.putExtra("date", current_date);

                startActivity(intent);

            }
        };


        mDbHelper = new ccalDbHelper(this);
        //displayDatabaseInfo();
        layoutScroller.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        is_starting = false;
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            String temp_inp = mSettings.getString(APP_PREFERENCES_COUNTER, "1200");
            // Выводим на экран данные из настроек
            if (temp_inp != null) {
                to_day = Integer.parseInt(temp_inp);
            }
            else {
                to_day = 1800;
            }
        }


        displayDatabaseInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void clear_table(){

        if (!arr_btn_edit.isEmpty()) {
            arr_btn_edit.clear();
        }
        layoutTable.removeAllViews();
    }



    private void append_to_table(String appended_row, String appended_ccal, String appended_data, boolean is_data){
        TableRow temp_tableRow = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT , TableRow.LayoutParams.MATCH_PARENT );
        params.weight = 1;
        temp_tableRow.setLayoutParams(params);
        TextView temp_text_box_appended_ccal = new TextView(this);
        TextView temp_text_box_appended_data = new TextView(this);
        //TextView temp_text_box_appended_row = new TextView(this); // temp_text_box_appended_row показывает номер записи - или ID по БД







        temp_text_box_appended_ccal.setText(appended_ccal);
        temp_text_box_appended_data.setText(appended_data);
        //temp_text_box_appended_row.setText(String.valueOf(appended_row));

        temp_text_box_appended_ccal.setLayoutParams(params);
        temp_text_box_appended_data.setLayoutParams(params);
        //temp_text_box_appended_row.setLayoutParams(params);

        //temp_text_box_appended_row.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        temp_text_box_appended_ccal.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
        temp_text_box_appended_data.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);



        //temp_tableRow.addView(temp_text_box_appended_row);
        temp_tableRow.addView(temp_text_box_appended_ccal);
        temp_tableRow.addView(temp_text_box_appended_data);

        if (is_data) {
            temp_tableRow.setId(Integer.parseInt(appended_row));
            temp_tableRow.setClickable(true);
            temp_tableRow.setOnClickListener(menu_items_listener);
        }


      //this code works - adds some btns to table
        /*if (is_data) {
            Button Selected = new Button(this);
            Selected.setText("Edit " + String.valueOf(appended_row));
            Selected.setId(Integer.parseInt(appended_row));
            Selected.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);

            Selected.setOnClickListener(menu_items_listener);

            arr_btn_edit.add(Selected);
            temp_tableRow.addView(arr_btn_edit.get(arr_btn_edit.indexOf(Selected)));
        }

*/
        layoutTable.addView(temp_tableRow);

    }







    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayDatabaseInfo() {

        if (is_starting) {
        } else {
            remaining_today = to_day;
            clear_table();
            //is_starting = true;
        }

        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Зададим условие для выборки - список столбцов
        String[] projection = {
                GuestEntry._ID,
                GuestEntry.COLUMN_CCAL,
                GuestEntry.COLUMN_DATE};



        // Делаем запрос
        Cursor cursor = db.query(
                GuestEntry.TABLE_NAME,   // таблица
                projection,            // столбцы
                "date = ?",                  // столбцы для условия WHERE
                new String[] {str_currentTime},                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order

        TextView displayTextView = (TextView) findViewById(R.id.text_view_info);
        displayTextView.setText("На каждый день " + to_day + " калорий.\n\n");
        try {
            displayTextView.append("Таблица содержит " + cursor.getCount() + " записей.\n\n");
            append_to_table("Номер", "Калории", "Дата приема пищи", false);

            //GuestEntry.COLUMN_CCAL + " - " +
               //     GuestEntry.COLUMN_DATE + "\n")

            // Узнаем индекс каждого столбца
            int idColumnIndex = cursor.getColumnIndex(GuestEntry._ID);
            int nameCcalIndex = cursor.getColumnIndex(GuestEntry.COLUMN_CCAL);
            int cityDateIndex = cursor.getColumnIndex(GuestEntry.COLUMN_DATE);


            // Проходим через все ряды
            while (cursor.moveToNext()) {
                // Используем индекс для получения строки или числа
                int currentID = cursor.getInt(idColumnIndex);
                String currentCcal = cursor.getString(nameCcalIndex);
                String currentDate = cursor.getString(cityDateIndex);

                if (str_currentTime.compareTo(currentDate) == 0)
                    remaining_today = remaining_today - Integer.parseInt(currentCcal);

                append_to_table(String.valueOf(currentID),currentCcal,currentDate, true);
                // Выводим значения каждого столбца
               // displayTextView.append(("\n" + currentID + " - " +
                      //  currentCcal + " - " +
                       // currentDate ));
            }
        } finally {
            // Всегда закрываем курсор после чтения
            displayTextView.append("\nОсталось калорий: " + String.valueOf(remaining_today)+ "\n");
            cursor.close();
            append_to_table("","","", false);
            append_to_table("","","", false);
            layoutScroller.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    //add custom calls to DB
    private void add_to_calls(String ccals){


        ccalDbHelper calls_dbase_helper = new ccalDbHelper(this);
        SQLiteDatabase db = calls_dbase_helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GuestEntry.COLUMN_CCAL, ccals);
        values.put(GuestEntry.COLUMN_DATE, str_currentTime);

        long newRowId = db.insert(GuestEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            // Если ID  -1, значит произошла ошибка
            Toast.makeText(this, "Ошибка при записи!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Готово! Калорий за прием: " + ccals, Toast.LENGTH_SHORT).show();
        }
    }

    //show and close fabs
    private void closeSubMenusFab(){
        layoutFab50.setVisibility(View.INVISIBLE);
        layoutFab100.setVisibility(View.INVISIBLE);
        layoutFab250.setVisibility(View.INVISIBLE);
        layoutFab500.setVisibility(View.INVISIBLE);
        layoutFabCustom .setVisibility(View.INVISIBLE);
        fabSettings.setImageResource(R.drawable.ic_create_black_24dp);
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        layoutFab50.setVisibility(View.VISIBLE);
        layoutFab100.setVisibility(View.VISIBLE);
        layoutFab250.setVisibility(View.VISIBLE);
        layoutFab500.setVisibility(View.VISIBLE);
        layoutFabCustom .setVisibility(View.VISIBLE);
        //Change settings icon to 'X' icon
        fabSettings.setImageResource(R.drawable.ic_close_black_24dp);
        fabExpanded = true;
    }



}

