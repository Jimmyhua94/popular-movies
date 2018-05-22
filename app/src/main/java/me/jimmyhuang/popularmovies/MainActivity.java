package me.jimmyhuang.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.jimmyhuang.popularmovies.model.Movie;
import me.jimmyhuang.popularmovies.utility.NetworkUtil;
import me.jimmyhuang.popularmovies.utility.PosterAdapter;
import me.jimmyhuang.popularmovies.utility.PosterItemDecoration;

import static me.jimmyhuang.popularmovies.utility.JsonUtil.parseDiscoverJson;
import static me.jimmyhuang.popularmovies.utility.JsonUtil.parseDiscoverTotalPagesJson;

public class MainActivity extends AppCompatActivity {

    private final int NUM_POSTERS = 20;
    private RecyclerView mPosters;
    private PosterAdapter mAdapter;
    private final List<Movie> mMovies = new ArrayList<>();
    private int sortOrder;

    private int page = 1;
    private int totalPages = 0;

    private boolean loading = true;

    private Menu menu;

    private boolean failedPageAdd = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_sort_order).setTitle(getResources().getString(sortOrder));
        hasNetwork();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String msg = getResources().getString(R.string.already_sorted);
        // https://developer.android.com/guide/topics/ui/menus#groups
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_rated:
                int ratedId = R.string.rated;
                if (sortOrder != ratedId && hasNetwork()) {
                    sortOrder = ratedId;
                    menu.findItem(R.id.menu_sort_order).setTitle(getResources().getString(sortOrder));

                    page = 1;

                    URL ratedMovieUrl = NetworkUtil.buildDiscoverUrl(sortOrder, page);
                    new SwitchMoviePosterTask().execute(ratedMovieUrl);
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_popular:
                int popularId = R.string.popular;
                if (sortOrder != popularId && hasNetwork()) {
                    sortOrder = popularId;
                    menu.findItem(R.id.menu_sort_order).setTitle(getResources().getString(sortOrder));

                    page = 1;

                    URL popularMovieUrl = NetworkUtil.buildDiscoverUrl(sortOrder, page);
                    new SwitchMoviePosterTask().execute(popularMovieUrl);
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_refresh:
                if (hasNetwork()) {
                    if (failedPageAdd) {
                        page++;

                        URL popularMovieUrl = NetworkUtil.buildDiscoverUrl(sortOrder, page);
                        new MoviePosterTask().execute(popularMovieUrl);
                        Toast.makeText(this, getResources().getString(R.string.page_load), Toast.LENGTH_SHORT).show();
                    } else {
                        page = 1;

                        URL popularMovieUrl = NetworkUtil.buildDiscoverUrl(sortOrder, page);
                        new SwitchMoviePosterTask().execute(popularMovieUrl);
                    }
                }
                break;
            default: break;
        }

        return super.onOptionsItemSelected((item));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sortOrder = R.string.popular;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPosters = findViewById(R.id.poster_rv);

        PosterItemDecoration posterDecoration = new PosterItemDecoration(10);
        mPosters.addItemDecoration(posterDecoration);

        final GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        mPosters.setLayoutManager(layoutManager);

        // https://stackoverflow.com/questions/4142090/how-to-retrieve-the-dimensions-of-a-view/4406090#4406090
        mPosters.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutManager.setSpanCount(mPosters.getWidth() / dpToPx(150));
            }
        });

        mPosters.setItemViewCacheSize(NUM_POSTERS);

        if (hasNetwork()) {
            page = 1;

            URL popularMovieUrl = NetworkUtil.buildDiscoverUrl(sortOrder, page);
            new MoviePosterTask().execute(popularMovieUrl);
        }

        mAdapter = new PosterAdapter(mMovies);

        mPosters.setAdapter(mAdapter);

        mPosters.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if (!loading) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                        if (lastVisibleItem >= totalItemCount - visibleItemCount ) {
                            if (page + 1 <= totalPages && hasNetwork() && !failedPageAdd) {
                                page++;
                                URL popularMovieUrl = NetworkUtil.buildDiscoverUrl(sortOrder, page);
                                new MoviePosterTask().execute(popularMovieUrl);
//                                Toast.makeText(MainActivity.this, "Page: " + page, Toast.LENGTH_SHORT).show();
                            } else {
                                failedPageAdd = true;
                            }
                        }
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private class SwitchMoviePosterTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL movieApiUrl = urls[0];
            String results = null;
            try {
                loading = true;
                results = NetworkUtil.getResponseFromHttpUrl(movieApiUrl);
            } catch (IOException e) {
                loading = false;
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            loading = false;
            if (s != null && !s.equals("")) {
                failedPageAdd = false;
                if (totalPages == 0) totalPages = parseDiscoverTotalPagesJson(s);
                List<Movie> movieList = parseDiscoverJson(s);
                if (movieList != null) {
                    mMovies.clear();
                    mMovies.addAll(movieList);
                    mAdapter.notifyDataSetChanged();
                    mPosters.scrollToPosition(0);
                }
            }
        }

    }

    private class MoviePosterTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL movieApiUrl = urls[0];
            String results = null;
            try {
                loading = true;
                results = NetworkUtil.getResponseFromHttpUrl(movieApiUrl);
            } catch (IOException e) {
                loading = false;
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            loading = false;
            if (s != null && !s.equals("")) {
                failedPageAdd = false;
                if (totalPages == 0) totalPages = parseDiscoverTotalPagesJson(s);
                List<Movie> movieList = parseDiscoverJson(s);
                if (movieList != null) {
                    mMovies.addAll(movieList);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    // https://stackoverflow.com/questions/29664993/how-to-convert-dp-px-sp-among-each-other-especially-dp-and-sp
    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.getResources().getDisplayMetrics());
    }

    // https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
    private boolean hasNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork;
        Boolean hasConnection = false;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
            hasConnection = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }

        if (!hasConnection) {
            Toast.makeText(MainActivity.this,getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
            if (menu != null) {
                menu.findItem(R.id.menu_refresh).setVisible(true);
            }
        } else {
            if (menu != null) {
                menu.findItem(R.id.menu_refresh).setVisible(false);
            }
        }

        return hasConnection;
    }


}
