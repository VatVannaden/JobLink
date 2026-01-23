package com.example.joblink.view.activity.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.adapter.PostAdapter;
import com.example.joblink.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView searchResultsRecyclerView;
    private TextView cancelButton, noResultsTextView;

    private PostAdapter postAdapter;
    private final List<Post> allPosts = new ArrayList<>();
    private final List<Post> filteredPosts = new ArrayList<>();
    private DatabaseReference postsRef;
    private ValueEventListener postsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView = view.findViewById(R.id.searchView);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        cancelButton = view.findViewById(R.id.cancelButton);
        noResultsTextView = view.findViewById(R.id.noResultsTextView);

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (getContext() != null) {
            searchEditText.setHintTextColor(getResources().getColor(R.color.text_ico));
        }
        searchEditText.setAlpha(0.8f);

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus || searchView.getQuery().length() > 0) {
                searchEditText.setAlpha(1.0f);
            } else {
                searchEditText.setAlpha(0.8f);
            }
        });

        setupRecyclerView();
        setupSearchListener();
        setupCancelButton();
        fetchAllPosts();
    }

    private void setupRecyclerView() {
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        postAdapter = new PostAdapter(getContext(), filteredPosts, this::navigateToPostDetail, (post, button) -> {});
        searchResultsRecyclerView.setAdapter(postAdapter);
    }

    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPosts(newText);
                return true;
            }
        });

        searchView.requestFocus();
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void fetchAllPosts() {
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allPosts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        post.setPostId(snapshot.getKey());
                        allPosts.add(post);
                    }
                }
                Collections.reverse(allPosts);
                filterPosts(searchView.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SearchFragment", "Failed to read posts.", databaseError.toException());
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load posts.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        postsRef.addValueEventListener(postsListener);
    }

    private void filterPosts(String query) {
        filteredPosts.clear();
        if (query.isEmpty()) {
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Post post : allPosts) {
                boolean titleMatch = post.getTitle() != null && post.getTitle().toLowerCase().contains(lowerCaseQuery);
                boolean locationMatch = post.getLocation() != null && post.getLocation().toLowerCase().contains(lowerCaseQuery);

                if (titleMatch || locationMatch) {
                    filteredPosts.add(post);
                }
            }
        }

        if (filteredPosts.isEmpty() && !query.isEmpty()) {
            noResultsTextView.setVisibility(View.VISIBLE);
            searchResultsRecyclerView.setVisibility(View.GONE);
        } else {
            noResultsTextView.setVisibility(View.GONE);
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
        }
        postAdapter.notifyDataSetChanged();
    }

    private void navigateToPostDetail(Post post) {
        PostDetailFragment detailFragment = new PostDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", post.getPostId());
        detailFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.mainFragment, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postsRef != null && postsListener != null) {
            postsRef.removeEventListener(postsListener);
        }
    }
}
