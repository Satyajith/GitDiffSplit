package com.procore.prdiffs;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

class DiffViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.diff_disp_textView)
    TextView textView;

    DiffViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}