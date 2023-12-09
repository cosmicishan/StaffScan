package com.example.staffscan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Tasks extends AppCompatActivity {

    private static DatabaseReference mDatabase;
    private ArrayList<Task> taskList;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (ContextCompat.checkSelfPermission(Tasks.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Tasks.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101);

            }
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        taskList = new ArrayList<>();

        ListView taskListView = findViewById(R.id.taskListView);

        adapter = new TaskAdapter(this, taskList);
        taskListView.setAdapter(adapter);

        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        mDatabase.child("tasks").child("1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList.clear();

                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> taskData = (Map<String, Object>) taskSnapshot.getValue();

                    String taskId = taskSnapshot.getKey();
                    String taskDescription = (String) taskData.get("task_description");
                    String taskHeader = (String) taskData.get("task_header");
                    String deadline = (String) taskData.get("deadline");
                    String status = (String) taskData.get("status");

                    Log.d("Firebase Data",status);

                    // Check if the task is pending and the deadline is still valid
                    if ("pending".equals(status) && isDeadlineValid(deadline)) {
                        Task task = new Task(taskId, taskDescription, taskHeader, deadline);
                        taskList.add(task);
                        makeNotification();
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d("Adapter", "Adapter notified, item count: " + adapter.getCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Database Error", databaseError.toString());
            }
        });
    }

    private boolean isDeadlineValid(String deadline) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date deadlineDate = dateFormat.parse(deadline);
            Date currentDate = new Date();

            // Compare deadline with today's date
            return currentDate.before(deadlineDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void makeNotification(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "123");
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Task Assigned")
                .setContentText("Your task data has been changed. Check it out.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), Tasks.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);

        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

            NotificationChannel notificationChannel = notificationManager.getNotificationChannel("123");

            if (notificationChannel == null){
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel("123", "Some Description", importance);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

        }

        notificationManager.notify(0, builder.build());

    }



    private static class TaskAdapter extends ArrayAdapter<Task> {

        public TaskAdapter(@NonNull AppCompatActivity context, ArrayList<Task> tasks) {
            super(context, 0, tasks);
        }

        @NonNull
        @Override
        public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
            Log.d("Adapter", "getView called for position: " + position);
            Task task = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_item, parent, false);
            }

            TextView taskInfoTextView = convertView.findViewById(R.id.taskInfoTextView);
            Button markAsCompleteButton = convertView.findViewById(R.id.markAsCompleteButton);

            if (task != null) {
                String taskInfo = "Task ID: " + task.getId() +
                        "\nDescription: " + task.getDescription() +
                        "\nHeader: " + task.getHeader() +
                        "\nDeadline: " + task.getDeadline();

                taskInfoTextView.setText(taskInfo);

                markAsCompleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Handle "Mark as Complete" button click
                        markTaskAsComplete(task.getId());
                    }
                });
            }

            return convertView;
        }

        private void markTaskAsComplete(String taskId) {
            // Update the status to "completed" in Firebase
            mDatabase.child("tasks").child("1").child(taskId).child("status").setValue("completed");
        }


    }

    private static class Task {
        private String id;
        private String description;
        private String header;
        private String deadline;

        public Task(String id, String description, String header, String deadline) {
            this.id = id;
            this.description = description;
            this.header = header;
            this.deadline = deadline;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getHeader() {
            return header;
        }

        public String getDeadline() {
            return deadline;
        }
    }
}