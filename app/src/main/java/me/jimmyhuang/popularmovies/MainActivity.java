package me.jimmyhuang.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.jimmyhuang.popularmovies.data.FavoritesContract;
import me.jimmyhuang.popularmovies.model.Movie;
import me.jimmyhuang.popularmovies.utility.NetworkUtil;
import me.jimmyhuang.popularmovies.utility.MainAdapter;
import me.jimmyhuang.popularmovies.utility.PosterItemDecoration;

import static me.jimmyhuang.popularmovies.utility.JsonUtil.parseDiscoverJson;
import static me.jimmyhuang.popularmovies.utility.JsonUtil.parseDiscoverTotalPagesJson;

public class MainActivity extends AppCompatActivity {

    private static final String SORTING_ORDER = "sorting";

    private static final int DISCOVER_MOVIE_LOADER = 20;
    private static final int SWITCH_MOVIE_LOADER = 21;
    private static final int FAVORITES_MOVIE_LOADER = 22;

    private static final String DISCOVER_URL_EXTRA = "discoverExtra";
    private static final String SWITCH_URL_EXTRA = "switchExtra";

    private final int NUM_POSTERS = 20;
    private final int POSTER_SPACING = 10;
    private final int POSTER_SPAN = 1;  // Value doesn't matter, gets auto detected

    private RecyclerView mPosters;
    private MainAdapter mAdapter;
    private final List<Movie> mMovies = new ArrayList<>();
    private int mSortOrder;
    private int mPrevSortOrder;

    private int mPage = 1;
    private int mTotalPages = 0;

    private boolean mLoading = true;
    private boolean mFailedPageAdd = false;

    private Menu mMenu;

    private Context mContext;

    private LoaderManager mLoaderManager;

    private LoaderManager.LoaderCallbacks<String> discoverLoaderListener = new LoaderManager.LoaderCallbacks<String>() {
        // https://stackoverflow.com/questions/10524667/android-asynctaskloader-doesnt-start-loadinbackground
        @NonNull
        @Override
        public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
            return new AsyncTaskLoader<String>(mContext) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (args == null) {
                        return;
                    }
                    if (takeContentChanged()) {
                        forceLoad();
                    }
                }
                @Override
                public String loadInBackground() {
                    String discoverUrlString = args.getString(DISCOVER_URL_EXTRA);
                    if (discoverUrlString == null || TextUtils.isEmpty(discoverUrlString)) {
                        return null;
                    }
                    try {
                        mLoading = true;
                        URL discoveryUrl = new URL(discoverUrlString);
                        return NetworkUtil.getResponseFromHttpUrl(discoveryUrl);
                    } catch (IOException e) {
                        mLoading = false;
                        e.printStackTrace();
                        return null;
                    }
                }
                @Override
                protected void onStopLoading() {
                    cancelLoad();
                }
            };
        }

        @Override
        public void onLoadFinished(@NonNull Loader<String> loader, String data) {
            mLoading = false;
            if (data != null && !data.equals("")) {
                mFailedPageAdd = false;
                if (mTotalPages == 0) mTotalPages = parseDiscoverTotalPagesJson(data);
                List<Movie> movieList = parseDiscoverJson(data);
                if (movieList != null) {
                    mMovies.addAll(movieList);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<String> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<String> switchLoaderListener = new LoaderManager.LoaderCallbacks<String>() {
        // https://stackoverflow.com/questions/10524667/android-asynctaskloader-doesnt-start-loadinbackground
        @NonNull
        @Override
        public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
            return new AsyncTaskLoader<String>(mContext) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (args == null) {
                        return;
                    }
                    if (takeContentChanged()) {
                        forceLoad();
                    }
                }
                @Override
                public String loadInBackground() {
                    String switchUrlString = args.getString(SWITCH_URL_EXTRA);
                    if (switchUrlString == null || TextUtils.isEmpty(switchUrlString)) {
                        return null;
                    }
                    try {
                        mLoading = true;
                        URL switchUrl = new URL(switchUrlString);
                        return NetworkUtil.getResponseFromHttpUrl(switchUrl);
                    } catch (IOException e) {
                        mLoading = false;
                        e.printStackTrace();
                        return null;
                    }
                }
                @Override
                protected void onStopLoading() {
                    cancelLoad();
                }
            };
        }

        @Override
        public void onLoadFinished(@NonNull Loader<String> loader, String data) {
            mLoading = false;
            if (data != null && !data.equals("")) {
                mFailedPageAdd = false;
                if (mTotalPages == 0) mTotalPages = parseDiscoverTotalPagesJson(data);
                List<Movie> movieList = parseDiscoverJson(data);
                if (movieList != null) {
                    mMovies.clear();
                    mMovies.addAll(movieList);
                    mAdapter.notifyDataSetChanged();
                    mPosters.scrollToPosition(0);
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<String> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> favoritesLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {
        // https://stackoverflow.com/questions/10524667/android-asynctaskloader-doesnt-start-loadinbackground
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable final Bundle args) {
            return new AsyncTaskLoader<Cursor>(mContext) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (takeContentChanged()) {
                        forceLoad();
                    }
                }
                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_FAVORITES_URI,
                                null,
                                null,
                                null,
                                null);
                    } catch (Exception e) {
                        Log.e("FAVORITES_LOADER", "Failed to load favorites");
                        e.printStackTrace();
                        return null;
                    }
                }
                @Override
                protected void onStopLoading() {
                    cancelLoad();
                }
            };
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            if (data.getCount() > 0) {
                List<Movie> movieList = new ArrayList<>();
                data.moveToFirst();
                do {
                    int id = data.getInt(data.getColumnIndex(
                            FavoritesContract.FavoritesEntry.COLUMN_ID
                    ));
                    int vote_average = data.getInt(data.getColumnIndex(
                            FavoritesContract.FavoritesEntry.COLUMN_VOTE_AVG
                    ));
                    String title = data.getString(data.getColumnIndex(
                            FavoritesContract.FavoritesEntry.COLUMN_TITLE
                    ));
                    String poster_path = data.getString(data.getColumnIndex(
                            FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH
                    ));
                    String overview = data.getString(data.getColumnIndex(
                            FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW
                    ));
                    String release_date = data.getString(data.getColumnIndex(
                            FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE
                    ));
                    Movie movie = new Movie(id, title);
                    movie.setOverview(overview);
                    movie.setReleaseDate(release_date);
                    movie.setPosterPath(poster_path);
                    movie.setVoteAverage(vote_average);
                    movieList.add(movie);
                } while (data.moveToNext());
                if (movieList != null && !movieList.isEmpty()) {
                    mMovies.clear();
                    mMovies.addAll(movieList);
                    mAdapter.notifyDataSetChanged();
                    mPosters.scrollToPosition(0);
                }
            } else {
                mSortOrder = mPrevSortOrder;
                mMenu.findItem(R.id.menu_sort_order).setTitle(getResources().getString(mSortOrder));
                Toast.makeText(getBaseContext(), getResources().getString(R.string.favorite_empty), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SORTING_ORDER, mSortOrder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_sort_order).setTitle(getResources().getString(mSortOrder));
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
                if (mSortOrder != ratedId && hasNetwork()) {
                    mPrevSortOrder = mSortOrder;
                    mSortOrder = ratedId;
                    mMenu.findItem(R.id.menu_sort_order).setTitle(getResources().getString(mSortOrder));
                    if (hasNetwork()) {
                        mPage = 1;

                        URL ratedMovieUrl = NetworkUtil.buildDiscoverUrl(mSortOrder, mPage);

                        Bundle switchBundle = new Bundle();
                        switchBundle.putString(SWITCH_URL_EXTRA, ratedMovieUrl.toString());

                        Loader<String> switchMovieLoader = mLoaderManager.getLoader(SWITCH_MOVIE_LOADER);

                        if (switchMovieLoader == null) {
                            mLoaderManager.initLoader(SWITCH_MOVIE_LOADER, switchBundle, switchLoaderListener).onContentChanged();
                        } else {
                            mLoaderManager.restartLoader(SWITCH_MOVIE_LOADER, switchBundle, switchLoaderListener).onContentChanged();
                        }
                    }
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_popular:
                int popularId = R.string.popular;
                if (mSortOrder != popularId && hasNetwork()) {
                    mPrevSortOrder = mSortOrder;
                    mSortOrder = popularId;
                    mMenu.findItem(R.id.menu_sort_order).setTitle(getResources().getString(mSortOrder));
                    if (hasNetwork()) {
                        mPage = 1;

                        URL ratedMovieUrl = NetworkUtil.buildDiscoverUrl(mSortOrder, mPage);

                        Bundle switchBundle = new Bundle();
                        switchBundle.putString(SWITCH_URL_EXTRA, ratedMovieUrl.toString());

                        Loader<String> switchMovieLoader = mLoaderManager.getLoader(SWITCH_MOVIE_LOADER);

                        if (switchMovieLoader == null) {
                            mLoaderManager.initLoader(SWITCH_MOVIE_LOADER, switchBundle, switchLoaderListener).onContentChanged();
                        } else {
                            mLoaderManager.restartLoader(SWITCH_MOVIE_LOADER, switchBundle, switchLoaderListener).onContentChanged();
                        }
                    }
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_favorite:
                int favoriteId = R.string.favorite;
                if (mSortOrder != favoriteId) {
                    mPrevSortOrder = mSortOrder;
                    mSortOrder = favoriteId;
                    mMenu.findItem(R.id.menu_sort_order).setTitle(getResources().getString(mSortOrder));
                    Loader<String> favoritesMovieLoader = mLoaderManager.getLoader(FAVORITES_MOVIE_LOADER);

                    if (favoritesMovieLoader == null) {
                        mLoaderManager.initLoader(FAVORITES_MOVIE_LOADER, null, favoritesLoaderListener).onContentChanged();
                    } else {
                        mLoaderManager.restartLoader(FAVORITES_MOVIE_LOADER, null, favoritesLoaderListener).onContentChanged();
                    }
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_refresh:
                if (hasNetwork()) {
                    if (mFailedPageAdd) {
                        if (hasNetwork()) {
                            mPage++;

                            URL popularMovieUrl = NetworkUtil.buildDiscoverUrl(mSortOrder, mPage);

                            Bundle discoverBundle = new Bundle();
                            discoverBundle.putString(DISCOVER_URL_EXTRA, popularMovieUrl.toString());

                            Loader<String> discoverMovieLoader = mLoaderManager.getLoader(DISCOVER_MOVIE_LOADER);

                            if (discoverMovieLoader == null) {
                                mLoaderManager.initLoader(DISCOVER_MOVIE_LOADER, discoverBundle, discoverLoaderListener).onContentChanged();
                            } else {
                                mLoaderManager.restartLoader(DISCOVER_MOVIE_LOADER, discoverBundle, discoverLoaderListener).onContentChanged();
                            }
                        }
                        Toast.makeText(this, getResources().getString(R.string.page_load), Toast.LENGTH_SHORT).show();
                    } else {
                        if (hasNetwork()) {
                            mPage = 1;

                            URL ratedMovieUrl = NetworkUtil.buildDiscoverUrl(mSortOrder, mPage);

                            Bundle switchBundle = new Bundle();
                            switchBundle.putString(SWITCH_URL_EXTRA, ratedMovieUrl.toString());

                            Loader<String> switchMovieLoader = mLoaderManager.getLoader(SWITCH_MOVIE_LOADER);

                            if (switchMovieLoader == null) {
                                mLoaderManager.initLoader(SWITCH_MOVIE_LOADER, switchBundle, switchLoaderListener).onContentChanged();
                            } else {
                                mLoaderManager.restartLoader(SWITCH_MOVIE_LOADER, switchBundle, switchLoaderListener).onContentChanged();
                            }
                        }
                    }
                }
                break;
            default: break;
        }

        return super.onOptionsItemSelected((item));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        if (savedInstanceState != null) {
            mSortOrder = savedInstanceState.getInt(SORTING_ORDER);
        } else {
            mSortOrder = R.string.popular;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPosters = findViewById(R.id.poster_rv);

        PosterItemDecoration posterDecoration = new PosterItemDecoration(POSTER_SPACING);
        mPosters.addItemDecoration(posterDecoration);

        final GridLayoutManager layoutManager = new GridLayoutManager(this, POSTER_SPAN);
        mPosters.setLayoutManager(layoutManager);

        // https://stackoverflow.com/questions/4142090/how-to-retrieve-the-dimensions-of-a-view/4406090#4406090
        mPosters.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float width = getResources().getDimension(R.dimen.poster_width);
                layoutManager.setSpanCount(mPosters.getWidth() / (int) width);
            }
        });

        mPosters.setItemViewCacheSize(NUM_POSTERS);

        mLoaderManager = getSupportLoaderManager();

        switch(mSortOrder) {
            case R.string.rated:
                if (hasNetwork()) {
                    mPage = 1;

                    URL ratedMovieUrl = NetworkUtil.buildDiscoverUrl(mSortOrder, mPage);

                    Bundle discoverBundle = new Bundle();
                    discoverBundle.putString(DISCOVER_URL_EXTRA, ratedMovieUrl.toString());

                    Loader<String> discoverMovieLoader = mLoaderManager.getLoader(DISCOVER_MOVIE_LOADER);

                    if (discoverMovieLoader == null) {
                        mLoaderManager.initLoader(DISCOVER_MOVIE_LOADER, discoverBundle, discoverLoaderListener).onContentChanged();
                    } else {
                        mLoaderManager.restartLoader(DISCOVER_MOVIE_LOADER, discoverBundle, discoverLoaderListener).onContentChanged();
                    }
                }
                break;
            case R.string.favorite:
                Loader<String> favoritesMovieLoader = mLoaderManager.getLoader(FAVORITES_MOVIE_LOADER);

                if (favoritesMovieLoader == null) {
                    mLoaderManager.initLoader(FAVORITES_MOVIE_LOADER, null, favoritesLoaderListener).onContentChanged();
                } else {
                    mLoaderManager.restartLoader(FAVORITES_MOVIE_LOADER, null, favoritesLoaderListener).onContentChanged();
                }
                break;
            default:
                if (hasNetwork()) {
                    mPage = 1;

                    URL popularMovieUrl = NetworkUtil.buildDiscoverUrl(mSortOrder, mPage);

                    Bundle discoverBundle = new Bundle();
                    discoverBundle.putString(DISCOVER_URL_EXTRA, popularMovieUrl.toString());

                    Loader<String> discoverMovieLoader = mLoaderManager.getLoader(DISCOVER_MOVIE_LOADER);

                    if (discoverMovieLoader == null) {
                        mLoaderManager.initLoader(DISCOVER_MOVIE_LOADER, discoverBundle, discoverLoaderListener).onContentChanged();
                    } else {
                        mLoaderManager.restartLoader(DISCOVER_MOVIE_LOADER, discoverBundle, discoverLoaderListener).onContentChanged();
                    }
                }
                break;
        }

        mAdapter = new MainAdapter(mMovies);

        mPosters.setAdapter(mAdapter);

        mPosters.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mSortOrder != R.string.favorite && dy > 0) {
                    if (!mLoading) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                        if (lastVisibleItem >= totalItemCount - visibleItemCount ) {
                            if (mPage + 1 <= mTotalPages && hasNetwork() && !mFailedPageAdd) {
                                if (hasNetwork()) {
                                    mPage++;

                                    URL popularMovieUrl = NetworkUtil.buildDiscoverUrl(mSortOrder, mPage);

                                    Bundle discoverBundle = new Bundle();
                                    discoverBundle.putString(DISCOVER_URL_EXTRA, popularMovieUrl.toString());

                                    Loader<String> discoverMovieLoader = mLoaderManager.getLoader(DISCOVER_MOVIE_LOADER);

                                    if (discoverMovieLoader == null) {
                                        mLoaderManager.initLoader(DISCOVER_MOVIE_LOADER, discoverBundle, discoverLoaderListener).onContentChanged();
                                    } else {
                                        mLoaderManager.restartLoader(DISCOVER_MOVIE_LOADER, discoverBundle, discoverLoaderListener).onContentChanged();
                                    }
                                }
                            } else {
                                mFailedPageAdd = true;
                            }
                        }
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mSortOrder == R.string.favorite) {
            Loader<String> favoritesMovieLoader = mLoaderManager.getLoader(FAVORITES_MOVIE_LOADER);

            if (favoritesMovieLoader == null) {
                mLoaderManager.initLoader(FAVORITES_MOVIE_LOADER, null, favoritesLoaderListener).onContentChanged();
            } else {
                mLoaderManager.restartLoader(FAVORITES_MOVIE_LOADER, null, favoritesLoaderListener).onContentChanged();
            }
        }

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
            Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
            if (mMenu != null) {
                mMenu.findItem(R.id.menu_refresh).setVisible(true);
            }
        } else {
            if (mMenu != null) {
                mMenu.findItem(R.id.menu_refresh).setVisible(false);
            }
        }

        return hasConnection;
    }
}
