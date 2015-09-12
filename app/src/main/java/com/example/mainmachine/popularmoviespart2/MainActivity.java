package com.example.mainmachine.popularmoviespart2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements MovieFragment.Callback{
    public boolean twoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null){
            twoPane = true;

            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailActivity.MovieDetailActivityFragment())
                        .commit();
            }
        }
        else{
            twoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onItemSelected(Movie m){
        if(twoPane) {
            Bundle args = new Bundle();
            args.putParcelable("selected_movie",m);
            MovieDetailActivity.MovieDetailActivityFragment movieDetail = new MovieDetailActivity.MovieDetailActivityFragment();
            movieDetail.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, movieDetail)
                    .commit();
        }
        else{
            Intent detailIntent = new Intent(this, MovieDetailActivity.class);
            detailIntent.putExtra("selected_movie", m);
            startActivity(detailIntent);
        }

    }


}
