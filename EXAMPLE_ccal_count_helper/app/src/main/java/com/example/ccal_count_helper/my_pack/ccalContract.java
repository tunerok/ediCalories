package com.example.ccal_count_helper.my_pack;
import android.provider.BaseColumns;
import java.util.Date;

public final class ccalContract {
    private ccalContract() {
    };

    public static final class GuestEntry implements BaseColumns {
        public final static String TABLE_NAME = "food_control";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_CCAL = "ccal";
        public static final String COLUMN_DATE = "date"; //2016-11-13
    }
}
