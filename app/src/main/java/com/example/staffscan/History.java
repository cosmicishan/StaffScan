package com.example.staffscan;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class History extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        SharedPreferences preferences = getSharedPreferences("RetrieveID", Context.MODE_PRIVATE);
        int employeeID = preferences.getInt("Employee Id", 1);

        new FetchDataTask().execute(employeeID);
    }

    private class FetchDataTask extends AsyncTask<Integer, Void, List<String[]>> {
        @Override
        protected List<String[]> doInBackground(Integer... params) {

            int employeeID = params[0];

            OkHttpClient client = new OkHttpClient();
            List<String[]> data = new ArrayList<>();

            try {

                RequestBody id = new FormBody.Builder().add("employeeID", Integer.toString(employeeID)).build();

                // Make an HTTP request to the PHP script to fetch data
                Request request = new Request.Builder()
                        .url("https://staffscan.000webhostapp.com/test/attendance.php") // Replace with the actual URL
                        .post(id) // POST request
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    JSONArray jsonArray = new JSONArray(responseData);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String startTime = jsonObject.getString("Start_Time");
                        String endTime = jsonObject.getString("End_Time");


                        data.add(new String[]{startTime, endTime});
                    }
                }
            } catch (Exception e) {
                Log.d("Error in connection", Log.getStackTraceString(e));
            }

            return data;
        }

        @Override
        protected void onPostExecute(List<String[]> data) {

            TableLayout tableLayout = findViewById(R.id.tableLayout);

            // Process and display the retrieved data
            for (String[] row : data) {

                TableRow tableRow = new TableRow(History.this);
                tableRow.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                ));

                String date = row[0].substring(0,10);
                String starttime = row[0].substring(10);
                String endtime = row[1].substring(10);

                String[] names = {date, starttime, endtime};

                for (String name : names) {

                    TextView textView = new TextView(History.this);
                    textView.setText(name);
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(16, 16, 16, 16);
                    textView.setTextSize(20);
                    textView.setTextColor(Color.parseColor("#a7bbcb"));

                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    params.weight = 1;
                    textView.setLayoutParams(params);

                    tableRow.addView(textView);
                }

                tableLayout.addView(tableRow);
            }
        }
    }
}