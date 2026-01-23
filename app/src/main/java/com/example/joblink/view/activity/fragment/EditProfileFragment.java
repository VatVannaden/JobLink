package com.example.joblink.view.activity.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.joblink.R;
import com.example.joblink.view.activity.HomeActivity;
import com.example.joblink.model.User;
import com.example.joblink.repository.UserRepository;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private FirebaseStorage storage;

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
                if (allGranted) showImagePickerDialog();
                else if (isAdded()) Toast.makeText(getContext(), "Permissions required.", Toast.LENGTH_SHORT).show();
            });

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        storage = FirebaseStorage.getInstance();

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
                String newGender = (checkedId == R.id.radioMale) ? "male" : "female";
                if (!newGender.equals(currentUserData.getGender())) {
                    currentUserData.setGender(newGender);
                    updateUserInDatabase("Gender updated");
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
                }
            });
        }
    }

    private void populateUI() {
        if (currentUserData == null || !isAdded()) return;

        profileName.setText(currentUserData.getUsername());
        newUsername.setText(currentUserData.getUsername());
        newPhone.setText(currentUserData.getPhone());
        editLocation.setText(currentUserData.getLocation());
        editProfession.setText(currentUserData.getProfession());

        if ("male".equalsIgnoreCase(currentUserData.getGender())) {
            radioMale.setChecked(true);
            profileGender.setText("Male");
        } else if ("female".equalsIgnoreCase(currentUserData.getGender())) {
            radioFemale.setChecked(true);
            profileGender.setText("Female");
        }

        if (!TextUtils.isEmpty(currentUserData.getDob())) {
            try {
                SimpleDateFormat webFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = webFormat.parse(currentUserData.getDob());
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
                editBirthday.setText(displayFormat.format(date));
                profileAge.setText(String.valueOf(calculateAge(date)));
            } catch (ParseException e) {
                editBirthday.setText(currentUserData.getDob());
            }
        }

        Glide.with(this)
                .load(currentUserData.getPhotoURL())
                .placeholder(R.drawable.img)
                .error(R.drawable.img)
                .into(profileImage);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            String newDob = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day);
            if (currentUserData != null && !newDob.equals(currentUserData.getDob())) {
                currentUserData.setDob(newDob);
                updateUserInDatabase("Birthday updated");
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showLocationDialog() {
        String[] locations = getResources().getStringArray(R.array.provinces);
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Province")
                .setItems(locations, (dialog, which) -> {
                    String selected = locations[which];
                    if (currentUserData != null && !selected.equals(currentUserData.getLocation())) {
                        currentUserData.setLocation(selected);
                        updateUserInDatabase("Location updated");
                    }
                }).show();
    }

    private void showProfessionDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("e.g., Developer");
        if (currentUserData != null) input.setText(currentUserData.getProfession());

        new AlertDialog.Builder(requireContext())
                .setTitle("Enter Profession")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String prof = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(prof) && currentUserData != null) {
                        currentUserData.setProfession(prof);
                        updateUserInDatabase("Profession updated");
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private void updateUsername() {
        String name = newUsername.getText().toString().trim();
        String pass = passwordUsername.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pass)) return;

        reauthenticateAndRun(pass, () -> {
            currentUserData.setUsername(name);
            updateUserInDatabase("Username updated");
            passwordUsername.setText("");
        });
    }

    private void updatePhoneNumber() {
        String phone = newPhone.getText().toString().trim();
        String pass = passwordPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(pass)) return;

        reauthenticateAndRun(pass, () -> {
            currentUserData.setPhone(phone);
            updateUserInDatabase("Phone updated");
            passwordPhone.setText("");
        });
    }

    private void uploadProfileImage() {
        if (newImageUri == null || auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        // Match Website Storage Path: profiles/{uid}/profile.jpg
        StorageReference fileRef = storage.getReference().child("profiles").child(uid).child("profile.jpg");

        fileRef.putFile(newImageUri).continueWithTask(task -> {
            if (!task.isSuccessful()) throw task.getException();
            return fileRef.getDownloadUrl();
        }).addOnSuccessListener(uri -> {
            currentUserData.setPhotoURL(uri.toString());
            updateUserInDatabase("Profile image updated");
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show());
    }

    private void updateUserInDatabase(String successMsg) {
        userRepository.updateUser(currentUserData.getUid(), currentUserData, new UserRepository.UserRepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()) Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reauthenticateAndRun(String password, Runnable action) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) action.run();
            else if (isAdded()) Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show();
        });
    }

    private void changePassword() {
        String oldP = oldPassword.getText().toString().trim();
        String newP = newPassword.getText().toString().trim();
        if (newP.length() < 8 || !newP.equals(confirmPassword.getText().toString().trim())) return;

        reauthenticateAndRun(oldP, () -> auth.getCurrentUser().updatePassword(newP).addOnCompleteListener(task -> {
            if (isAdded() && task.isSuccessful()) {
                Toast.makeText(getContext(), "Password changed", Toast.LENGTH_SHORT).show();
                oldPassword.setText(""); newPassword.setText(""); confirmPassword.setText("");
            }
        }));
    }

    private int calculateAge(Date birthDate) {
        Calendar dob = Calendar.getInstance();
        dob.setTime(birthDate);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--;
        return age;
    }

    private void checkAndRequestPermissions() {
        String imagePerm = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        requestPermissionsLauncher.launch(new String[]{Manifest.permission.CAMERA, imagePerm});
    }

    private void openCamera() {
        ContentValues v = new ContentValues(); v.put(MediaStore.Images.Media.TITLE, "Profile");
        newImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
        cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, newImageUri));
    }

    private void openGallery() {
        galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomeActivity) ((HomeActivity) getActivity()).showBottomNavigationView(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof HomeActivity) ((HomeActivity) getActivity()).showBottomNavigationView(true);
    }
}