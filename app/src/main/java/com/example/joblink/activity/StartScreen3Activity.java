package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.databinding.ActivityStartScreen3Binding;

public class StartScreen3Activity extends AppCompatActivity {

    ActivityStartScreen3Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityStartScreen3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen3Activity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
