package com.example.joblink.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.databinding.ActivityStartScreen1Binding;

public class StartScreen1Activity extends AppCompatActivity {

    ActivityStartScreen1Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityStartScreen1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(view -> {
            Intent intent = new Intent(StartScreen1Activity.this, StartScreen2Activity.class);
            startActivity(intent);
        });


    }
}