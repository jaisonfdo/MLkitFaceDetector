package com.droidmentor.mlkitfacedetection.Utils.ProgressBarUtil;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmentor.mlkitfacedetection.R;


/**
 * Created by Jaison on 03/02/17.
 */

public class ProgressUtils {

    Context context;
    private Dialog progressDialog;

    public ProgressUtils(Context context) {
        this.context = context;
    }

    public void showDialog(ProgressBarData progressBarData) {
        Log.d("util", "show dialog");

        progressDialog = new Dialog(context);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progressdialog);

        ProgressBar progressbar = progressDialog.findViewById(R.id.progressBar1);
        RelativeLayout rlBackground = progressDialog.findViewById(R.id.rlLoaderBackground);
        TextView tvProgressMessage = progressDialog.findViewById(R.id.tvProgressDesc);

        if (progressBarData == null)
           return;

        progressbar.getIndeterminateDrawable().setColorFilter(progressBarData.getProgressbarTintColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        progressDialog.setCancelable(progressBarData.isCancelable);

        if (!TextUtils.isEmpty(progressBarData.getProgressMessage())) {
            tvProgressMessage.setVisibility(View.VISIBLE);
            tvProgressMessage.setText(progressBarData.getProgressMessage());
            tvProgressMessage.setTextColor(progressBarData.getProgressMessageColor());
            rlBackground.setBackgroundColor(progressBarData.getBackgroundViewColor());
        } else {
            tvProgressMessage.setVisibility(View.GONE);
            rlBackground.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        progressDialog.show();
    }


    public void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        Log.d("util", "dismiss dialog");
    }

}
