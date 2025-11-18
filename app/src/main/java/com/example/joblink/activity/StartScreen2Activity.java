package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.databinding.ActivityStartScreen2Binding;

public class StartScreen2Activity extends AppCompatActivity {

    ActivityStartScreen2Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityStartScreen2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen2Activity.this, StartScreen3Activity.class);
            startActivity(intent);
        });


    }
}