package com.example.quizzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionsActivity extends AppCompatActivity {


    public static final String FILE_NAME = "QUIZZER";
    public static final String KEY_NAME = "QUESTIONS";

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private TextView question, noIndicator;
    private FloatingActionButton bookmarksBtn;
    private LinearLayout optionsContainer;
    private Button optionA,optionB,optionC,optionD;
    private Button shareBtn, nextBtn;
    private List<QuestionModel> list;
    private int position = 0;
    private int score = 0;
    private String setId;
    private InterstitialAd mInterstitialAd;

    private int count = 0;
    private Dialog loadingDialog;

    private List<QuestionModel> bookmarksList;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    private int matchedQuestionPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        /////////// Loading Dialog


        loadingDialog = new Dialog(QuestionsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        //////////// Loading Dialog

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadAds();

        question = findViewById(R.id.question);
        noIndicator = findViewById(R.id.no_indicator);
        bookmarksBtn = findViewById(R.id.bookmarks_btn);
        optionsContainer = findViewById(R.id.options_container);
        shareBtn = findViewById(R.id.share_btn);
        nextBtn = findViewById(R.id.next_btn);
        optionA=findViewById(R.id.optionA);
        optionB=findViewById(R.id.optionB);
        optionC=findViewById(R.id.optionC);
        optionD=findViewById(R.id.optionD);


        preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();

        getBookmarks();

        bookmarksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modelMatch()) {
                    bookmarksList.remove(matchedQuestionPosition);
                    bookmarksBtn.setImageDrawable(getDrawable(R.drawable.bookmark_btn));
                    Toast.makeText(QuestionsActivity.this, "Removed from bookmarks", Toast.LENGTH_SHORT).show();

                } else {
                    bookmarksList.add(list.get(position));
                    bookmarksBtn.setImageDrawable(getDrawable(R.drawable.bookmark));
                    Toast.makeText(QuestionsActivity.this, "Added to bookmarks", Toast.LENGTH_SHORT).show();

                }
            }
        });

        setId = getIntent().getStringExtra("setId");

        list = new ArrayList<>();
        loadingDialog.show();
        myRef.child("SETS").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String id = dataSnapshot1.getKey();
                    String question = dataSnapshot1.child("question").getValue().toString();
                    String a = dataSnapshot1.child("optionA").getValue().toString();
                    String b = dataSnapshot1.child("optionB").getValue().toString();
                    String c = dataSnapshot1.child("optionC").getValue().toString();
                    String d = dataSnapshot1.child("optionD").getValue().toString();
                    String answer = dataSnapshot1.child("correctANS").getValue().toString();

                    list.add(new QuestionModel(id, question, a, b, c, d, answer, setId));

                }
                if (list.size() > 0) {
                    for (int i = 0; i < 4; i++) {
                        optionsContainer.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                checkAnswer((Button) view);
                            }
                        });

                        shareBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String body = "Q: " + list.get(position).getQuestion() + "\n" + "\n" +
                                        "  1. " + list.get(position).getA() + "\n" +
                                        "  2. " + list.get(position).getB() + "\n" +
                                        "  3. " + list.get(position).getC() + "\n" +
                                        "  4. " + list.get(position).getD();
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                //    shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Quizzer Challenge");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, body);
                                shareIntent.setPackage("com.whatsapp");
                                try {
                                    startActivity(shareIntent);
                                } catch (android.content.ActivityNotFoundException ex) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.whatsapp")));
                                }
                            }
                        });
                    }


                    playAnim(question, 0, list.get(position).getQuestion());
                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            nextBtn.setEnabled(false);
                            nextBtn.setAlpha(0.7f);
                            enableOption(true);
                            position++;
                            if (position == list.size()) {

                                if(mInterstitialAd.isLoaded()){
                                    mInterstitialAd.show();
                                    return;
                                }

                                Intent scoreIntent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                                scoreIntent.putExtra("score", score);
                                scoreIntent.putExtra("total", list.size());
                                startActivity(scoreIntent);
                                finish();
                                return;
                            }
                            count = 0;
                            playAnim(question, 0, list.get(position).getQuestion());
                        }
                    });

                } else {
                    finish();
                    Toast.makeText(QuestionsActivity.this, "No Questions found!!", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestionsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();

        storeBookmarks();
    }

    private void playAnim(final View view, final int value, final String data) {

          for (int i = 0; i < 4; i++) {
              optionsContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
          }

        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                String option = "";
                if (value == 0 && count < 4) {
                    if (count == 0) {
                        option = list.get(position).getA();
                    } else if (count == 1) {
                        option = list.get(position).getB();

                    } else if (count == 2) {
                        option = list.get(position).getC();

                    } else if (count == 3) {
                        option = list.get(position).getD();

                    }
                    playAnim(optionsContainer.getChildAt(count), 0, option);
                    count++;
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (value == 0) {
                    try {
                        ((TextView) view).setText(data);
                        noIndicator.setText(position + 1 + "/" + list.size());
                        if (modelMatch()) {
                            bookmarksBtn.setImageDrawable(getDrawable(R.drawable.bookmark));
                        } else {
                            bookmarksBtn.setImageDrawable(getDrawable(R.drawable.bookmark_btn));
                        }

                    } catch (ClassCastException ex) {
                        ((Button) view).setText(data);

                    }
                    view.setTag(data);
                    playAnim(view, 1, data);
                }else{
                    enableOption(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }


    private void checkAnswer(Button selectedOption) {
        enableOption(false);
        nextBtn.setEnabled(true);
        nextBtn.setAlpha(1);
        if (selectedOption.getText().toString().equals(list.get(position).getAnswer())) {
            //correct
            score++;
            selectedOption.setBackground(getResources().getDrawable(R.drawable.correct_option));
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            selectedOption.setTextColor(getResources().getColor(R.color.colorAccent));

        } else {
            //incorrect
            selectedOption.setBackground(getResources().getDrawable(R.drawable.correct_option));
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
            selectedOption.setTextColor(getResources().getColor(R.color.colorAccent));
            Button correctOption = (Button) optionsContainer.findViewWithTag(list.get(position).getAnswer());
            correctOption.setBackground(getResources().getDrawable(R.drawable.correct_option));
            correctOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            correctOption.setTextColor(getResources().getColor(R.color.colorAccent));

        }

    }

    private void enableOption(boolean enable) {
        for (int i = 0; i < 4; i++) {
            optionsContainer.getChildAt(i).setEnabled(enable);
            if (enable) {
                optionsContainer.getChildAt(i).setBackground(getResources().getDrawable(R.drawable.rounded_borders));
                optionsContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
                optionA.setTextColor(Color.parseColor("#000000"));
                optionB.setTextColor(Color.parseColor("#000000"));
                optionC.setTextColor(Color.parseColor("#000000"));
                optionD.setTextColor(Color.parseColor("#000000"));

            }
        }
    }

    private void getBookmarks() {

        String json = preferences.getString(KEY_NAME, "");

        Type type = new TypeToken<List<QuestionModel>>() {}.getType();

        bookmarksList = gson.fromJson(json, type);

        if (bookmarksList == null) {
            bookmarksList = new ArrayList<>();
        }

    }

    private boolean modelMatch() {
        boolean matched = false;
        int i = 0;
        for (QuestionModel model : bookmarksList) {
            if (model.getQuestion().equals(bookmarksList.get(position).getQuestion())
                    && model.getA().equals(bookmarksList.get(position).getAnswer())
                    && model.getSetId().equals(bookmarksList.get(position).getSetId())) {
                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return matched;
    }

    private void storeBookmarks() {

        String json = gson.toJson(bookmarksList);

        editor.putString(KEY_NAME, json);
        editor.commit();
    }

    private void loadAds() {

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener(){

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                Intent scoreIntent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                scoreIntent.putExtra("score", score);
                scoreIntent.putExtra("total", list.size());
                startActivity(scoreIntent);
                finish();
                return;
            }
        });
    }

}
