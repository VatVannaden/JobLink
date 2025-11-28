package com.example.joblink.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

public class PostRepository {

    private final DatabaseReference postsRef;
    private final FirebaseAuth mAuth;

    public interface PostCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public PostRepository() {
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        mAuth = FirebaseAuth.getInstance();
    }

    public void createPost(Post post, final PostCallback<Void> callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("User not logged in."));
            return;
        }

        String postId = postsRef.push().getKey();
        if (postId == null) {
            callback.onError(new Exception("Could not generate post ID."));
            return;
        }

        post.setPostId(postId);
        post.setEmployerId(currentUser.getUid());
        post.setTimestamp(System.currentTimeMillis());

        postsRef.child(postId)
                .setValue(post)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public LiveData<List<Post>> getAllPosts() {
        MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();

        Query orderedPostsQuery = postsRef.orderByChild("timestamp");

        orderedPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> posts = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Post post = postSnapshot.getValue(Post.class);
                        if (post != null) {
                            posts.add(post);
                        }
                    }
                }
                Collections.reverse(posts);
                postsLiveData.postValue(posts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Database read failed: " + error.getMessage());
                postsLiveData.postValue(null);
            }
        });

        return postsLiveData;
    }

    public LiveData<Long> getPostCountForCurrentUser() {
        MutableLiveData<Long> postCountLiveData = new MutableLiveData<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            postCountLiveData.postValue(0L);
            return postCountLiveData;
        }

        String currentUserId = currentUser.getUid();

        Query userPostsQuery = postsRef.orderByChild("employerId").equalTo(currentUserId);

        userPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    postCountLiveData.postValue(snapshot.getChildrenCount());
                } else {
                    postCountLiveData.postValue(0L);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Post count listen failed: " + error.getMessage());
                postCountLiveData.postValue(0L);
            }
        });

        return postCountLiveData;
    }
}
