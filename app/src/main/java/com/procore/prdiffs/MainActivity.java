package com.procore.prdiffs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.procore.prdiffs.model.PullRequest;
import com.procore.prdiffs.network.ApiInterface;
import com.procore.prdiffs.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ApiInterface mApiInterface;
    private PRAdapter adapter;

    @BindView(R.id.pr_list_recyclerView)
    RecyclerView prListRecyclerView;

    @BindView(R.id.pr_list_empty_textView)
    TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        prListRecyclerView.setLayoutManager(layoutManager);

        mApiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        getPullReq();
    }

    private void getPullReq() {
        Call<List<PullRequest>> call = mApiInterface.getPullRequests("open",
                "created", "desc");
        call.enqueue(new Callback<List<PullRequest>>() {

            @Override
            public void onResponse(Call<List<PullRequest>> call, Response<List<PullRequest>> response) {
                if (response.isSuccessful()) {
                    adapter = new PRAdapter(response.body(), MainActivity.this);
                    emptyMessage.setVisibility(View.GONE);
                    prListRecyclerView.setVisibility(View.VISIBLE);
                    prListRecyclerView.setAdapter(adapter);
                } else{
                    emptyMessage.setVisibility(View.VISIBLE);
                    prListRecyclerView.setVisibility(View.GONE);
                    System.out.println(response.raw().request().url());
                }

            }

            @Override
            public void onFailure(Call<List<PullRequest>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
