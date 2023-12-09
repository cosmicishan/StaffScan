package com.example.staffscan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.icu.text.UnicodeSetSpanner;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Context;
import android.provider.Settings;

import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings;


public class Camera extends AppCompatActivity {

    private final int CAM_REQ = 42;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private FusedLocationProviderClient fusedLocationClient;

    private Location currentLocation;
    private Location specificLocation;
    private static final double SPECIFIC_LATITUDE = 23.1558099;
    private static final double SPECIFIC_LONGITUDE = 72.663609;

    private FaceDetector faceDetector;

    Bitmap dbpicture, camerapicture;

    ImageView imgcamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        SharedPreferences preferences = getSharedPreferences("RetrieveID", Context.MODE_PRIVATE);
        int employeeID = preferences.getInt("Employee Id", 1);

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        faceDetector = FaceDetection.getClient(options);

        imgcamera = findViewById(R.id.imgcamera);
        Button punchin = findViewById(R.id.punchin);

        new DownloadImageTask().execute();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocationUpdates();

        punchin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Toast.makeText(Camera.this, "Verifying Location", Toast.LENGTH_SHORT).show();

                    try {
                        if (isLocationEnabled()) {

                            if (!currentLocation.isFromMockProvider()) {

                                float distance = currentLocation.distanceTo(specificLocation);

                                if (distance < 500) {

                                    String action = "punchin";
                                    String ID = Integer.toString(employeeID);

                                    Toast.makeText(Camera.this, "Retreiving the records.", Toast.LENGTH_SHORT).show();

                                    new RetrieveTimeTask().execute(ID, action);
                                } else {
                                    Toast.makeText(Camera.this, "You are far away. You must be around 500 meters from the location. \nYou are" + distance + "away.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(Camera.this, "Please turn off Mock Location", Toast.LENGTH_SHORT).show();
                                openDeveloperOptionsSettings();
                            }

                        } else {
                            enableLocationServices();
                        }
                    } catch (Exception e) {
                        Log.d("Error in Location service", e.getMessage());
                        requestLocationUpdates();
                    }
                }
        });

        Button punchout = findViewById(R.id.punchout);

        punchout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String action = "punchout";
                String ID = Integer.toString(employeeID);

                new RetrieveTimeTask().execute(ID, action);

            }
        });

    }

    private boolean areDeveloperOptionsEnabled() {
        int adbEnabled = Settings.Secure.getInt(
                getContentResolver(),
                Settings.Global.ADB_ENABLED,
                0
        );

        return adbEnabled > 0;
    }

    private void openDeveloperOptionsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        startActivity(intent);
    }

    public static boolean areMockLocationsEnabled(Context context) {
        // Check if mock locations are enabled in developer options
        if (Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("1")) {
            return false;
        }

        // Check if mock locations are being provided by an app
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("1");
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void enableLocationServices() {
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void requestLocationUpdates() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Log.d("Inside current location", "Retrieving current location");
                            Location location = task.getResult();
                            if (location != null) {
                                currentLocation = location;
                                Log.d("Current Location", Double.toString(currentLocation.getLatitude()) + " " + Double.toString(currentLocation.getLongitude()));
                                specificLocation = new Location("");
                                specificLocation.setLatitude(SPECIFIC_LATITUDE);
                                specificLocation.setLongitude(SPECIFIC_LONGITUDE);
                            }
                        }


                    });
        } else {
            // You may want to request location permissions here
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){

            if (requestCode == CAM_REQ){

                Bitmap img = (Bitmap)(data.getExtras().get("data"));

                detectface(img);

            }
        }

    }

    private void detectface(Bitmap imageBitmap){

        InputImage inputimage = InputImage.fromBitmap(imageBitmap, 0);

        faceDetector.process(inputimage).addOnSuccessListener(faces -> {
            drawFaceBoundingBox(imageBitmap, faces);
        })
                .addOnFailureListener(e -> {
            Log.d("Couldn't detect faces", "face error");
        });

    }

    private void drawFaceBoundingBox(Bitmap image, List<Face> faces){

            if (faces != null && !faces.isEmpty()) {

                Bitmap tempbitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);

                Canvas canva = new Canvas(tempbitmap);

                canva.drawBitmap(image, 0, 0, null);

                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);

                for (Face face : faces) {

                    Rect bounds = face.getBoundingBox();
                    canva.drawRect(bounds, paint);

                }

                imgcamera.setImageBitmap(tempbitmap);

                int length = faces.size();

                if (length > 1){
                    Toast.makeText(this, "Multiple faces is detected", Toast.LENGTH_SHORT).show();
                }

                else {

                    SharedPreferences preferences = getSharedPreferences("RetrieveID", Context.MODE_PRIVATE);
                    int employeeID = preferences.getInt("Employee Id", 1);

                    String action_php = "insert";
                    String ID = Integer.toString(employeeID);
                    new UpdateTime().execute(ID, action_php);

                }


            } else {

                Toast.makeText(this, "NO face Detected", Toast.LENGTH_SHORT).show();

            }

    }

    public float calculateDistance(float[] embedding1, float[] embedding2) {
        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same length");
        }

        float sum = 0.0f;
        for (int i = 0; i < embedding1.length; i++) {
            float diff = embedding1[i] - embedding2[i];
            sum += diff * diff;
        }

        return (float) Math.sqrt(sum);
    }


    private class UpdateTime extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String employeeID = params[0];
            String action_php = params[1];

            String late = "false";

            OkHttpClient client = new OkHttpClient();

            Date currentDate = new Date();

            // Define a date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Format the current date as a string
            String formattedDate = dateFormat.format(currentDate);

            try {
                Date specificTime = dateFormat.parse("2023-11-04 12:00:00");

                if (currentDate.after(specificTime)) {
                    late = "true";
                } else {
                    late = "false";
                }
            } catch (ParseException e) {
                Log.d("Parsing error", Log.getStackTraceString(e));
            }


            try {

                RequestBody id = new FormBody.Builder().add("employee_id", employeeID).add("action", action_php).add("time", formattedDate).add("late", late).build();

                Log.d("Connecting", "Starting the connection");

                // Make an HTTP request to the PHP script to fetch data
                Request request = new Request.Builder()
                        .url("https://staffscan.000webhostapp.com/test/punch.php")
                        .post(id) // POST request
                        .build();

                Log.d("after connection", "done");

                Response response = client.newCall(request).execute();

                Log.d("response", response.toString());

                if (response.isSuccessful()) {

                    if (action_php.equals("insert")){
                        return "insert";
                    }

                    if (action_php.equals("update")){
                        return "update";
                    }

                    return response.body().string();
                }
                else {

                    return "HTTP Error in Updating Time: " + response.code();
                }

            }
            catch (Exception e) {
                return "Error in Insertion: " + e.toString();
            }

        }


        @Override
        protected void onPostExecute(String data) {

            if (data == "insert"){
                Toast.makeText(Camera.this, "Your attendance has been taken.", Toast.LENGTH_SHORT).show();
            }

            if (data == "update"){
                Toast.makeText(Camera.this, "Your leaving time has been recorded.", Toast.LENGTH_SHORT).show();
            }

            Log.d("Camera Activity Info: " , data);

        }
    }

    private class RetrieveTimeTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String employeeId = params[0];
            String action = params[1];

            OkHttpClient client = new OkHttpClient();

            String url = "https://staffscan.000webhostapp.com/test/punch_check.php";

            RequestBody id2 = new FormBody.Builder().add("employee_id", employeeId).add("action", action).build();

            Request request = new Request.Builder().url(url).post(id2).build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string() + "|" + action;
                } else {
                    return "Error: " + response.code() + "|" + action;
                }
            } catch (Exception e) {
                return "Error: " + e.getMessage() + "|" + action;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(new Date());

            String[] parts = result.split("\\|");
            String action = parts[1];

            if (action.equals("punchin")) {


                if (result.contains(currentTime.substring(0, 10))) {
                    Toast.makeText(Camera.this, "You have already punched in for today.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent icamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(icamera, CAM_REQ);
                }
            } else if (action.equals("punchout")) {

                if (result.contains(currentTime.substring(0, 10))) {
                    Toast.makeText(Camera.this, "You have already punched out for today.", Toast.LENGTH_SHORT).show();
                } else {
                    String action_php = "update";

                    SharedPreferences preferences = getSharedPreferences("RetrieveID", Context.MODE_PRIVATE);
                    int employeeID = preferences.getInt("Employee Id", 1);
                    String ID = Integer.toString(employeeID);

                    new UpdateTime().execute(ID, action_php);
                }

            }

            Log.d("Update in retrieving data", result);
        }

    }


    private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... voids) {

            Log.d("Inside the download image task function", "We enetered");

            SharedPreferences preferences = getSharedPreferences("RetrieveID", Context.MODE_PRIVATE);
            int employeeID = preferences.getInt("Employee Id", 1);
            String ID = Integer.toString(employeeID);

            String url = "https://staffscan.000webhostapp.com/test/profile_picture.php?id=" + employeeID;

            try {

                OkHttpClient client = new OkHttpClient();


                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                         Bitmap faceimage = BitmapFactory.decodeStream(response.body().byteStream());
                         return faceimage;
                }
                else {
                    Log.d("Error in php of faceimage", String.valueOf(response.code()));
                    return null;
                }

            }
            catch (Exception e) {

                Log.d("Error in processing image", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            if (result != null) {
                dbpicture = result;
            } else {
                Log.d("Picture not retrieved", "Camera image not retrieved");
            }
        }
    }


}