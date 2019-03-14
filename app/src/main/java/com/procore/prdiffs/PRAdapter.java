package com.procore.prdiffs;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.procore.prdiffs.model.PullRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

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
        String pUser = "by " + current.getUser().getLogin();
        holder.prNumberTextView.setText(pNum);
        try {
            holder.prDate.setText(timeAgo(current.getCreatedAt()).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

    public CharSequence timeAgo(String input) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        long time = sdf.parse(input).getTime();
        long now = System.currentTimeMillis();

        return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
    }
}
