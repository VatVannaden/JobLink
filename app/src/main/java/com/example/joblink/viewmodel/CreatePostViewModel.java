package com.example.joblink.viewmodel;

import android.net.Uri;
import androidx.lifecycle.ViewModel;

import com.example.joblink.model.Post;

import java.util.ArrayList;

public class CreatePostViewModel extends ViewModel {

    private Post post = new Post();
    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private int coverImageIndex = 0;

    public Post getPost() {
        return post;
    }

    public ArrayList<Uri> getImageUris() {
        return imageUris;
    }

    public int getCoverImageIndex() {
        return coverImageIndex;
    }

    public void setCoverImageIndex(int index) {
        if (index >= 0 && index < imageUris.size()) {
            this.coverImageIndex = index;
        } else {
            this.coverImageIndex = 0;
        }
    }

    public void clear() {
        post = new Post();
        imageUris.clear();
        coverImageIndex = 0;
    }
}
