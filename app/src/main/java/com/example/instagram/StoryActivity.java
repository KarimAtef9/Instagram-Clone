package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.Model.Story;
import com.example.instagram.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;

    ImageView storyImage_iv, publisherImage_iv;
    TextView publisherUsername_tv;
    View prev_v, next_v;

    ArrayList<String> imagesIds;
    ArrayList<String> storiesIds;
    String userId;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long curTime = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < curTime - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        storiesProgressView = findViewById(R.id.stories_spv);
        storyImage_iv = findViewById(R.id.storyImage_iv);
        publisherImage_iv = findViewById(R.id.publisherImage_iv);
        publisherUsername_tv = findViewById(R.id.publisherUsername_tv);
        prev_v = findViewById(R.id.prev_v);
        next_v = findViewById(R.id.next_v);

        userId = getIntent().getStringExtra("userId");

        readStories(userId);
        updatePublisherInfo(userId);

        // get previous story
        prev_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        prev_v.setOnTouchListener(onTouchListener); // on touch hold up

        // get next story
        next_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        next_v.setOnTouchListener(onTouchListener); // on touch hold up

    }

    @Override
    public void onNext() {
        counter++;
        Glide.with(getApplicationContext()).load(imagesIds.get(counter)).into(storyImage_iv);
    }

    @Override
    public void onPrev() {
        if (counter == 0)
            return;
        counter--;
        Glide.with(getApplicationContext()).load(imagesIds.get(counter)).into(storyImage_iv);
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    // read stories of given user
    private void readStories(String userId) {
        imagesIds = new ArrayList<>();
        storiesIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imagesIds.clear();
                storiesIds.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    long curTime = System.currentTimeMillis();
                    if (curTime > story.getStartTime() && curTime < story.getEndTime()) {
                        imagesIds.add(story.getImageUrl());
                        storiesIds.add(story.getStoryId());
                    }
                }

                storiesProgressView.setStoriesCount(imagesIds.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                Glide.with(getApplicationContext()).load(imagesIds.get(counter)).into(storyImage_iv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update username and profile image of story publisher
    private void updatePublisherInfo(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                publisherUsername_tv.setText(user.getUsername());
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(publisherImage_iv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
