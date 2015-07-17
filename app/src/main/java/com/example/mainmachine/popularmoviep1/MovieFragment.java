package com.example.mainmachine.popularmoviep1;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class MovieFragment extends Fragment{

    public MovieFragment() {
        // Required empty public constructor
    }

    MovieAdapter movieAdapter;
    String[] sortOptions;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_movie, container, false);

        GridView gv = (GridView)rootView.findViewById(R.id.gvMovieList);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(),MovieDetailActivity.class);
                detailIntent.putExtra("selected_movie", movieAdapter.getItem(position));
                startActivity(detailIntent);
            }
        });
        movieAdapter = new MovieAdapter(getActivity(),R.layout.movie_layout, new ArrayList<Movie>());

        gv.setAdapter(movieAdapter);

        sortOptions = getResources().getStringArray(R.array.sort_options);
        Spinner sortSpinner = (Spinner)rootView.findViewById(R.id.spinner);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MovieFetcher request = new MovieFetcher();
                request.execute(sortOptions[position].toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }


        });



        return rootView;
    }



    class MovieFetcher extends AsyncTask<String,Void,Movie[]> {

        private Movie[] movieResponse;
        @Override
        protected Movie[] doInBackground(String... params) {
            Uri.Builder movieDbUri = new Uri.Builder();

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JsonResponse = null;
            String orderBy = params[0].equals("Most Popular")?"popularity.desc":"vote_average.desc";


            movieDbUri.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("sort_by", orderBy)
                    .appendQueryParameter("api_key", getString(R.string.api_key));
            try {
                URL url = new URL(movieDbUri.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                //if there is no response from the server
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                //if response from server is empty
                if(buffer.length() == 0){
                    return null;
                }

                JsonResponse = buffer.toString();
                try{
                    movieResponse = parseMovieResponse(JsonResponse);
                }
                catch(JSONException j){
                    Log.e("Response Parsing Error ", j.getMessage());
                }
            } catch (Exception e) {
                Toast toast = Toast.makeText(getActivity(), "Error: " + e.getMessage(),Toast.LENGTH_LONG);
                toast.show();
            }
            finally{
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    }
                    catch(final IOException e){
                        Log.e("Error Closing Stream ", e.getMessage());
                    }
                }
            }

            return movieResponse;
        }

        protected void onPostExecute(Movie[] m){
            movieAdapter.clear();
            for(Movie tempM: m){
                movieAdapter.add(tempM);
                movieAdapter.notifyDataSetChanged();
            }
        }

        private Movie[] parseMovieResponse(String json) throws JSONException{
            JSONObject movieListJson = new JSONObject(json);
            JSONArray movieListArray = movieListJson.getJSONArray("results");
            ArrayList<Movie> returnMovie = new ArrayList<>();
            for(int i = 0; i < movieListArray.length();i++){
                Movie tempMovie = new Movie();
                JSONObject tempJson= movieListArray.getJSONObject(i);
                tempMovie.name = tempJson.getString("original_title");
                tempMovie.id = tempJson.getString("id");
                if(!tempJson.getString("poster_path").equals("null")) {
                    tempMovie.posterUri = "http://image.tmdb.org/t/p/w185" + tempJson.getString("poster_path");
                }
                tempMovie.overview = tempJson.getString("overview");
                tempMovie.voteAvg = tempJson.getString("vote_average");
                tempMovie.releaseDate = tempJson.getString("release_date");
                returnMovie.add(tempMovie);
            }
            Movie[] returnArray = new Movie[returnMovie.size()];
            return returnMovie.toArray(returnArray);
        }
    }

    public class MovieAdapter extends ArrayAdapter<Movie>{
        Context context;
        int layoutResourceId;
        ArrayList<Movie> movies = null;

        public MovieAdapter(Context c, int layoutRId, ArrayList<Movie> m){
            super(c, layoutRId, m);
            context = c;
            layoutResourceId = layoutRId;
            movies = m;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View cell = convertView;
            MovieHolder tempM = null;
            if(cell == null){
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                cell = inflater.inflate(layoutResourceId, parent, false);
                tempM = new MovieHolder();
                tempM.imgPoster = (ImageView)cell.findViewById(R.id.imgPoster);
                tempM.txtTitle = (TextView)cell.findViewById(R.id.txtTitle);
                cell.setTag(tempM);
            }
            else{
                tempM = (MovieHolder)cell.getTag();
            }
            Movie movie = movies.get(position);
            tempM.txtTitle.setText(movie.name);
            if(movie.posterUri != null) {
                Picasso.with(context).load(movie.posterUri).into(tempM.imgPoster);
            }
            else{
                Picasso.with(context).load(R.drawable.placeholdersmall).into(tempM.imgPoster);
            }

            return cell;
        }

        private class MovieHolder {
            ImageView imgPoster;
            TextView txtTitle;
        }
    }





}