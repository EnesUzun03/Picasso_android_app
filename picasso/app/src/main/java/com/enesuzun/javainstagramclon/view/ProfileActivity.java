package com.enesuzun.javainstagramclon.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.enesuzun.javainstagramclon.R;
import com.enesuzun.javainstagramclon.adapter.PostAdapter;
import com.enesuzun.javainstagramclon.databinding.ActivityProfileBinding;
import com.enesuzun.javainstagramclon.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ArrayList<Post> postArrayList;
    private PostAdapter postAdapter;
    private boolean showingUserPosts = true; // Varsayılan olarak paylaşılanları göster

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        postArrayList = new ArrayList<>();

        // Kullanıcı emailini göster
        binding.emailText.setText(auth.getCurrentUser().getEmail());

        // RecyclerView ayarları
        binding.profileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postArrayList);
        binding.profileRecyclerView.setAdapter(postAdapter);

        // Button click listeners
        binding.postsButton.setOnClickListener(v -> {
            if (!showingUserPosts) {
                showingUserPosts = true;
                getUserPosts();
                updateButtonStyles();
            }
        });

        binding.savedButton.setOnClickListener(v -> {
            if (showingUserPosts) {
                showingUserPosts = false;
                getSavedPosts();
                updateButtonStyles();
            }
        });

        // İlk yükleme
        updateButtonStyles();
        getUserPosts();
    }

    private void updateButtonStyles() {
        if (showingUserPosts) {
            binding.postsButton.setAlpha(1.0f);
            binding.savedButton.setAlpha(0.5f);
        } else {
            binding.postsButton.setAlpha(0.5f);
            binding.savedButton.setAlpha(1.0f);
        }
    }

    private void getUserPosts() {
        firestore.collection("Post")
                .whereEqualTo("useremail", auth.getCurrentUser().getEmail())
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        //Toast.makeText(ProfileActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        updatePostList(value.getDocuments());
                    }
                });
    }

    private void getSavedPosts() {
        firestore.collection("Post")
                .whereArrayContains("savedBy", auth.getCurrentUser().getEmail())
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        //Toast.makeText(ProfileActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        updatePostList(value.getDocuments());
                    }
                });
    }

    private void updatePostList(List<DocumentSnapshot> documents) {
        postArrayList.clear();//Öncelikle listeyi temizler
        for (DocumentSnapshot document : documents) {
            Map<String, Object> data = document.getData();
            if (data != null) {
                String userEmail = (String) data.get("useremail");
                String comment = (String) data.get("comment");
                String downloadUrl = (String) data.get("downloadurl");
                ArrayList<String> likedBy = (ArrayList<String>) data.get("likedBy");
                ArrayList<String> savedBy = (ArrayList<String>) data.get("savedBy");

                Post post = new Post(userEmail, comment, downloadUrl);
                post.setId(document.getId());

                if (likedBy != null) {
                    for (String email : likedBy) {
                        post.addLike(email);
                    }
                }

                if (savedBy != null) {
                    for (String email : savedBy) {
                        post.addSave(email);
                    }
                }

                postArrayList.add(post);
            }
        }
        postAdapter.notifyDataSetChanged();
    }
}