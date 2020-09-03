package com.itikarus.miska.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.itikarus.miska.R;
import com.squareup.picasso.Picasso;

public class ShowImgDialog extends Dialog {

    private int resid = 0;
    private String url = "";
    private String content = "";
    private ImageView imageView;
    private int type;

    public ShowImgDialog(@NonNull Context context, int imgResid) {
        super(context);
        resid = imgResid;
        type = 0;
    }

    public ShowImgDialog(@NonNull Context context, String url) {
        super(context);
        this.url = url;
        type = 2;
    }

    public ShowImgDialog(@NonNull Context context, String url, String text) {
        super(context);
        this.url = url;
        content = text;
        type = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_show_img);
        imageView = findViewById(R.id.imgViewer);

        if (type == 0) {
            Picasso.get().load(resid)
                    .fit()
                    .into(imageView);
        } else {
            Picasso.get().load(url)
                    .fit()
                    .into(imageView);
        }

        if (type == 1) {
            TextView contentViewer = findViewById(R.id.tv_content);
            contentViewer.setText(content);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }
}
