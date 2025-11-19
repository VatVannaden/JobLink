package com.example.joblink.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.joblink.R;
import com.example.joblink.databinding.ActivityCompleteProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CompleteProfileActivity extends AppCompatActivity {

    private ActivityCompleteProfileBinding binding;
    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private static final int REQUEST_PERMISSIONS = 1003;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};

    private Long dateOfBirthMillis = 0L;

    private boolean isGoogleSignIn;
    private String username;
    private String email;
    private String password;
    private boolean isUploading = false;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompleteProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        getIntentData();

        setupPhoneNumberInput();
        setupImagePicker();
        setupSaveButton();
        setupDatePickers();
        setupLocationPicker();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        isGoogleSignIn = intent.getBooleanExtra("continueWithGoogle", false);
        username = intent.getStringExtra("username");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");
        System.out.println("Received data on create - Google: " + isGoogleSignIn + ", Username: " + username + ", Email: " + email);
    }

    private void setupPhoneNumberInput() {
        binding.editTextPhoneNumber.setText("0 ");
        binding.editTextPhoneNumber.setSelection(binding.editTextPhoneNumber.getText().length());


        binding.editTextPhoneNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String input = s.toString();
                if (!input.startsWith("0 ")) {
                    String cleanInput = input.replace("0 ", "").replace("0", "");
                    binding.editTextPhoneNumber.setText("0 " + cleanInput);
                    binding.editTextPhoneNumber.setSelection(binding.editTextPhoneNumber.getText().length());
                }
                isFormatting = false;
            }
        });
    }

    private void setupImagePicker() {
        binding.addProfileImg.setOnClickListener(v -> {
            if (hasPermissions()) {
                showImagePickerDialog();
            } else {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS);
            }
        });
    }

    private void setupSaveButton() {
        binding.confirmButton.setOnClickListener(v -> uploadProfile());
    }

    private void setupDatePickers() {
        binding.dateOfBirth.setOnClickListener(v -> showDatePickerDialog());
    }

    private void setupLocationPicker() {
        binding.location.setOnClickListener(v -> showLocationDialog());
    }

    private void showLocationDialog() {
        String[] locations = {  "Phnom Penh",
                "Banteay Meanchey",
                "Battambang",
                "Kampong Cham",
                "Kampong Chhnang",
                "Kampong Speu",
                "Kampong Thom",
                "Kampot",
                "Kandal",
                "Kep",
                "Koh Kong",
                "Kratié",
                "Mondulkiri",
                "Oddar Meanchey",
                "Pailin",
                "Preah Sihanouk",
                "Preah Vihear",
                "Prey Veng",
                "Pursat",
                "Ratanakiri",
                "Siem Reap",
                "Stung Treng",
                "Svay Rieng",
                "Takéo",
                "Tboung Khmum"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Province");
        builder.setItems(locations, (dialog, which) -> binding.location.setText(locations[which]));
        builder.show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -18);

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            Calendar minAge = Calendar.getInstance();
            minAge.add(Calendar.YEAR, 0);

            if (selectedDate.after(minAge)) {
                Toast.makeText(this, "You must be at least 16 years old", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.US);
            binding.dateOfBirth.setText(sdf.format(selectedDate.getTime()));
            dateOfBirthMillis = selectedDate.getTimeInMillis();
        };


        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showImagePickerDialog() {
        if (!hasGalleryPermission()) {
            requestGalleryPermission();
            return;
        }

        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image From");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Profile_Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Profile image for JobLink");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gallery error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfile() {
        if (isUploading) {
            Toast.makeText(this, "Upload in progress...", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = binding.editTextPhoneNumber.getText().toString().trim();
        String location = binding.location.getText().toString().trim();
        String profession = binding.editTextProfession.getText().toString().trim();

        if (imageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = binding.radioGroupGender.getCheckedRadioButtonId();
        String gender;
        if (selectedGenderId == R.id.radioMale) {
            gender = "Male";
        } else if (selectedGenderId == R.id.radioFemale) {
            gender = "Female";
        } else {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateOfBirthMillis == 0L) {
            Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (location.isEmpty() || location.equals("Province")) {
            Toast.makeText(this, "Please select your location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profession.isEmpty()) {
            Toast.makeText(this, "Please enter your profession", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidImageUri(imageUri)) {
            Toast.makeText(this, "Invalid image selected. Please choose another image.", Toast.LENGTH_SHORT).show();
            return;
        }

        isUploading = true;
        binding.confirmButton.setEnabled(false);

        uploadImageToFirebase(gender, phone, location, profession);
    }


    private boolean isValidImageUri(Uri uri) {
        if (uri == null) return false;

        String scheme = uri.getScheme();
        if (scheme == null) return false;

        return scheme.equals("content") || scheme.equals("file");
    }

    private void uploadImageToFirebase(String gender, String phone, String location, String profession) {
        try {
            String userId;
            if (isGoogleSignIn && auth.getCurrentUser() != null) {
                userId = auth.getCurrentUser().getUid();
            } else {
                userId = "temp_" + System.currentTimeMillis();
            }

            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("profile_images").child(userId + ".jpg");

            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String profileImageUrl = downloadUri.toString();
                    proceedWithUserCreation(profileImageUrl, gender, phone, location, profession, userId);
                }).addOnFailureListener(e -> {
                    showError("Failed to get image URL: " + e.getMessage());
                });
            }).addOnFailureListener(e -> {
                showError("Image upload failed: " + e.getMessage());
            });

        } catch (Exception e) {
            showError("Upload error: " + e.getMessage());
        }
    }

    private void proceedWithUserCreation(String profileImageUrl, String gender, String phone, String location, String profession, String tempUserId) {
        if (isGoogleSignIn) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                saveProfileToFirebase(user.getUid(), user.getEmail(), username, gender, phone, location, profession, profileImageUrl);
            } else {
                showError("Google sign-in failed. Please try again.");
            }
        } else {
            if (password == null || password.isEmpty()) {
                showError("Password is missing. Please go back and try again.");
                return;
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(registerTask -> {
                if (registerTask.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        saveProfileToFirebase(user.getUid(), email, username, gender, phone, location, profession, profileImageUrl);
                    } else {
                        showError("Failed to get user after creation.");
                    }
                } else {
                    String errorMessage = registerTask.getException() != null ?
                            registerTask.getException().getMessage() : "Registration failed";
                    showError("Registration failed: " + errorMessage);
                }
            });
        }
    }

    private void saveProfileToFirebase(String userId, String email, String username, String gender, String phone, String location, String profession, String profileImageUrl) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("username", username);
        profileData.put("email", email);
        profileData.put("gender", gender);
        profileData.put("phoneNumber", phone);
        profileData.put("location", location);
        profileData.put("profession", profession);
        profileData.put("dateOfBirth", dateOfBirthMillis);
        profileData.put("profileImageUrl", profileImageUrl);
        profileData.put("isProfileComplete", true);
        profileData.put("createdAt", System.currentTimeMillis());


        firebaseDatabase.getReference("Users").child(userId)
                .setValue(profileData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile completed successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showError("Failed to save profile: " + e.getMessage());
                });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        resetUploadState();
    }

    private void resetUploadState() {
        isUploading = false;
        binding.confirmButton.setEnabled(true);
        binding.confirmButton.setText("Confirm");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && hasPermissions()) {
            showImagePickerDialog();
        } else if (requestCode == 2001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showImagePickerDialog();
        } else {
            Toast.makeText(this, "Permission required to use this feature", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasGalleryPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestGalleryPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 2001);
    }

    private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
