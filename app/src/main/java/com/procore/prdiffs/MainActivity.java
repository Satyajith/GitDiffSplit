package com.procore.prdiffs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.procore.prdiffs.model.PullRequest;
import com.procore.prdiffs.network.ApiInterface;
import com.procore.prdiffs.network.RetrofitClient;
import com.procore.prdiffs.utils.RecyclerTouchListener;
import com.procore.prdiffs.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ApiInterface mApiInterface;
    private PRAdapter mAdapter;
    private CompositeDisposable disposable = new CompositeDisposable();
    private List<PullRequest> prList;
    private ProgressDialog dialog;

    @BindView(R.id.pr_list_recyclerView)
    RecyclerView prListRecyclerView;

    @BindView(R.id.pr_list_empty_textView)
    TextView emptyMessage;

    @BindView(R.id.pr_list_octocat_imageView)
    ImageView octocatImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Pull Requests");
        toolbar.setTitleTextColor(getResources().getColor(R.color.md_black_1000));

        prList = new ArrayList<>();
        mAdapter = new PRAdapter(prList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        prListRecyclerView.setLayoutManager(layoutManager);
        prListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        prListRecyclerView.setAdapter(mAdapter);

        prListRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                prListRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                dialog = Utility.createProgress(MainActivity.this);
                if (isStoragePermissionGranted())
                    getDiff(prList.get(position).getDiffUrl());
            }
        }));

        mApiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);

        getPullReqs();
    }

    private void getPullReqs() {
        disposable.add(
                mApiInterface.getPullRequests("open",
                        "created", "desc")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new Function<List<PullRequest>, List<PullRequest>>() {

                            @Override
                            public List<PullRequest> apply(List<PullRequest> pullRequests) {
                                return pullRequests;
                            }
                        })
                        .subscribeWith(new DisposableSingleObserver<List<PullRequest>>() {

                            @Override
                            public void onSuccess(List<PullRequest> pullRequests) {
                                prList.clear();
                                prList.addAll(pullRequests);
                                mAdapter.notifyDataSetChanged();
                                emptyMessage.setVisibility(prList.size() > 0 ? View.GONE : View.VISIBLE);
                                octocatImage.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                                showError(e);
                            }
                        })
        );
    }

    private void getDiff(String diffUrl) {
        dialog = Utility.createProgress(MainActivity.this);
        disposable.add(
                mApiInterface.getDiff(diffUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<ResponseBody>() {
                            @Override
                            public void onSuccess(ResponseBody responseBody) {
                                dialog.dismiss();
                                try {
                                    File f = Environment.getExternalStorageDirectory();
                                    File file = new File(f, "diff.txt");
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    fileOutputStream.write(responseBody.bytes());
                                    fileOutputStream.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Intent splitView = new Intent(MainActivity.this, DiffSplitActivity.class);
                                startActivity(splitView);
                            }

                            @Override
                            public void onError(Throwable e) {
                                dialog.dismiss();
                            }
                        }));

    }

    private void showError(Throwable e) {
        String message = "";
        try {
            if (e instanceof IOException) {
                message = "No internet connection!";
            } else if (e instanceof HttpException) {
                HttpException error = (HttpException) e;
                String errorBody = error.response().errorBody().string();
                JSONObject jObj = new JSONObject(errorBody);

                message = jObj.getString("error");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.pr_list_recyclerView), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

    public boolean isStoragePermissionGranted() {
        dialog.dismiss();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}