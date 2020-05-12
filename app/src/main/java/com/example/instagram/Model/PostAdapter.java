package com.example.instagram.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.CommentActivity;
import com.example.instagram.Fragment.PostDetailsFragment;
import com.example.instagram.Fragment.ProfileFragment;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private ArrayList<Post> postsList;
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

        // update below image items (likes comments buttons...)
        update_LikeButton_NoLikes(post.getPostId(), holder.like_imageview, holder.likes_textview);
        updateNumberOfComments(post.getPostId(), holder.comments_textview);
        updateSaveButton(post.getPostId(), post.getPublisher(), holder.save_imageview);

        // like & unlike post
        holder.like_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // like post
                if (holder.like_imageview.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);
                    addNotification(post.getPublisher(), post.getPostId());
                } else {
                    // remove like
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        // go to comments through comment button or view comments text
        holder.comment_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());
                mContext.startActivity(intent);

            }
        });
        holder.comments_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());
                mContext.startActivity(intent);

            }
        });

        // save and un-save posts
        holder.save_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.save_imageview.getTag().equals("save")) {
                    // save post in database as Saves - my id - publisher id - post id
                    FirebaseDatabase.getInstance().getReference("Saves")
                            .child(firebaseUser.getUid()).child(post.getPostId())
                            .child(post.getPublisher()).setValue(System.currentTimeMillis());
                } else {
                    FirebaseDatabase.getInstance().getReference("Saves")
                            .child(firebaseUser.getUid()).child(post.getPostId())
                            .child(post.getPublisher()).removeValue();
                }
            }
        });

        // open profile by image or username
        holder.profile_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileId", post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });
        holder.username_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileId", post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        // open post when click on image
        holder.postImage_imageview.setOnClickListener(new View.OnClickListener() {
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


    private void addNotification(String posterId, String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(posterId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("note", "liked your post");
        hashMap.put("posterId", posterId);
        hashMap.put("postId", postId);
        hashMap.put("isPost", true);

        reference.push().setValue(hashMap);
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
    private void update_LikeButton_NoLikes(String postId,
                                           final ImageView like_imageview, final TextView likes_textview) {
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
                long likesCount = dataSnapshot.getChildrenCount();
                if (likesCount == 1)
                    likes_textview.setText(likesCount +" like");
                else
                    likes_textview.setText(likesCount +" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // update number of comments
    private void updateNumberOfComments(String postId, final TextView comments_textview) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long commentsCount = dataSnapshot.getChildrenCount();
                if (commentsCount == 0) {
                    comments_textview.setVisibility(View.GONE);
                } else if (commentsCount == 1) {
                    comments_textview.setVisibility(View.VISIBLE);
                    comments_textview.setText("View " + commentsCount + " comment");
                } else {
                    comments_textview.setVisibility(View.VISIBLE);
                    comments_textview.setText("View all " + commentsCount + " comments");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update save button (image view)
    private void updateSaveButton(final String postId, final String publisherId, final ImageView save_iv) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Saves").child(firebaseUser.getUid()).child(postId).child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // already saved
                    save_iv.setImageResource(R.drawable.ic_save_black);
                    save_iv.setTag("saved");
                } else {
                    // press to save
                    save_iv.setImageResource(R.drawable.ic_saved);
                    save_iv.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
