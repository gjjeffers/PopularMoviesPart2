package com.example.mainmachine.popularmoviespart2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class MovieDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieDetailActivityFragment())
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

        public MovieDetailActivityFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
            Intent intent = getActivity().getIntent();
            if(intent == null || !intent.hasExtra("selected_movie")){
                return rootView;
            }
            Movie m = intent.getParcelableExtra("selected_movie");
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
            return rootView;
        }


    }

}






