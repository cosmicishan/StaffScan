package com.example.staffscan;

// app/src/main/java/com/example/staffscandemo/Leave.java

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Leave extends AppCompatActivity {

    private TextView textViewStartTime;
    private TextView textViewEndTime;
    private EditText editTextReason;
    private Button btnPickStartDate;
    private Button btnPickEndDate;
    private Button btnSubmit;

    private Calendar startCalendar;
    private Calendar endCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave);

        // Initialize UI components
        textViewStartTime = findViewById(R.id.textViewStartTime);
        textViewEndTime = findViewById(R.id.textViewEndTime);
        editTextReason = findViewById(R.id.editTextReason);
        btnPickStartDate = findViewById(R.id.btnPickStartTime);
        btnPickEndDate = findViewById(R.id.btnPickEndTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        // Initialize Calendar objects
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // Set OnClickListener for the "Pick Start Date" button
        btnPickStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(true);
            }
        });

        // Set OnClickListener for the "Pick End Date" button
        btnPickEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(false);
            }
        });

        // Set OnClickListener for the "Submit" button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to submit leave request
                submitLeaveRequest();
            }
        });
    }

    // Method to show DatePickerDialog
    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        if (isStartDate) {
                            startCalendar = selectedDate;
                            updateDisplay(textViewStartTime, startCalendar);
                        } else {
                            endCalendar = selectedDate;
                            updateDisplay(textViewEndTime, endCalendar);
                        }
                    }
                },
                year,
                month,
                dayOfMonth
        );

        datePickerDialog.show();
    }

    // Method to update the display of selected date
    private void updateDisplay(TextView textView, Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        textView.setText(dateFormat.format(calendar.getTime()));
    }

    // Method to submit leave request to Firebase
    private void submitLeaveRequest() {
        // Retrieve reason from the EditText
        String reason = editTextReason.getText().toString();

        // Validate input
        if (startCalendar == null || endCalendar == null || reason.isEmpty()) {
            // Display a Toast for validation error
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format date using SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Get reference to the "leave_requests" node in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("leave_requests/1");

        // Create a map to represent the leave request
        Map<String, Object> leaveRequestMap = new HashMap<>();
        leaveRequestMap.put("admin_id", 3);
        leaveRequestMap.put("description", reason);
        leaveRequestMap.put("end_date", dateFormat.format(endCalendar.getTime()));
        leaveRequestMap.put("start_date", dateFormat.format(startCalendar.getTime()));
        leaveRequestMap.put("status", "approved");

        // Generate a unique key for the leave request
        String leaveRequestId = databaseReference.push().getKey();

        // Submit the leave request to Firebase
        if (leaveRequestId != null) {
            databaseReference.child(leaveRequestId).setValue(leaveRequestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error == null) {
                        // Leave request submitted successfully
                        // You can show a Toast or navigate to another activity as needed
                        Toast.makeText(Leave.this, "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle the error
                        // You can show a Toast or log the error as needed
                        Toast.makeText(Leave.this, "Error submitting leave request", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
