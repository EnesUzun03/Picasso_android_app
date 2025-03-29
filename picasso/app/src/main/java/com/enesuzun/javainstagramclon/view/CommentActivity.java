package com.enesuzun.javainstagramclon.view;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.enesuzun.javainstagramclon.R;
import com.enesuzun.javainstagramclon.adapter.CommentAdapter;
import com.enesuzun.javainstagramclon.databinding.ActivityCommentBinding;
import com.enesuzun.javainstagramclon.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {

    private ActivityCommentBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String postId;
    private String postComment;
    private ArrayList<Comment> comments;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Intent'ten gelen yorumu al
        postComment = getIntent().getStringExtra("postComment");
        binding.commentText.setText(postComment);

        comments = new ArrayList<>();
        setupRecyclerView();
        getComments();

        binding.sendCommentButton.setOnClickListener(v -> {
            String commentText = binding.commentEditText.getText().toString();
            if (!commentText.isEmpty()) {
                addComment(commentText);
                binding.commentEditText.setText("");
            }
        });
    }

    private void setupRecyclerView() {
        binding.commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(comments);
        binding.commentRecyclerView.setAdapter(commentAdapter);
    }

    private void getComments() {
        postId = getIntent().getStringExtra("postId");
        firestore.collection("Post").document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        comments.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String userEmail = doc.getString("userEmail");
                            String commentText = doc.getString("commentText");
                            comments.add(new Comment(userEmail, commentText));
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void addComment(String commentText) {
        HashMap<String, Object> commentData = new HashMap<>();
        commentData.put("userEmail", auth.getCurrentUser().getEmail());
        commentData.put("commentText", commentText);
        commentData.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("Post").document(postId)
                .collection("comments")
                .add(commentData)
                .addOnSuccessListener(documentReference -> {
                    // Yorum sayısını güncelle
                    firestore.collection("Post").document(postId)
                            .collection("comments")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                int commentCount = queryDocumentSnapshots.size();
                                firestore.collection("Post").document(postId)
                                        .update("commentCount", commentCount);
                            });
                });
    }
} 