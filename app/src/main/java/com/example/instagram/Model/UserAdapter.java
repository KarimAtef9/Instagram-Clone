package com.example.instagram.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends ArrayAdapter<User> {
    private ArrayList<User> userList;
    // required for getting image
    private Context mContext;

    private FirebaseUser firebaseUser;


    public UserAdapter(Context context, ArrayList<User> userList) {
        super(context, 0, userList);
        this.mContext = context;
        this.userList = userList;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.user_item, parent, false);
        }

        TextView username = listItemView.findViewById(R.id.username_textview);
        TextView fullName = listItemView.findViewById(R.id.fullname_textview);
        CircleImageView profileImage = listItemView.findViewById(R.id.profile_image);
        final Button followBtn = listItemView.findViewById(R.id.follow_btn);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = userList.get(position);

        followBtn.setVisibility(View.VISIBLE);
        username.setText(user.getUsername());
        fullName.setText(user.getFullName());
        Glide.with(mContext).load(user.getImageUrl()).into(profileImage);

        // setting text on follow button
        setFollowButtonText(user.getId(), followBtn);

        if (user.getId().equals(firebaseUser.getUid())) {
            followBtn.setVisibility(View.GONE);
        }

        // to open clicked user profile
        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileId", user.getId());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        // follow and unfollow depending on current case
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // not friends
                if (followBtn.getText().toString().equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("Following")
                            .child(user.getId()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getId()).child("Followers")
                            .child(firebaseUser.getUid()).setValue(true);

                    addNotification(user.getId());
                } else {
                    // already friends, then un-friend
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("Following")
                            .child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getId()).child("Followers")
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });


        return listItemView;
    }


    // setting text on button (follow or following)
    private void setFollowButtonText(final String userId, final Button followBtn) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("Following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userId).exists()) {
                    followBtn.setText("Following");
                } else {
                    followBtn.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification(String otherUser) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(otherUser);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("note", "started following you");
        hashMap.put("posterId", "");
        hashMap.put("postId", "");
        hashMap.put("isPost", false);

        reference.push().setValue(hashMap);
    }

}
