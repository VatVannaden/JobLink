package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.joblink.R;
import com.example.joblink.databinding.ActivityStartScreen1Binding;

public class StartScreen1Activity extends AppCompatActivity {

    ActivityStartScreen1Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start_screen1);

        binding = ActivityStartScreen1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ConstraintLayout button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(StartScreen1Activity.this, StartScreen2Activity.class);
            startActivity(intent);
        });
    }
}