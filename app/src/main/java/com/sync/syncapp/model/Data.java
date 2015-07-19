package com.sync.syncapp.model;

import android.util.Log;

import com.sync.syncapp.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.lang.Comparable;

/**
 * Created by nick on 7/19/15.
 */
public class Data implements Comparable<Data>{
    
    private Date timestamp;
    private String value;
    
    public static int DATA_TYPE_CO2 = 10;
    public static int DATA_TYPE_TEMP = 20;
    public static int DATA_TYPE_LIGHT = 30;
    public static int DATA_TYPE_SLEEP = 40;
    private int dataType;

    public Data(String timestamp, String value, int type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            this.timestamp = sdf.parse(timestamp);
        } catch (ParseException pe) {
            Log.e(Constants.TAG, "error parsing date", pe);
            
        }
        this.value = value;
        
        this.dataType = type;
    }

    public int compareTo(Data otherData) {
        return timestamp.compareTo(otherData.getTimestamp());
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getValueString() {
        return value;
    }
    
    public float getValueFloat() {
        return Float.valueOf(value);
    }

    public int getDataType() {
        return dataType;
    }
}
