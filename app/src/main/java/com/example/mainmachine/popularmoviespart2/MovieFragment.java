package com.example.mainmachine.popularmoviespart2;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    MovieAdapter movieAdapter;
    private static final int LOADER_ID = 0;

    protected static final String[] MOVIE_COLUMNS = {
            FavoriteEntry.COLUMN_API_ID,
            FavoriteEntry.COLUMN_TITLE,
            FavoriteEntry.COLUMN_SYN,
            FavoriteEntry.COLUMN_RATING,
            FavoriteEntry.COLUMN_RELEASE_DATE,
            FavoriteEntry.COLUMN_POSTER
    };
    protected static int COL_API_ID = 1;
    protected static int COL_TITLE = 2;
    protected static int COL_SYN = 3;
    protected static int COL_RATING = 4;
    protected static int COL_RELEASE_DATE = 5;
    protected static int COL_POSTER = 6;

    protected ArrayList<Movie> favoriteMovies;

    Callback mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("CurrentMovies", movieAdapter.movies);
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
                Movie selectedMovie =  movieAdapter.getItem(position);
                mCallback.onItemSelected(selectedMovie);
            }
        });

        movieAdapter = new MovieAdapter(getActivity(),R.layout.movie_layout, new ArrayList<Movie>());
        getLoaderManager().initLoader(LOADER_ID, savedInstanceState, this);

        if(savedInstanceState == null){
            updateMovieList(getString(R.string.menu_popular));
        }
        else {
            movieAdapter.setMovies((ArrayList<Movie>) savedInstanceState.get("CurrentMovies"));
        }
        gv.setAdapter(movieAdapter);


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callback)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallback = (Callback) activity;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
    }

    public interface Callback{
        public void onItemSelected(Movie m);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_popular) {
            updateMovieList(getString(R.string.menu_popular));
            return true;
        }
        else if(id == R.id.action_highest_rated){
            updateMovieList(getString(R.string.menu_highest_rated));
            return true;
        }
        else if(id == R.id.action_favorite){
            movieAdapter.clear();
            for(Movie tempM: favoriteMovies){
                movieAdapter.add(tempM);
                movieAdapter.notifyDataSetChanged();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateMovieList(String option){
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null && ni.isConnectedOrConnecting()) {
            MovieFetcher request = new MovieFetcher();
            request.execute(option);
        }
        else{
            Toast toast = Toast.makeText(getActivity(), "No Network Access",Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i,Bundle bundle){
        return new CursorLoader(getActivity(), FavoriteEntry.CONTENT_URI,MOVIE_COLUMNS,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor cursor){
        favoriteMovies = new ArrayList<>();
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()){
                Movie _m = new Movie();
                _m.id = cursor.getString(COL_API_ID);
                _m.name = cursor.getString(COL_TITLE);
                _m.posterUri = cursor.getString(COL_POSTER);
                _m.overview = cursor.getString(COL_SYN);
                _m.voteAvg = cursor.getString(COL_RATING);
                _m.releaseDate = cursor.getString(COL_RELEASE_DATE);
                favoriteMovies.add(_m);
                cursor.moveToNext();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){movieAdapter.setMovies(new ArrayList<Movie>());}

    class MovieFetcher extends AsyncTask<String,Void,Movie[]> {

        private Movie[] movieResponse;
        @Override
        protected Movie[] doInBackground(String... params) {
            Uri.Builder movieDbUri = new Uri.Builder();

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JsonResponse;
            String orderBy = params[0].equals(getString(R.string.menu_popular))?"popularity.desc":"vote_average.desc";


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
                StringBuilder buffer = new StringBuilder();

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
                Log.e("Error: ", e.getMessage());
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
                JSONObject tempJson= movieListArray.getJSONObject(i);
                Movie tempMovie = new Movie(tempJson);
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

        public void setMovies(ArrayList<Movie> m){
            movies.clear();
            movies.addAll(m);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View cell = convertView;
            MovieHolder tempM;
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
