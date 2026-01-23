package com.example.joblink.view.activity.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.joblink.R;
import com.example.joblink.viewmodel.CreatePostViewModel;
import com.example.joblink.model.Post;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.card.MaterialCardView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePostLocationFragment extends Fragment {

    private CreatePostViewModel viewModel;
    private Post post;

    private ProgressBar progressBar;
    private TextView stepText;
    private MaterialCardView backButton, nextButton;

    private TextView locationText;
    private EditText writeSomethingText, exactLocationText;
    private RadioGroup workTypeRadioGroup;

    private final ArrayList<Integer> allRadioIds = new ArrayList<>();

    private final int currentStep = 2;
    private final int totalSteps = 6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post_location, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);
        post = viewModel.getPost();

        initializeViews(view);
        restoreUIState(view);
        setupListeners(view);
        updateProgress();

        return view;
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        stepText = view.findViewById(R.id.step);
        backButton = view.findViewById(R.id.backButton);
        nextButton = view.findViewById(R.id.nextButton);

        locationText = view.findViewById(R.id.locationText);
        writeSomethingText = view.findViewById(R.id.writeSomethingText);
        exactLocationText = view.findViewById(R.id.exactLocationText);
        workTypeRadioGroup = view.findViewById(R.id.workTypeRadioGroup);

        allRadioIds.clear();
        allRadioIds.add(R.id.onSite);
        allRadioIds.add(R.id.remote);
        allRadioIds.add(R.id.hybrid);
    }

    private void restoreUIState(View view) {
        if (post == null) return;

        locationText.setText(post.getLocation());
        writeSomethingText.setText(post.getAddressDetails());
        exactLocationText.setText(post.getMapsLink());

        String workplaceType = post.getWorkPlaceType();
        if (workplaceType != null && !workplaceType.isEmpty()) {
            for (int id : allRadioIds) {
                RadioButton rb = view.findViewById(id);
                if (rb != null && rb.getText().toString().equalsIgnoreCase(workplaceType)) {
                    rb.setChecked(true);
                    rb.setTag(true);
                    break;
                }
            }
        }
    }

    private void setupListeners(View view) {
        locationText.setOnClickListener(v -> showLocationDialog());

        writeSomethingText.addTextChangedListener(new SimpleTextWatcher(s -> {
            if (post != null) post.setAddressDetails(s);
        }));
        exactLocationText.addTextChangedListener(new SimpleTextWatcher(s -> {
            if (post != null) post.setMapsLink(s);
        }));

        for (int radioId : allRadioIds) {
            RadioButton rb = view.findViewById(radioId);
            if (rb != null) {
                rb.setOnClickListener(v -> {
                    boolean wasChecked = rb.getTag() != null && (boolean) rb.getTag();
                    uncheckAllRadios(view);

                    if (wasChecked) {
                        rb.setTag(false);
                        post.setWorkPlaceType(null);
                    } else {
                        rb.setChecked(true);
                        rb.setTag(true);
                        post.setWorkPlaceType(rb.getText().toString());
                    }
                });
            }
        }

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        nextButton.setOnClickListener(v -> {
            validateForm();
        });
    }

    private void showLocationDialog() {
        String[] locations = getResources().getStringArray(R.array.provinces);
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Province")
                .setItems(locations, (dialog, which) -> {
                    locationText.setText(locations[which]);
                    if (post != null) post.setLocation(locations[which]);
                })
                .show();
    }

    private void uncheckAllRadios(View view) {
        for (int id : allRadioIds) {
            RadioButton rb = view.findViewById(id);
            if (rb != null) {
                rb.setChecked(false);
                rb.setTag(false);
            }
        }
    }

    private void expandShortenedUrl(String shortUrl, Consumer<String> callback) {
        new Thread(() -> {
            String currentUrl = shortUrl;
            try {
                for (int i = 0; i < 5; i++) {
                    HttpURLConnection connection = (HttpURLConnection) new URL(currentUrl).openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    // Mimic a real browser to avoid being blocked by Google
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    String location = connection.getHeaderField("Location");
                    connection.disconnect();

                    Log.d("expandShortenedUrl", "Step " + i + ": " + responseCode + " Redirect to: " + location);

                    if (responseCode >= 300 && responseCode < 400 && location != null) {
                        if (location.startsWith("/")) {
                            URL base = new URL(currentUrl);
                            currentUrl = base.getProtocol() + "://" + base.getHost() + location;
                        } else {
                            currentUrl = location;
                        }
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e("expandShortenedUrl", "Error expanding URL: " + shortUrl, e);
            }

            final String result = currentUrl;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> callback.accept(result));
            }
        }).start();
    }

    private boolean isGoogleMapsShortUrl(String url) {
        return url != null && (url.contains("goo.gl/maps") || url.contains("maps.app.goo.gl"));
    }

    private void validateForm() {
        if (post == null) {
            Toast.makeText(getContext(), "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(post.getLocation())) {
            Toast.makeText(getContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
            locationText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(post.getMapsLink())) {
            Toast.makeText(getContext(), "Please enter a Google Maps link", Toast.LENGTH_SHORT).show();
            exactLocationText.requestFocus();
            return;
        }

        String regex = "^(https?://)?(www\\.)?(google\\.com/maps|goo\\.gl/maps|maps\\.app\\.goo\\.gl|maps\\.google\\.com)/.*$";
        if (!post.getMapsLink().toLowerCase().matches(regex)) {
            Toast.makeText(getContext(), "Please enter a valid Google Maps link", Toast.LENGTH_SHORT).show();
            exactLocationText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(post.getAddressDetails())) {
            Toast.makeText(getContext(), "Please enter an address description", Toast.LENGTH_SHORT).show();
            writeSomethingText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(post.getWorkPlaceType())) {
            Toast.makeText(getContext(), "Please select a work model (On-site, Remote, or Hybrid)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        Toast.makeText(getContext(), "Processing map link...", Toast.LENGTH_SHORT).show();

        // Check if URL is shortened and expand it
        if (isGoogleMapsShortUrl(post.getMapsLink())) {
            expandShortenedUrl(post.getMapsLink(), expandedUrl -> {
                processMapUrl(expandedUrl);
            });
        } else {
            processMapUrl(post.getMapsLink());
        }
    }

    private void processMapUrl(String url) {
        Log.d("processMapUrl", "Final URL being analyzed: " + url);
        LatLng coordinates = getLatLngFromUrl(url);
        if (coordinates != null) {
            post.setLatitude(coordinates.latitude);
            post.setLongitude(coordinates.longitude);
            navigateToNext();
        } else {
            Log.e("processMapUrl", "Failed to extract coordinates from: " + url);
            Toast.makeText(getContext(), "Could not extract coordinates. Try opening the map in a browser and copying the link from the address bar.", Toast.LENGTH_LONG).show();
            exactLocationText.requestFocus();
        }
    }

    private LatLng getLatLngFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;

        try {
            // Pattern 1: @lat,lng (Standard browser format)
            Pattern p1 = Pattern.compile("@(-?[\\d.]+),(-?[\\d.]+)");
            Matcher m1 = p1.matcher(url);
            if (m1.find()) {
                return new LatLng(Double.parseDouble(m1.group(1)), Double.parseDouble(m1.group(2)));
            }

            // Pattern 2: !3dlat!4dlng (Common in 'place' URLs)
            Pattern p2 = Pattern.compile("!3d(-?[\\d.]+)!4d(-?[\\d.]+)");
            Matcher m2 = p2.matcher(url);
            if (m2.find()) {
                return new LatLng(Double.parseDouble(m2.group(1)), Double.parseDouble(m2.group(2)));
            }

            // Pattern 3: search/lat,lng OR query=lat,lng OR q=lat,lng
            Pattern p3 = Pattern.compile("(?:search/|place/|query=|q=|ll=|dir/)(-?[\\d.]+)(?:%2C|,|\\+|\\s)+(-?[\\d.]+)");
            Matcher m3 = p3.matcher(url);
            if (m3.find()) {
                double lat = Double.parseDouble(m3.group(1));
                double lng = Double.parseDouble(m3.group(2));
                if (Math.abs(lat) <= 90 && Math.abs(lng) <= 180) return new LatLng(lat, lng);
            }

            // Pattern 4: Fallback - look for any sequence of "decimal,decimal" that are valid coordinates
            Pattern p4 = Pattern.compile("(-?\\d+\\.\\d+)[,|+]+(-?\\d+\\.\\d+)");
            Matcher m4 = p4.matcher(url);
            while (m4.find()) {
                double lat = Double.parseDouble(m4.group(1));
                double lng = Double.parseDouble(m4.group(2));
                // Coordinates must be within valid world ranges
                if (Math.abs(lat) <= 90 && Math.abs(lng) <= 180) {
                    return new LatLng(lat, lng);
                }
            }

        } catch (Exception e) {
            Log.e("getLatLngFromUrl", "Error parsing coordinates", e);
        }

        return null;
    }

    private void navigateToNext() {
        CreatePostScheduleFragment fragment = new CreatePostScheduleFragment();
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void updateProgress() {
        int progress = (int) ((currentStep / (float) totalSteps) * 100);
        if (progressBar != null) progressBar.setProgress(progress);
        if (stepText != null) stepText.setText(String.format(Locale.US, "Step %d of %d", currentStep, totalSteps));
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<String> onChange;

        public SimpleTextWatcher(Consumer<String> onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            onChange.accept(s.toString().trim());
        }
    }
}
