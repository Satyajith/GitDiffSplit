package com.procore.prdiffs.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;

import com.procore.prdiffs.R;

import java.util.Objects;

public class Utility {
    public static ProgressDialog createProgress(Context c){
        ProgressDialog d = new ProgressDialog(c);
        try {
            d.show();
        }catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        d.setCancelable(false);
        Objects.requireNonNull(d.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.setContentView(R.layout.progressdialog);
        return d;
    }
}
