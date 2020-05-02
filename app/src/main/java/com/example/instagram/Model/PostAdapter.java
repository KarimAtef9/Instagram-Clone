package com.example.instagram.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    ArrayList<Post> postsList;
    // required for getting image
    private Context mContext;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context context, ArrayList<Post> posts) {
        this.mContext = context;
        this.postsList = posts;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Post post = postsList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // update post specs
        Glide.with(mContext).load(post.getImageUrl()).into(holder.postImage_imageview);
        if (post.getDescription().equals("")) {
            holder.description_textview.setVisibility(View.GONE);
        } else {
            holder.description_textview.setText(post.getDescription());
        }
        updatePublisherInfo(post.getPublisher(), holder.username_textview,
                holder.publisher_textview, holder.profile_imageview);

        update_LikeButton_NoLikes(post.getPostId(), holder.like_imageview);
        updateNumberOfLikes(post.getPostId(), holder.likes_textview);

        holder.like_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // like post
                if (holder.like_imageview.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);
                } else {
                    // remove like
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profile_imageview, postImage_imageview, like_imageview, comment_imageview, save_imageview;
        public TextView username_textview, likes_textview, publisher_textview, description_textview, comments_textview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_imageview = itemView.findViewById(R.id.profile_image);
            postImage_imageview = itemView.findViewById(R.id.post_image);
            like_imageview = itemView.findViewById(R.id.like_imageview);
            comment_imageview = itemView.findViewById(R.id.comment_imageview);
            save_imageview = itemView.findViewById(R.id.save_imageview);

            username_textview = itemView.findViewById(R.id.username_textview);
            likes_textview = itemView.findViewById(R.id.likes_textview);
            publisher_textview = itemView.findViewById(R.id.publisher_textview);
            description_textview = itemView.findViewById(R.id.description_textview);
            comments_textview = itemView.findViewById(R.id.viewComments_textview);


        }
    }


    private void updatePublisherInfo(final String userId, final TextView username_textview,
                                     final TextView publisher_textview, final ImageView profile_imageview) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username_textview.setText(user.getUsername());
                publisher_textview.setText(user.getUsername());
                Glide.with(mContext).load(user.getImageUrl()).into(profile_imageview);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update like button whether this post liked or not && update number of likes
    private void update_LikeButton_NoLikes(String postId, final ImageView like_imageview) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    like_imageview.setImageResource(R.drawable.ic_liked);
                    like_imageview.setTag("liked");
                } else {
                    like_imageview.setImageResource(R.drawable.ic_like);
                    like_imageview.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateNumberOfLikes(String postId, final TextView likes_textview) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes_textview.setText(dataSnapshot.getChildrenCount() +" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
