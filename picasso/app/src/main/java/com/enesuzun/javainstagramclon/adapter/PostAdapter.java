package com.enesuzun.javainstagramclon.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.enesuzun.javainstagramclon.R;
import com.enesuzun.javainstagramclon.databinding.RecyclerRowBinding;
import com.enesuzun.javainstagramclon.model.Post;
import com.squareup.picasso.Picasso;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.enesuzun.javainstagramclon.view.CommentActivity;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    private ArrayList<Post> postArrayList;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public PostAdapter(ArrayList<Post> postArrayList) {
        this.postArrayList = postArrayList;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PostHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        holder.recyclerRowBinding.recyclerViewUserEmailText.setText(postArrayList.get(position).email);
        holder.recyclerRowBinding.recyclerViewCommentId.setText(postArrayList.get(position).comment);
        Picasso.get().load(postArrayList.get(position).downloadUrl).into(holder.recyclerRowBinding.recyclerViewImageView);

        Post post = postArrayList.get(position);
        String currentUserEmail = auth.getCurrentUser().getEmail();
        
        if (post.isLikedBy(currentUserEmail)) {
            holder.likeButton.setImageResource(R.drawable.ic_like_filled);
        } else {
            holder.likeButton.setImageResource(R.drawable.ic_like_empty);
        }

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post.isLikedBy(currentUserEmail)) {
                    post.addLike(currentUserEmail);
                    holder.likeButton.setImageResource(R.drawable.ic_like_filled);
                    
                    firestore.collection("Post")
                            .whereEqualTo("downloadurl", post.downloadUrl)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    ArrayList<String> likedBy = (ArrayList<String>) document.get("likedBy");
                                    if (likedBy == null) {
                                        likedBy = new ArrayList<>();
                                    }
                                    
                                    if (!likedBy.contains(currentUserEmail)) {
                                        likedBy.add(currentUserEmail);
                                        
                                        document.getReference().update(
                                            "likedBy", likedBy,
                                            "likeCount", post.getLikeCount()
                                        );
                                    }
                                }
                            });
                } else {
                    post.removeLike(currentUserEmail);
                    holder.likeButton.setImageResource(R.drawable.ic_like_empty);
                    
                    firestore.collection("Post")
                            .whereEqualTo("downloadurl", post.downloadUrl)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    ArrayList<String> likedBy = (ArrayList<String>) document.get("likedBy");
                                    if (likedBy != null && likedBy.contains(currentUserEmail)) {
                                        likedBy.remove(currentUserEmail);
                                        
                                        document.getReference().update(
                                            "likedBy", likedBy,
                                            "likeCount", post.getLikeCount()
                                        );
                                    }
                                }
                            });
                }
                holder.likeCountText.setText(String.valueOf(post.getLikeCount()));
            }
        });

        holder.likeCountText.setText(String.valueOf(post.getLikeCount()));

        if (post.isSavedBy(currentUserEmail)) {
            holder.saveButton.setImageResource(R.drawable.ic_save_filled);
        } else {
            holder.saveButton.setImageResource(R.drawable.ic_save_empty);
        }

        holder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post.isSavedBy(currentUserEmail)) {
                    post.addSave(currentUserEmail);
                    holder.saveButton.setImageResource(R.drawable.ic_save_filled);
                    
                    firestore.collection("Post")
                            .whereEqualTo("downloadurl", post.downloadUrl)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    ArrayList<String> savedBy = (ArrayList<String>) document.get("savedBy");
                                    if (savedBy == null) {
                                        savedBy = new ArrayList<>();
                                    }
                                    
                                    if (!savedBy.contains(currentUserEmail)) {
                                        savedBy.add(currentUserEmail);
                                        document.getReference().update("savedBy", savedBy);
                                    }
                                }
                            });
                } else {
                    post.removeSave(currentUserEmail);
                    holder.saveButton.setImageResource(R.drawable.ic_save_empty);
                    
                    firestore.collection("Post")
                            .whereEqualTo("downloadurl", post.downloadUrl)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    ArrayList<String> savedBy = (ArrayList<String>) document.get("savedBy");
                                    if (savedBy != null && savedBy.contains(currentUserEmail)) {
                                        savedBy.remove(currentUserEmail);
                                        document.getReference().update("savedBy", savedBy);
                                    }
                                }
                            });
                }
            }
        });

        firestore.collection("Post").document(post.getId())
                .collection("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int commentCount = queryDocumentSnapshots.size();
                    post.setCommentCount(commentCount);
                    holder.commentCountText.setText(String.valueOf(commentCount));
                });

        holder.commentButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CommentActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("postComment", post.comment);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    class PostHolder extends RecyclerView.ViewHolder {

        RecyclerRowBinding recyclerRowBinding;
        public ImageButton likeButton;
        public ImageButton saveButton;
        public ImageButton commentButton;
        public TextView likeCountText;
        public TextView commentCountText;

        public PostHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
            this.likeButton = recyclerRowBinding.likeButton;
            this.saveButton = recyclerRowBinding.saveButton;
            this.commentButton = recyclerRowBinding.commentButton;
            this.likeCountText = recyclerRowBinding.likeCountText;
            this.commentCountText = recyclerRowBinding.commentCountText;
        }
    }
}
