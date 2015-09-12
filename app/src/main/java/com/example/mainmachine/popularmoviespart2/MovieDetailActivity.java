package com.example.mainmachine.popularmoviespart2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.FavoriteEntry;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;


public class MovieDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new MovieDetailActivityFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class MovieDetailActivityFragment extends Fragment {

        Movie m;
        ArrayAdapter<String> trailerAdapter;
        ArrayAdapter<String> reviewAdapter;

        public MovieDetailActivityFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
            Intent intent = getActivity().getIntent();
            if(intent == null){
                return rootView;
            }
            if(intent.hasExtra("selected_movie")) {
                m = intent.getParcelableExtra("selected_movie");
            }
            else if(getArguments() != null){
                m = getArguments().getParcelable("selected_movie");
            }
            else{
                return rootView;
            }
            ImageView poster = (ImageView)rootView.findViewById(R.id.imgDetailPoster);
            if(m.posterUri != null) {
                Picasso.with(rootView.getContext()).load(m.posterUri.replace("w185", "w780")).into(poster);
            }
            else{
                Picasso.with(rootView.getContext()).load(R.drawable.placeholderlarge).into(poster);
            }
            TextView title = (TextView)rootView.findViewById(R.id.txtDetailTitle);
            title.setText(m.name);
            TextView summary = (TextView)rootView.findViewById(R.id.txtDetailSummary);
            summary.setText(m.overview);
            TextView rating = (TextView)rootView.findViewById(R.id.txtDetailRating);
            rating.setText(m.voteAvg + "/10");
            TextView release = (TextView)rootView.findViewById(R.id.txtDetailRelease);
            release.setText(m.releaseDate);
            
            trailerAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,new ArrayList<String>());
            ListView trailerView = (ListView)rootView.findViewById(R.id.lstDetailTrailer);
            trailerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String trailerKey = m.trailerId[position];
                    Intent ytIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=" + trailerKey));
                    startActivity(ytIntent);
                }
            });
            trailerView.setAdapter(trailerAdapter);
            TrailerFetcher tf = new TrailerFetcher();
            tf.execute(m.id);

            ListView reviewView = (ListView)rootView.findViewById(R.id.lstDetailReview);
            reviewAdapter = getReviewAdapter();
            reviewView.setAdapter(reviewAdapter);
            ReviewFetcher rf = new ReviewFetcher();
            rf.execute(m.id);


            CheckBox favoriteCheck = (CheckBox)rootView.findViewById(R.id.favbutton);
            Cursor favCursor = getActivity().getContentResolver().query(FavoriteEntry.buildFavorite(m.id),null,null,null,null);
            if(favCursor.getCount() > 0){
                favoriteCheck.setChecked(true);
            }
            favCursor.close();
            favoriteCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        //add to favorites db
                        ContentValues movieVals = new ContentValues();
                        movieVals.put(FavoriteEntry.COLUMN_API_ID, m.id);
                        movieVals.put(FavoriteEntry.COLUMN_TITLE, m.name);
                        movieVals.put(FavoriteEntry.COLUMN_SYN, m.overview);
                        movieVals.put(FavoriteEntry.COLUMN_RATING, m.voteAvg);
                        movieVals.put(FavoriteEntry.COLUMN_RELEASE_DATE, m.releaseDate);
                        movieVals.put(FavoriteEntry.COLUMN_POSTER, m.posterUri);

                        getActivity().getContentResolver().insert(FavoriteEntry.CONTENT_URI, movieVals);
                    } else
                        getActivity().getContentResolver().delete(FavoriteEntry.CONTENT_URI, null, new String[]{m.id});

                }
            });
            return rootView;
        }

        private ArrayAdapter<String> getReviewAdapter(){
            return new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_2, new ArrayList<String>()) {
                @Override
            public View getView(int position,View view,ViewGroup parent){
                    if(view == null){
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        view = inflater.inflate(android.R.layout.simple_list_item_2,parent, false);
                    }

                    TextView text1 = (TextView)view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView)view.findViewById(android.R.id.text2);
                    text1.setText(m.reviewer[position]);
                    text2.setText(m.review[position]);

                    return view;
                }
            };
        }

        class ReviewFetcher extends AsyncTask<String,Void,Hashtable<String,String[]>> {

            private Hashtable<String,String[]> movieResponse;

            @Override
            protected Hashtable<String,String[]> doInBackground(String... params) {
                Uri.Builder movieDbUri = new Uri.Builder();

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String JsonResponse;
                String movieKey = params[0];


                movieDbUri.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(movieKey)
                        .appendPath("reviews")
                        .appendQueryParameter("api_key", getString(R.string.api_key));
                try {
                    URL url = new URL(movieDbUri.build().toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();

                    //if there is no response from the server
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    //if response from server is empty
                    if (buffer.length() == 0) {
                        return null;
                    }

                    JsonResponse = buffer.toString();
                    try{
                        movieResponse = parseTrailerResponse(JsonResponse);
                    }
                    catch(JSONException j){
                        Log.e("Response Parsing Error ", j.getMessage());
                    }
                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("Error Closing Stream ", e.getMessage());
                        }
                    }
                }

                return movieResponse;
            }
            protected void onPostExecute(Hashtable<String,String[]> ht){
                reviewAdapter.clear();
                ArrayList<String> reviewNames = new ArrayList<>();
                ArrayList<String> reviewContent = new ArrayList<>();
                for(String tempKey: ht.keySet()){
                    reviewAdapter.add(tempKey);
                    reviewNames.add(ht.get(tempKey)[0]);
                    reviewContent.add(ht.get(tempKey)[1]);
                }
                m.reviewer = reviewNames.toArray(new String[reviewNames.size()]);
                m.review = reviewContent.toArray(new String[reviewContent.size()]);
                reviewAdapter.notifyDataSetChanged();
            }

            private Hashtable<String,String[]> parseTrailerResponse(String json) throws JSONException {
                JSONObject trailerListJson = new JSONObject(json);
                JSONArray trailerListArray = trailerListJson.getJSONArray("results");
                Hashtable<String,String[]> returnTrailer = new Hashtable<>();
                for(int i = 0; i < trailerListArray.length();i++){
                    JSONObject tempJson= trailerListArray.getJSONObject(i);
                        returnTrailer.put(tempJson.getString("id"), new String[]{tempJson.getString("author"),tempJson.getString("content")});

                }
                return returnTrailer;
            }

        }

        class TrailerFetcher extends AsyncTask<String,Void,Hashtable<String,String>> {

            private Hashtable<String,String> movieResponse;

            @Override
            protected Hashtable<String,String> doInBackground(String... params) {
                Uri.Builder movieDbUri = new Uri.Builder();

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String JsonResponse;
                String movieKey = params[0];


                movieDbUri.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(movieKey)
                        .appendPath("videos")
                        .appendQueryParameter("api_key", getString(R.string.api_key));
                try {
                    URL url = new URL(movieDbUri.build().toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();

                    //if there is no response from the server
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    //if response from server is empty
                    if (buffer.length() == 0) {
                        return null;
                    }

                    JsonResponse = buffer.toString();
                    try{
                        movieResponse = parseTrailerResponse(JsonResponse);
                    }
                    catch(JSONException j){
                        Log.e("Response Parsing Error ", j.getMessage());
                    }
                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("Error Closing Stream ", e.getMessage());
                        }
                    }
                }

                return movieResponse;
            }
            protected void onPostExecute(Hashtable<String,String> ht){
                trailerAdapter.clear();
                ArrayList<String> trailerNames = new ArrayList<>();
                ArrayList<String> trailerKeys = new ArrayList<>();
                for(String tempKey: ht.keySet()){
                    trailerAdapter.add(tempKey);
                    trailerAdapter.notifyDataSetChanged();
                    trailerNames.add(tempKey);
                    trailerKeys.add(ht.get(tempKey));

                }
                m.trailer = trailerNames.toArray(new String[trailerNames.size()]);
                m.trailerId = trailerKeys.toArray(new String[trailerKeys.size()]);
            }

            private Hashtable<String,String> parseTrailerResponse(String json) throws JSONException {
                JSONObject trailerListJson = new JSONObject(json);
                JSONArray trailerListArray = trailerListJson.getJSONArray("results");
                Hashtable<String,String> returnTrailer = new Hashtable<>();
                for(int i = 0; i < trailerListArray.length();i++){
                    JSONObject tempJson= trailerListArray.getJSONObject(i);
                    if(tempJson.getString("site").compareToIgnoreCase("YouTube") == 0) {
                        returnTrailer.put(tempJson.getString("name"), tempJson.getString("key"));
                    }
                }
                return returnTrailer;
            }
        }




    }

}






