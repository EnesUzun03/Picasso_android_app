package com.enesuzun.javainstagramclon.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.enesuzun.javainstagramclon.databinding.ItemCommentBinding;
import com.enesuzun.javainstagramclon.model.Comment;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {

    private ArrayList<Comment> comments;

    public CommentAdapter(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    //Burada satır oluşturuyoruz
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//parent satırın ekleneceğı ana ViewGrup
        ItemCommentBinding binding = ItemCommentBinding.inflate(//ItemCommentBinding bir viewBinding'dir
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CommentHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.binding.commentUserEmail.setText(comment.getUserEmail());
        holder.binding.commentContent.setText(comment.getCommentText());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;

        public CommentHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 