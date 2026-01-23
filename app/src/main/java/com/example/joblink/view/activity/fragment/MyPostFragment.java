package com.example.joblink.view.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.view.activity.HomeActivity;
import com.example.joblink.adapter.PostAdapter;
import com.example.joblink.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyPostFragment extends Fragment implements PostAdapter.OnItemClickListener, PostAdapter.OnBookmarkClickListener {

    private RecyclerView myPostRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private LinearLayout noPostLayout;

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private boolean isDataReady = false;
    private boolean isAnimationFinished = false;

    public MyPostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onLoadingAnimationFinished();

        if (currentUser != null) {
            fetchUserPosts();
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        noPostLayout = view.findViewById(R.id.noPost);
        myPostRecyclerView = view.findViewById(R.id.myPostRecyclerView);

        myPostRecyclerView.setVisibility(View.GONE);
        noPostLayout.setVisibility(View.GONE);

        myPostRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, false, this, this);
        myPostRecyclerView.setAdapter(postAdapter);

        setupButtonClickListeners(view);

        if (currentUser != null) {
            fetchUserPosts();
        } else {
            isDataReady = true;
            tryShowContent();
        }
    }

    private void fetchUserPosts() {
        String currentUserId = currentUser.getUid();
        Query userPostsQuery = databaseReference.orderByChild("employerId").equalTo(currentUserId);

        userPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Post post = postSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setPostId(postSnapshot.getKey());
                            postList.add(post);
                        }
                    }
                }

                Collections.reverse(postList);
                postAdapter.notifyDataSetChanged();

                isDataReady = true;
                tryShowContent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                isDataReady = true;
                tryShowContent();
            }
        });
    }

    public void onLoadingAnimationFinished() {
        isAnimationFinished = true;
        tryShowContent();
    }

    private void tryShowContent() {
        if (isDataReady && isAnimationFinished) {
            updateUI(!postList.isEmpty());
        }
    }

    private void updateUI(boolean hasPosts) {
        if (hasPosts) {
            myPostRecyclerView.setVisibility(View.VISIBLE);
            noPostLayout.setVisibility(View.GONE);
            myPostRecyclerView.setAlpha(0f);
            myPostRecyclerView.animate().alpha(1f).setDuration(300).start();
        } else {
            myPostRecyclerView.setVisibility(View.GONE);
            noPostLayout.setVisibility(View.VISIBLE);
            noPostLayout.setAlpha(0f);
            noPostLayout.animate().alpha(1f).setDuration(300).start();
        }
    }

    private void setupButtonClickListeners(View view) {
        View backButton = view.findViewById(R.id.imageView12);
        if (backButton != null) {
            backButton.setOnClickListener(v -> handleBackButton());
        }
    }

    private void handleBackButton() {
        if (getActivity() != null) {
            getParentFragmentManager().popBackStack();
        }
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

    @Override
    public void onItemClick(Post post) {
        if (post != null && post.getPostId() != null && isAdded()) {
            Fragment detailFragment = new PostDetailFragment();
            Bundle args = new Bundle();
            args.putString("postId", post.getPostId());
            detailFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.mainFragment, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onBookmarkClick(Post post, ImageButton bookmarkButton) {
    }
}