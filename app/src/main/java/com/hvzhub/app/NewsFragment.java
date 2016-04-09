package com.hvzhub.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.hvzhub.app.API.ErrorUtils;
import com.hvzhub.app.API.NetworkUtils;
import com.hvzhub.app.API.model.APIError;
import com.hvzhub.app.API.model.Games.News.NewsContainer;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.GamePrefs;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @param <T1> The type of news items being displayed
 * @param <T2> The container returned by the API call used to get the news items.
 */
public abstract class NewsFragment<T1, T2 extends NewsContainer<T1>> extends Fragment {
    protected static final int ITEMS_TO_FETCH_AT_ONCE = 20;
    protected final boolean infiniteScrollMode;
    protected Call<T2> loadNewsCall;
    protected OnLogoutListener mListener;
    protected boolean loading;
    protected boolean atEnd;

    protected ListView listView;
    protected BaseAdapter adapter;
    protected View loadingFooter;
    protected List<T1> newsList;
    protected SwipeRefreshLayout swipeContainer;


    public NewsFragment(boolean infiniteScrollMode) {
        this.infiniteScrollMode = infiniteScrollMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getActivity().getString(R.string.news));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.news_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loading = false;
        atEnd = false;

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNews();
            }
        });

        listView = (ListView) view.findViewById(R.id.list_view);
        // Set up the listView to automatically load more items when the top of the view is reached
        if (infiniteScrollMode) {
            setupInfiniteScroll(listView);
        }

        newsList = new LinkedList<>();
        adapter = createAdapter(newsList);
        loadingFooter = getActivity().getLayoutInflater().inflate(R.layout.loader_list_item, null);
        listView.addFooterView(loadingFooter);
        listView.setAdapter(adapter);
        listView.removeFooterView(loadingFooter);

        loadNews(false);
    }

    private void setupInfiniteScroll(ListView listView) {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem = firstVisibleItem + visibleItemCount;
                if (!atEnd && (lastItem >= totalItemCount) && totalItemCount != 0) {
                    if (!loading) {
                        loading = true; // This *must* be set *immediately* or the this block will be called multiple times in succession
                        loadNews(false);
                    }
                }
            }
        });
    }

    /**
     * Create the adapter using the specified list
     *
     * @return The created adapter, bound to the list.
     */
    protected abstract BaseAdapter createAdapter(List<T1> list);

    private void refreshNews() {
        
        loadNews(true);
    }

    /**
     * Create the load news call to be used when loading news. Ensure that it is created exactly
     * as specified by the parameters.
     *
     * @param uuid             The uuid to use for the call
     * @param gameId           The gameId to use for the call
     * @param initialNum       The initial position in the the list to start
     * @param fetchEntireList  Whether or not to fetch the entire list at once. If NewsFragment was
     *                         created with infiniteScrollMode = false, fetchEntireList will always
     *                         be true.
     * @param numItemsToFetch  The total number of items the call will return. If fetchEntireList
     *                         is true, numItemsToFetch will be -1
     *
     * @return The created call
     */
    protected abstract Call<T2> createLoadNewsCall(
            Uuid uuid,
            int gameId,
            int initialNum,
            boolean fetchEntireList,
            int numItemsToFetch
    );

    /**
     * Load the news
     * @param refresh whether or not to do a complete refresh
     *                (eg. clear everything on screen + get the latest news)
     */
    private void loadNews(final boolean refresh) {
        if (!NetworkUtils.networkIsAvailable(getActivity())) {
            loading = false;
            if (refresh) {
                swipeContainer.setRefreshing(false);
            }
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle(getString(R.string.network_not_available))
                    .setMessage(getString(R.string.network_not_available_hint))
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadNews(refresh);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            loading = true;
            if (refresh) {
                showListViewProgress(false); // Make sure the loader is hidden if we're refreshing
            } else {
                showListViewProgress(true);
            }

            String uuid = getActivity().getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).getString(GamePrefs.PREFS_SESSION_ID, null);
            int gameId = getActivity().getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).getInt(GamePrefs.PREFS_GAME_ID, -1);
            if (loadNewsCall != null) {
                // Cancel the last call in case it is still in progress.
                loadNewsCall.cancel();
            }
            loadNewsCall = createLoadNewsCall(
                    new Uuid(uuid),
                    gameId,
                    refresh ? 0 : newsList.size(),
                    !infiniteScrollMode,
                    infiniteScrollMode ? ITEMS_TO_FETCH_AT_ONCE : -1
            );
            loadNewsCall.enqueue(new Callback<T2>() {
                @Override
                public void onResponse(Call<T2> call, Response<T2> response) {
                    if (refresh) {
                        swipeContainer.setRefreshing(false);
                    }
                    if (response.isSuccessful()) {
                        List<T1> newsFromDB = response.body().getNews();
                        if (newsFromDB == null || newsFromDB.isEmpty()) {
                            atEnd = true;
                            if (refresh) {
                                swipeContainer.setRefreshing(false);
                            } else {
                                showListViewProgress(false);
                            }
                            loading = false;
                        } else {
                            if (refresh) {
                                newsList.clear();
                                atEnd = false;
                            }
                            newsList.addAll(response.body().getNews());
                            adapter.notifyDataSetChanged();
                            loading = false;
                            if (!infiniteScrollMode) {
                                showListViewProgress(false);
                            }
                        }
                    } else {
                        loading = false;
                        if (refresh) {
                            swipeContainer.setRefreshing(false);
                        } else {
                            showListViewProgress(false);
                        }

                        APIError apiError = ErrorUtils.parseError(response);
                        String err = apiError.error.toLowerCase();
                        if (err.contains(getString(R.string.invalid_session_id))) {
                            // Notify the parent activity that the user should be logged out
                            // Don't bother stopping the loading animation
                            mListener.onLogout();
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                            b.setTitle(getString(R.string.unexpected_response))
                                    .setMessage(getString(R.string.unexpected_response_hint))
                                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            loadNews(refresh);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<T2> call, Throwable t) {
                    loading = false;
                    if (refresh) {
                        swipeContainer.setRefreshing(false);
                    } else {
                        showListViewProgress(false);
                    }
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    b.setTitle(getString(R.string.generic_connection_error))
                            .setMessage(getString(R.string.generic_connection_error_hint))
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    loadNews(refresh);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
            });
        }
    }

    private void showListViewProgress(boolean show) {
        if (show) {
            // If the progress view is already displayed, don't add another one
            if (listView.getFooterViewsCount() == 0) {
                if (loadingFooter != null) {
                    loadingFooter = getActivity().getLayoutInflater().inflate(R.layout.loader_list_item, null);
                }
                listView.addFooterView(loadingFooter);
            }
        } else {
            if (loadingFooter != null) {
                listView.removeFooterView(loadingFooter);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLogoutListener) {
            mListener = (OnLogoutListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must be an instance of OnLogoutListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
