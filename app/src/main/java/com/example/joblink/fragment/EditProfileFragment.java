package com.example.joblink.fragment;

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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.activity.HomeActivity;
import com.example.joblink.model.User;
import com.example.joblink.repository.UserRepository;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditProfileFragment extends Fragment {
    private ImageView profileImage, backButton;
    private TextView profileName, profileGender, profileAge;
    private EditText newUsername, passwordUsername, newPhone, passwordPhone, oldPassword, newPassword, confirmPassword;
    private TextView editLocation, editBirthday, editProfession;
    private RadioGroup radioGroupGender;
    private RadioButton radioMale, radioFemale;
    private Button btnUpdateUsername, btnUpdatePhone, btnChangePassword;
    private View changePhotoButton;

    private FirebaseAuth auth;
    private UserRepository userRepository;
    private StorageReference storageProfileImagesRef;

    private User currentUserData;
    private Uri newImageUri;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            newImageUri = result.getData().getData();
                            if (newImageUri != null) {
                                profileImage.setImageURI(newImageUri);
                                uploadProfileImage();
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK && newImageUri != null) {
                            profileImage.setImageURI(newImageUri);
                            uploadProfileImage();
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
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Permissions are required to select an image.", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        storageProfileImagesRef = FirebaseStorage.getInstance().getReference("profile_images");

        initializeViews(view);
        setupListeners();
        observeUserData();
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        backButton = view.findViewById(R.id.imageView12);
        changePhotoButton = view.findViewById(R.id.changePhotoButton);
        profileName = view.findViewById(R.id.profileName);
        profileGender = view.findViewById(R.id.profileGender);
        profileAge = view.findViewById(R.id.profileAge);
        newUsername = view.findViewById(R.id.newUsername);
        passwordUsername = view.findViewById(R.id.passwordUsername);
        btnUpdateUsername = view.findViewById(R.id.btnChangeUsername);
        newPhone = view.findViewById(R.id.newPhone);
        passwordPhone = view.findViewById(R.id.passwordPhone);
        btnUpdatePhone = view.findViewById(R.id.btnChangePhone);
        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        radioMale = view.findViewById(R.id.radioMale);
        radioFemale = view.findViewById(R.id.radioFemale);
        editLocation = view.findViewById(R.id.editLocation);
        editBirthday = view.findViewById(R.id.editBirthday);
        editProfession = view.findViewById(R.id.editProfession);
        oldPassword = view.findViewById(R.id.oldPassword);
        newPassword = view.findViewById(R.id.newPassword);
        confirmPassword = view.findViewById(R.id.confirmPassword);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        changePhotoButton.setOnClickListener(v -> checkAndRequestPermissions());
        editLocation.setOnClickListener(v -> showLocationDialog());
        editBirthday.setOnClickListener(v -> showDatePickerDialog());
        editProfession.setOnClickListener(v -> showProfessionDialog());

        btnUpdateUsername.setOnClickListener(v -> updateUsername());
        btnUpdatePhone.setOnClickListener(v -> updatePhoneNumber());
        btnChangePassword.setOnClickListener(v -> changePassword());

        radioGroupGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (currentUserData != null) {
                String newGender = (checkedId == R.id.radioMale) ? "Male" : "Female";
                if (!newGender.equals(currentUserData.getGender())) {
                    currentUserData.setGender(newGender);
                    userRepository.updateUser(currentUserData.getUserId(), currentUserData, new UserRepository.UserRepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Gender updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onError(Exception e) {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Failed to update gender", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void observeUserData() {
        if (auth.getCurrentUser() != null) {
            userRepository.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    currentUserData = user;
                    populateUI();
                } else {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Could not load user profile.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void populateUI() {
        if (currentUserData == null || !isAdded()) return;

        profileName.setText(currentUserData.getUsername());
        newUsername.setText(currentUserData.getUsername());
        newPhone.setText(currentUserData.getPhoneNumber());
        editLocation.setText(currentUserData.getLocation());
        editProfession.setText(currentUserData.getProfession());

        if ("Male".equals(currentUserData.getGender())) {
            radioMale.setChecked(true);
            profileGender.setText("Male");
        } else if ("Female".equals(currentUserData.getGender())) {
            radioFemale.setChecked(true);
            profileGender.setText("Female");
        }

        if (currentUserData.getDateOfBirth() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            editBirthday.setText(sdf.format(new java.util.Date(currentUserData.getDateOfBirth())));
            profileAge.setText(String.valueOf(calculateAge(currentUserData.getDateOfBirth())));
        }

        if (currentUserData.getProfileImageUrl() != null && !currentUserData.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUserData.getProfileImageUrl())
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.img);
        }
    }

    private void checkAndRequestPermissions() {
        String imagePermission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        String[] permissionsToRequest = {Manifest.permission.CAMERA, imagePermission};

        boolean allGranted = true;
        for (String permission : permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) showImagePickerDialog();
        else requestPermissionsLauncher.launch(permissionsToRequest);
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(requireContext())
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
        newImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (currentUserData != null && currentUserData.getDateOfBirth() > 0) {
            calendar.setTimeInMillis(currentUserData.getDateOfBirth());
        }

        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            calendar.set(year, month, day);
            long newDateOfBirth = calendar.getTimeInMillis();
            if (currentUserData != null && currentUserData.getDateOfBirth() != newDateOfBirth) {
                currentUserData.setDateOfBirth(newDateOfBirth);
                userRepository.updateUser(currentUserData.getUserId(), currentUserData, new UserRepository.UserRepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if(isAdded()) {
                            Toast.makeText(getContext(), "Birthday updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        if(isAdded()) {
                            Toast.makeText(getContext(), "Failed to update birthday", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showLocationDialog() {
        String[] locations = getResources().getStringArray(R.array.provinces);
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Province")
                .setItems(locations, (dialog, which) -> {
                    String selectedLocation = locations[which];
                    if (currentUserData != null && !selectedLocation.equals(currentUserData.getLocation())) {
                        currentUserData.setLocation(selectedLocation);
                        userRepository.updateUser(currentUserData.getUserId(), currentUserData, new UserRepository.UserRepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), "Location updated", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onError(Exception e) {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), "Failed to update location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .show();
    }

    private void showProfessionDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("e.g., Software Developer");
        if (currentUserData != null) input.setText(currentUserData.getProfession());

        new AlertDialog.Builder(requireContext())
                .setTitle("Enter Your Profession")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String profession = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(profession) && currentUserData != null && !profession.equals(currentUserData.getProfession())) {
                        currentUserData.setProfession(profession);
                        userRepository.updateUser(currentUserData.getUserId(), currentUserData, new UserRepository.UserRepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), "Profession updated", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onError(Exception e) {
                                if(isAdded()) {
                                    Toast.makeText(getContext(), "Failed to update profession", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUsername() {
        String newUsernameText = newUsername.getText().toString().trim();
        String password = passwordUsername.getText().toString().trim();

        if (TextUtils.isEmpty(newUsernameText)) {
            newUsername.setError("Username cannot be empty");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordUsername.setError("Password is required");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        reauthenticateAndRun(password, () -> {
            currentUserData.setUsername(newUsernameText);
            userRepository.updateUser(user.getUid(), currentUserData, new UserRepository.UserRepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if(isAdded()) {
                        Toast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
                        passwordUsername.setText("");
                    }
                }
                @Override
                public void onError(Exception e) {
                    if(isAdded()) {
                        Toast.makeText(getContext(), "Failed to update username", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void updatePhoneNumber() {
        String newPhoneText = newPhone.getText().toString().trim();
        String password = passwordPhone.getText().toString().trim();
        if (TextUtils.isEmpty(newPhoneText)) {
            newPhone.setError("Phone number cannot be empty");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordPhone.setError("Password is required");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        reauthenticateAndRun(password, () -> {
            currentUserData.setPhoneNumber(newPhoneText);
            userRepository.updateUser(user.getUid(), currentUserData, new UserRepository.UserRepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if(isAdded()) {
                        Toast.makeText(getContext(), "Phone number updated successfully", Toast.LENGTH_SHORT).show();
                        passwordPhone.setText("");
                    }
                }
                @Override
                public void onError(Exception e) {
                    if(isAdded()) {
                        Toast.makeText(getContext(), "Failed to update phone number", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void changePassword() {
        String oldPass = oldPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 8) {
            newPassword.setError("Password must be at least 8 characters");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            confirmPassword.setError("New passwords do not match");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        reauthenticateAndRun(oldPass, () -> user.updatePassword(newPass).addOnCompleteListener(task -> {
            if (isAdded()) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                    oldPassword.setText("");
                    newPassword.setText("");
                    confirmPassword.setText("");
                } else {
                    Toast.makeText(getContext(), "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }));
    }

    private void uploadProfileImage() {
        if (newImageUri == null) return;
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        if(isAdded()) {
            Toast.makeText(getContext(), "Uploading new image...", Toast.LENGTH_SHORT).show();
        }
        StorageReference fileRef = storageProfileImagesRef.child(user.getUid() + ".jpg");

        fileRef.putFile(newImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            currentUserData.setProfileImageUrl(uri.toString());
                            userRepository.updateUser(user.getUid(), currentUserData, new UserRepository.UserRepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    if(isAdded()) {
                                        Toast.makeText(getContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onError(Exception e) {
                                    if(isAdded()) {
                                        Toast.makeText(getContext(), "Failed to save new image URL", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }))
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reauthenticateAndRun(String password, Runnable action) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            if(isAdded()) {
                Toast.makeText(getContext(), "User not found or email is missing.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        action.run();
                    } else {
                        if(isAdded()) {
                            Toast.makeText(getContext(), "Authentication failed. Please check your password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private int calculateAge(long dateOfBirthMillis) {
        if (dateOfBirthMillis == 0) return 0;
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(dateOfBirthMillis);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showBottomNavigationView(true);
        }
    }
}
