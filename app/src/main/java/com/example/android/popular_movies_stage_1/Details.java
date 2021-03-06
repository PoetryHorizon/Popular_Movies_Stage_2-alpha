package com.example.android.popular_movies_stage_1;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popular_movies_stage_1.data.AppExecutors;
import com.example.android.popular_movies_stage_1.data.FavoritesDbSingle;
import com.example.android.popular_movies_stage_1.data.MovieDb;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

// Followed the walkthrough of @Gill AND from Slack.  His video on Youtube helped me follow the direction
// to implement the working code.

// got help from @ Napu Taitano (AND Graduate) with the Reviews task

// Code highly inspired and guided by watching Jeriel NG Udacity's Guide
// on Youtube!
// Got Guidance and Direction from @maluta [AND Graduate, Los Angeles, CA] from Slack!!

public class Details extends AppCompatActivity {

    private LinearLayout mTrailerList;
    private TextView noReviews;
    private Button mFavoriteButton;
    private String mId;

    private final String PARAM_RESULTS = "results";
    private final String PARAM_KEY = "key";
    private final String PARAM_NAME = "name";

    private final String PARAM_AUTHOR = "author";
    private final String PARAM_CONTENT = "content";

    private String[] mTrailerKeys;
    private String[] mTrailerNames;
    private String[] mReviewAuthors;
    private String[] mReviewContent;

    private ScrollView mScrollView;

    private int reviewCounter;

    private static final String BASE_URL = "https://image.tmdb.org/t/p/w185";
    private final String TRAILER_BASE_URL = "http://youtube.com/watch?v=";

    // Favorites Variables Ver.1
    static MovieDb movieDb;
    boolean favoriteExists;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent intent = getIntent();
        movie = intent.getParcelableExtra("movie");
        TextView title = findViewById(R.id.title_text_view);
        title.setText(movie.getTitle());
        ImageView poster = findViewById(R.id.movie_image_detail);
        String posterPath = movie.getPoster();
        Picasso.with(this).load(BASE_URL + posterPath).into(poster);
        TextView date = findViewById(R.id.year_text_view);
        String releaseYear = movie.getDate();
        if (releaseYear.length() > 4) releaseYear = releaseYear.substring(0, 4);
        date.setText(releaseYear);
        TextView vote = findViewById(R.id.rating_text_view);
        String voteAverage = movie.getVote();
        vote.setText(voteAverage);
        TextView overview = findViewById(R.id.summary_text_view);
        overview.setText(movie.getOverview());
        mId = movie.getID();
        reviewCounter = 0;
        mFavoriteButton = findViewById(R.id.save_button);
        mTrailerList = findViewById(R.id.trailer_list);
        mScrollView = findViewById(R.id.mScrollView);

        // TODO: Figure out why Favorite Button is showing saved before saving it

        if (savedInstanceState == null) {
            favoriteExists = intent.getBooleanExtra("isFavorite", false);
        }

        setFavoriteButton();

        new FetchReviewsTask().execute();
        new FetchTrailersTask().execute();


        // Allocates the favorites movie data into a single instance

        movieDb = FavoritesDbSingle.getInstance(this);



        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!favoriteExists) {
                    favoriteExists = true;
                    addMovie();
                    mFavoriteButton.setText(getString(R.string.favorite_marked));
                    mFavoriteButton.setTextColor(getResources().getColor(R.color.colorAccent));
                    Toast.makeText(Details.this,
                           "Saved to Favorites!", Toast.LENGTH_SHORT).show();
                } else {
                    favoriteExists = false;
                    deleteMovie();
                    mFavoriteButton.setText(getString(R.string.not_added_to_favorites));
                    mFavoriteButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    Toast.makeText(Details.this,
                            "Removed from Favorites!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setFavoriteButton() {
        if (favoriteExists){
            mFavoriteButton.setText(getString(R.string.favorite_marked));
            mFavoriteButton.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            mFavoriteButton.setText(getString(R.string.not_added_to_favorites));
            mFavoriteButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }


    // ASYNC Task for Trailers

    public class FetchTrailersTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL trailersRequestUrl = NetworkUtils.buildTrailerUrl(mId);
                return NetworkUtils.getResponseFromHttpUrl(trailersRequestUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            extractTrailers(result);
            loadTrailersUI();
        }
    }

    public void extractTrailers(String trailersResponse) {
        try {
            JSONObject jsonTrailersObject = new JSONObject(trailersResponse);
            JSONArray trailersResults = jsonTrailersObject.getJSONArray(PARAM_RESULTS);
            mTrailerKeys = new String[trailersResults.length()];
            mTrailerNames = new String[trailersResults.length()];
            for (int i = 0; i < trailersResults.length(); i++)
            {
                mTrailerKeys[i] = trailersResults.getJSONObject(i).optString(PARAM_KEY);
                mTrailerNames[i] = trailersResults.getJSONObject(i).optString(PARAM_NAME);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadTrailersUI() {
        if (mTrailerKeys.length == 0) {
            TextView noTrailers = new TextView(this);
            noTrailers.setText(R.string.no_trailers);
            noTrailers.setPadding(0, 30, 0, 0);
            noTrailers.setTextSize(15);
            noTrailers.setTextColor(getColor(R.color.noReviews));
            mTrailerList.addView(noTrailers);
        }
        else {
            for (int i = 0; i < mTrailerKeys.length; i++) {
                Button trailerItem = new Button(this);
                trailerItem.setText(mTrailerNames[i]);
                trailerItem.setPadding(0, 50, 0, 30);
                trailerItem.setTextSize(15);
                final String trailerUrl = TRAILER_BASE_URL + mTrailerKeys[i];
                trailerItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Referenced from Udacity's Course
                        Uri youtubeLink = Uri.parse(trailerUrl);
                        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, youtubeLink);
                        if (youtubeIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(youtubeIntent);
                        }
                    }
                });
                mTrailerList.addView(trailerItem);
            }
        }
    }

    // ASYNC Task for Reviews

    public class FetchReviewsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL reviewsRequestUrl = NetworkUtils.buildReviewUrl(mId);
                return NetworkUtils.getResponseFromHttpUrl(reviewsRequestUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            extractReviews(result);
            loadReviewUI();
        }
    }

    // Get JSON Data for Reviews from Movie DB

    public void extractReviews (String reviewResponse){
        try {
            JSONObject jsonReviewObject = new JSONObject(reviewResponse);
            JSONArray reviewResults = jsonReviewObject.getJSONArray(PARAM_RESULTS);
            mReviewAuthors = new String[reviewResults.length()];
            mReviewContent = new String[reviewResults.length()];
            for (int i = 0; i < reviewResults.length(); i++)
            {
                mReviewAuthors[i] = reviewResults.getJSONObject(i).optString(PARAM_AUTHOR);
                mReviewContent [i] = reviewResults.getJSONObject(i).optString(PARAM_CONTENT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Load User Interface to display in Details Activity
    public void loadReviewUI() {
        if (mReviewContent.length == 0) {
            findViewById(R.id.author).setVisibility(View.GONE);
            findViewById(R.id.reviews_text_view).setVisibility(View.GONE);
            findViewById(R.id.next_button).setVisibility(View.GONE);

            //TextView noReviews = new TextView(this);
            noReviews = findViewById(R.id.no_reviews_tv);
            noReviews.setText(R.string.no_reviews);
            noReviews.setTextSize(15);
            noReviews.setTextColor(getColor(R.color.noReviews));
        } else {
            if (mReviewContent.length == 1) {
                findViewById(R.id.next_button).setVisibility(View.GONE);
            }
            String authorHeader = mReviewAuthors[reviewCounter] + ":";
            ((TextView) findViewById(R.id.author)).setText(authorHeader);
            ((TextView) findViewById(R.id.reviews_text_view)).setText(mReviewContent[reviewCounter]);
            findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (reviewCounter < mReviewContent.length - 1) {
                        reviewCounter++;
                    } else {
                        reviewCounter = 0;
                    }
                    loadReviewUI();
                }
            });
        }
    }

    private void addMovie(){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                movieDb.movieDao().insertMovie(movie);
            }
        });}

        private void deleteMovie(){
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    movieDb.movieDao().deleteMovie(movie);
                }
            });
        }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("SCROLL_POSITION", new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()});
        outState.putBoolean("isFavorite", favoriteExists);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int[] position = savedInstanceState.getIntArray("SCROLL_POSITION");
        if(position != null) {
            mScrollView.postDelayed(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(position[0], position[1]);
                }
            }, 300);
        }

        favoriteExists = savedInstanceState.getBoolean("isFavorite", favoriteExists);
        setFavoriteButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFavoriteButton();
    }
}