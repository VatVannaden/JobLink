package com.example.joblink.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityCompleteProfileBinding;
import com.example.joblink.model.User;
import com.example.joblink.viewmodel.CompleteProfileViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CompleteProfileActivity extends AppCompatActivity {

    private ActivityCompleteProfileBinding binding;
    private CompleteProfileViewModel viewModel;
    private Uri imageUri;
    private String selectedDobString = "";

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            if (imageUri != null) {
                                binding.addProfileImg.setImageURI(imageUri);
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && imageUri != null) {
                            binding.addProfileImg.setImageURI(imageUri);
                        }
                    });

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    showImagePickerDialog();
                } else {
                    Toast.makeText(this, "Permissions are required to use this feature.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompleteProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CompleteProfileViewModel.class);

        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        setupPhoneNumberInput();
        setupImagePicker();
        setupSaveButton();
        setupDatePickers();
        setupLocationPicker();
        setupProfessionPicker();
    }

    private void observeViewModel() {
        viewModel.getStatus().observe(this, status -> {
            switch (status) {
                case LOADING:
                    setLoadingState(true, null);
                    break;
                case SUCCESS:
                    Runnable successAction = () -> {
                        Toast.makeText(CompleteProfileActivity.this, "Profile completed successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    };
                    setLoadingState(false, successAction);
                    break;
                case ERROR:
                    Runnable errorAction = () -> {
                        String error = viewModel.getErrorMessage().getValue();
                        showError(error != null ? error : "An unknown error occurred");
                    };
                    setLoadingState(false, errorAction);
                    break;
            }
        });
    }

    private void setupPhoneNumberInput() {
        binding.editTextPhoneNumber.setText("0");
        binding.editTextPhoneNumber.setSelection(binding.editTextPhoneNumber.getText().length());
        binding.editTextPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().startsWith("0")) {
                    binding.editTextPhoneNumber.setText("0");
                    binding.editTextPhoneNumber.setSelection(1);
                }
            }
        });
    }

    private void setupImagePicker() {
        binding.addProfileImg.setOnClickListener(v -> checkAndRequestPermissions());
    }

    private void setupSaveButton() {
        binding.confirmButton.setOnClickListener(v -> validateAndSaveProfile());
    }

    private void setupDatePickers() {
        binding.dateOfBirth.setOnClickListener(v -> showDatePickerDialog());
    }

    private void setupLocationPicker() {
        binding.location.setOnClickListener(v -> showLocationDialog());
    }

    private void setupProfessionPicker() {
        binding.editTextProfession.setOnClickListener(v -> showProfessionDialog());
    }

    private void checkAndRequestPermissions() {
        String imagePermission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        String[] permissionsToRequest = {Manifest.permission.CAMERA, imagePermission};

        boolean allPermissionsGranted = true;
        for (String permission : permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            showImagePickerDialog();
        } else {
            requestPermissionsLauncher.launch(permissionsToRequest);
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image From")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Profile Picture");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -18);

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDobString = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);

            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            binding.dateOfBirth.setText(sdf.format(selectedDate.getTime()));
        };

        new DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showLocationDialog() {
        String[] locations = getResources().getStringArray(R.array.provinces);
        new AlertDialog.Builder(this)
                .setTitle("Select Province")
                .setItems(locations, (dialog, which) -> {
                    binding.location.setText(locations[which]);
                })
                .show();
    }

    private void showProfessionDialog() {
        String[] professions = getResources().getStringArray(R.array.professions);
        new AlertDialog.Builder(this)
                .setTitle("Select Your Profession")
                .setItems(professions, (dialog, which) -> {
                    if (which == professions.length - 1) {
                        showCustomProfessionDialog();
                    } else {
                        String selectedProfession = professions[which];
                        binding.editTextProfession.setText(selectedProfession);
                    }
                })
                .show();
    }

    private void showCustomProfessionDialog() {
        final EditText input = new EditText(this);
        input.setHint("e.g., Marketing Manager");

        new AlertDialog.Builder(this)
                .setTitle("Enter Your Profession")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String customProfession = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(customProfession)) {
                        binding.editTextProfession.setText(customProfession);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void validateAndSaveProfile() {
        if (!isInputValid()) return;

        User user = new User();
        user.setPhone(binding.editTextPhoneNumber.getText().toString().trim());
        user.setGender(binding.radioMale.isChecked() ? "male" : "female");
        user.setLocation(binding.location.getText().toString().trim());
        user.setProfession(binding.editTextProfession.getText().toString().trim());
        user.setDob(selectedDobString);
        user.setSetupComplete(true);

        viewModel.saveUserProfile(imageUri, user);
    }

    private boolean isInputValid() {
        if (imageUri == null) {
            showError("Please select a profile image");
            return false;
        }

        String phone = binding.editTextPhoneNumber.getText().toString().trim();
        if (phone.length() < 9 || phone.length() > 10) {
            showError("Please enter a valid phone number (9-10 digits)");
            return false;
        }

        if (binding.radioGroupGender.getCheckedRadioButtonId() == -1) {
            showError("Please select a gender");
            return false;
        }

        String location = binding.location.getText().toString().trim();
        if (location.isEmpty() || location.equalsIgnoreCase("Province")) {
            showError("Please select your location");
            return false;
        }

        if (TextUtils.isEmpty(selectedDobString)) {
            showError("Please select your date of birth");
            return false;
        }

        String profession = binding.editTextProfession.getText().toString().trim();
        if (profession.isEmpty() || profession.equalsIgnoreCase("Select your profession")) {
            showError("Please enter or select your profession");
            return false;
        }
        return true;
    }

    private void setLoadingState(boolean isLoading, Runnable onFinished) {
        float targetAlpha = isLoading ? 1.0f : 0.0f;

        if (isLoading) {
            binding.loadingOverlay.setVisibility(View.VISIBLE);
            binding.loadingOverlay.setAlpha(0.0f);
            binding.loadingOverlay.animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .start();
        }
        binding.loadingOverlay.animate()
                .alpha(targetAlpha)
                .setDuration(500)
                .setStartDelay(isLoading ? 0 : 1500)
                .withEndAction(() -> {
                    if (!isLoading) {
                        binding.loadingOverlay.setVisibility(View.GONE);
                        if (onFinished != null) onFinished.run();
                    }
                });
    }

    private void showError(String message) {
        if (isFinishing() || isDestroyed() || message == null) return;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}