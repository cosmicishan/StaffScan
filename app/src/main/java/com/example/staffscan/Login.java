package com.example.staffscan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {
    private Button register, logginbutton;
    private EditText edemail, edpassword;
    private OkHttpClient client = new  OkHttpClient();

    private final static String BASE_URL = "https://staffscan.000webhostapp.com/test/connection.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edemail = findViewById(R.id.email);
        edpassword = findViewById(R.id.password);

        logginbutton = findViewById(R.id.loginbutton);

        logginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = edemail.getText().toString();
                String password = edpassword.getText().toString();
                new LoginTask().execute(email, password);
            }

        });
    }
        private class LoginTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                try {
                    String email = params[0];
                    String password = params[1];

                    RequestBody formdata = new FormBody.Builder().add("email", email).add("password", password).build();

                    // Create the POST request
                    Request request = new Request.Builder()
                            .url(BASE_URL) // Replace with your server URL
                            .post(formdata)
                            .build();

                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        return response.body().string();
                    } else {

                        return "HTTP Error: " + response.code();
                    }

                } catch (Exception e) {

                    Log.d("Error in Connection", Log.getStackTraceString(e));
                    return "Network Error";

                }
            }

            @Override
            protected void onPostExecute(String result) {

                try {

                    JSONObject jobj = new JSONObject(result);
                    int success = jobj.getInt("success");
                    int employeeID = jobj.getInt("employeeID");

                    SharedPreferences preferences = getSharedPreferences("RetrieveID", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("Employee Id", employeeID);
                    editor.apply();

                    if (success == 1) {
                        startActivity(new Intent(Login.this, MainActivity.class));
                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    }
                    else if (success == 0){

                        Toast.makeText(Login.this, "Please enter correct credentials", Toast.LENGTH_SHORT).show();

                    }
                    else {

                        Log.d("Some unknown error", result.toString());
                        Toast.makeText(Login.this, "Some unknown error", Toast.LENGTH_SHORT).show();

                    }
                }
                catch (JSONException e) {
                    Toast.makeText(Login.this, "Enter correct Credentials", Toast.LENGTH_SHORT).show();
                }
            }
    }
}


