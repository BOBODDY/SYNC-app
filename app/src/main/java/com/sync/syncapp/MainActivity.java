package com.sync.syncapp;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Random;

//very first comment
public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar;
    LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        lineChart = (LineChart) findViewById(R.id.lineChart);
        lineChart.setVisibility(View.INVISIBLE);

        new GetDataTask().execute();
    }

    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetDataTask extends AsyncTask<Void, Void, LineData> {
        @Override
        protected LineData doInBackground(Void... voids) {
            Random rand = new Random(); //TODO: get real data
            ArrayList<Entry> values = new ArrayList<>();
            for(int i=0; i < 10; i++) {
                values.add(new Entry(rand.nextFloat() * 100, i));
            }

            LineDataSet lineDataSet = new LineDataSet(values, "Data");

            ArrayList<LineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet);

            ArrayList<String> xVals = new ArrayList<>();
            xVals.add("10pm");
            xVals.add("11pm");
            xVals.add("Midnight");
            xVals.add("1am");
            xVals.add("2am");
            xVals.add("3am");
            xVals.add("4am");
            xVals.add("5am");
            xVals.add("6am");
            xVals.add("7am");

            return new LineData(xVals, dataSets);
        }

        @Override
        protected void onPostExecute(LineData data) {
            super.onPostExecute(data);

            lineChart.setData(data);
            lineChart.invalidate();
            lineChart.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
