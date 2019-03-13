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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

    public PRAdapter(List<PullRequest> prList, Context context) {
        this.prList = prList;
        this.context = context;
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
        String pNum = "# " + String.valueOf(current.getNumber());
        String pUser = "by "+ current.getUser().getLogin();
        holder.prNumberTextView.setText(pNum);
        holder.prDate.setText(current.getCreatedAt());
        holder.prUsernameTextView.setText(pUser);
    }

    @Override
    public int getItemCount() {
        return prList.size();
    }

    class PRViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pr_title_textView)
        TextView prTitleTextView;
        @BindView(R.id.pr_number_textView)
        TextView prNumberTextView;
        @BindView(R.id.pr_username_textView)
        TextView prUsernameTextView;
        @BindView(R.id.pr_date_textView)
        TextView prDate;

        PRViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
