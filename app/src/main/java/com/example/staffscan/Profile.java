package com.example.staffscan;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences preferences = getSharedPreferences("RetrieveID", Context.MODE_PRIVATE);
        int employeeID = preferences.getInt("Employee Id", 1);

        new FetchData().execute(employeeID);

    }

    private class FetchData extends AsyncTask<Integer, Void, List<String[]>> {
        @Override
        protected List<String[]> doInBackground(Integer... params) {

            int employeeID = params[0];

            OkHttpClient client = new OkHttpClient();
            List<String[]> data = new ArrayList<>();

            try {

                RequestBody id = new FormBody.Builder().add("employeeID", Integer.toString(employeeID)).build();

                // Make an HTTP request to the PHP script to fetch data
                Request request = new Request.Builder()
                        .url("https://staffscan.000webhostapp.com/test/profile.php") // Replace with the actual URL
                        .post(id) // POST request
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    JSONArray jsonArray = new JSONArray(responseData);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String dbname = jsonObject.getString("Name");
                        String email = jsonObject.getString("MobileNumber");
                        String mobileno = jsonObject.getString("EmailID");
                        String education = jsonObject.getString("education");
                        String position = jsonObject.getString("position");
                        String salary = jsonObject.getString("salary");
                        data.add(new String[]{dbname, email, mobileno, education, position, salary});

                    }
                }
            } catch (Exception e) {
                Log.d("Error in Profile php connection", Log.getStackTraceString(e));
            }

            return data;
        }

        @Override
        protected void onPostExecute(List<String[]> data) {

            String[] array = data.get(0);

            TextView name = findViewById(R.id.name);
            name.setText(array[0]);

            TextView mobileno = findViewById(R.id.monum);
            mobileno.setText("Mobile Number: " + array[1]);

            TextView email = findViewById(R.id.emailid);
            email.setText("Email ID: " + array[2]);

            TextView education = findViewById(R.id.education);
            education.setText("Education: " + array[3]);

            TextView position = findViewById(R.id.position);
            position.setText("Position: " + array[4]);

            TextView salary = findViewById(R.id.salary);
            salary.setText("Salary: " + array[5]);

        }
    }
}