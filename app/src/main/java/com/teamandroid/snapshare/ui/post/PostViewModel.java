package com.teamandroid.snapshare.ui.post;

import android.net.Uri;
import android.view.View;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.teamandroid.snapshare.data.model.Post;
import com.teamandroid.snapshare.data.repository.FirebaseStorageRepository;
import com.teamandroid.snapshare.data.repository.FirestoreRepository;
import com.teamandroid.snapshare.utils.Constants;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PostViewModel extends ViewModel {
    public MutableLiveData<Post> post;
    public MutableLiveData<String> description = new MutableLiveData<>();
    private MutableLiveData<Uri> mUriImage = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsAddPostSuccessful = new MutableLiveData<>();
    private FirestoreRepository mFirestoreRepository = FirestoreRepository.getInstance();
    private FirebaseStorageRepository mStorageRepository = FirebaseStorageRepository.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public MutableLiveData<Boolean> getIsLoading() {
        return mIsLoading;
    }

    public MutableLiveData<Boolean> getIsAddPostSuccessful() {
        return mIsAddPostSuccessful;
    }

    public MutableLiveData<Post> getPost() {
        if (post == null) {
            post = new MutableLiveData<>();
        }
        return post;
    }

    public void setValueUriImage(Uri uri) {
        mUriImage.setValue(uri);
    }

    public void onClickShareButton(View view) {
        Post postTemp = new Post();
        postTemp.setCaption(description.getValue());
        post.setValue(postTemp);
    }

    private void addPostToFirestore(Post post) {
        mFirestoreRepository.addPost(post, new FirestoreRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mIsAddPostSuccessful.postValue(true);
                mIsLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                mIsAddPostSuccessful.postValue(false);
                mIsLoading.postValue(false);
            }
        });
    }

    private void addPostImageToFirebaseStorage(final Post postTemp) {
        mStorageRepository.addPostImage(mUriImage.getValue(), currentUser.getUid(),
            new FirebaseStorageRepository.Callback<Uri>() {
                @Override
                public void onSuccess(Uri result) {
                    Post post = createNewPost(result.toString(), postTemp.getCaption());
                    addPostToFirestore(post);
                }

                @Override
                public void onFailure(Exception e) {
                    mIsLoading.postValue(false);
                }
            });
    }

    public void sharePost(Post post) {
        mIsLoading.postValue(true);
        addPostImageToFirebaseStorage(post);
    }

    public Post createNewPost(String imageUrl, String caption) {
        Post post = new Post();
        post.setUserId(currentUser.getUid());
        post.setCaption(caption);
        post.setImageUrl(imageUrl);
        post.setLikeCount(Constants.ZERO);
        post.setCreatedAt(Timestamp.now());
        return post;
    }
}
