package com.procore.prdiffs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.procore.prdiffs.model.PullRequest;
import com.procore.prdiffs.network.ApiInterface;
import com.procore.prdiffs.network.RetrofitClient;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PRAdapter extends RecyclerView.Adapter<PRAdapter.PRViewHolder> {

    private List<PullRequest> prList;
    private Context context;
    private LayoutInflater mInflater;
    private ApiInterface mApiInterface;

    public PRAdapter(List<PullRequest> prList, Context context) {
        this.prList = prList;
        this.context = context;
        mApiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public PRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.pull_request_row, parent, false);
        return new PRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PRViewHolder holder, int position) {
        final PullRequest current = prList.get(position);

        holder.prTitleTextView.setText(current.getTitle());
        holder.prNumberTextView.setText(String.valueOf(current.getNumber()));
        holder.prUsernameTextView.setText(current.getUser().getLogin());
    }

    @Override
    public int getItemCount() {
        return prList.size();
    }

    class PRViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.pr_title_textView)
        TextView prTitleTextView;
        @BindView(R.id.pr_number_textView)
        TextView prNumberTextView;
        @BindView(R.id.pr_username_textView)
        TextView prUsernameTextView;

        PRViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            getDiff(prList.get(getAdapterPosition()).getDiffUrl());
            //Toast.makeText(context, prList.get(getAdapterPosition()).getDiffUrl(), Toast.LENGTH_LONG).show();
        }

        private void getDiff(String diffUrl) {
            Call<ResponseBody> call = mApiInterface.getDiff(diffUrl);
            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            System.out.println(response.body().string() + call.request().url());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else{
                        System.out.println(call.request().url());
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }
}
