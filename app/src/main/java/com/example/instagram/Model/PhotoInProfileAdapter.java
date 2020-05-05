package com.example.instagram.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.Fragment.PostDetailsFragment;
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
        final Post post = postsList.get(position);

        Glide.with(mContext).load(post.getImageUrl()).into(holder.photo);

        // open post when click on image
        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postId", post.getPostId());
                editor.putString("publisherId", post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PostDetailsFragment()).commit();
            }
        });
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
