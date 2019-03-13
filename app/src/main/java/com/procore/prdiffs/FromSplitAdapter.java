package com.procore.prdiffs;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.procore.prdiffs.model.DiffDisplay;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.reflectoring.diffparser.api.model.Hunk;
import io.reflectoring.diffparser.api.model.Line;

public class FromSplitAdapter extends RecyclerView.Adapter<DiffViewHolder> {

    private List<DiffDisplay> dList;
    private Context context;
    private LayoutInflater mInflater;

    public FromSplitAdapter(List<DiffDisplay> dList, Context context) {
        this.dList = dList;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DiffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.diff_split_item, parent, false);
        return new DiffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiffViewHolder holder, int position) {
        final DiffDisplay current = dList.get(position);

        switch (current.getDiffType()){
            case DIFF:
                String d = (String) current.getDiffObj();
                holder.textView.setText(d);
                holder.textView.setTextColor(context.getResources().getColor(R.color.md_black_1000));
                holder.textView.setTypeface(Typeface.DEFAULT_BOLD);
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.md_grey_200));
                break;
            case HUNK_HEADER:
                Hunk h = (Hunk) current.getDiffObj();
                holder.textView.setText(h.getFromFileRange().getLineStart()+", "+h.getFromFileRange().getLineCount());
                holder.textView.setTextColor(context.getResources().getColor(R.color.md_grey_800));
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.md_blue_grey_100));
                break;
            case LINE:
                Line l = (Line) current.getDiffObj();
                switch (l.getLineType()){
                    case TO:
                        holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.md_white_1000));
                        holder.textView.setText("");
                        break;
                    case FROM:
                        holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.md_red_100));
                        holder.textView.setText(current.getLineNum()+" - "+l.getContent());
                        break;
                    case NEUTRAL:
                        holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.md_white_1000));
                        holder.textView.setText(current.getLineNum()+" "+l.getContent());
                        break;
                }
                holder.textView.setTextColor(context.getResources().getColor(R.color.md_black_1000));
                break;

        }
    }

    @Override
    public int getItemCount() {
        return dList.size();
    }
}