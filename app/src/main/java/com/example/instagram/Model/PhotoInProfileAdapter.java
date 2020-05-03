package com.example.instagram.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.R;

import java.util.ArrayList;

public class PhotoInProfileAdapter extends RecyclerView.Adapter<PhotoInProfileAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Post> postsList;

    public PhotoInProfileAdapter(Context mContext, ArrayList<Post> postsList) {
        this.mContext = mContext;
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photo_in_profile_item, parent, false);
        return new PhotoInProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postsList.get(position);

        Glide.with(mContext).load(post.getImageUrl()).into(holder.photo);
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photoInProfile_imageview);
        }
    }
}
