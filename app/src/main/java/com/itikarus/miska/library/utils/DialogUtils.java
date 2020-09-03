package com.itikarus.miska.library.utils;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.itikarus.miska.R;

public class DialogUtils {

    public interface OnOkayEvent {
        void onOkay();
    }

    public interface OnOkayNoEvent {
        void onOkay();

        void onNo();
    }

    public static void showOkayDialog(Context context, String title, String msg, final OnOkayEvent func) {
        try {
            new MaterialDialog.Builder(context)
                    .title(title)
                    .content(msg)
                    .positiveText("Okay")
                    .canceledOnTouchOutside(false)
                    .positiveColorRes(R.color.colorPrimary)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (func != null) func.onOkay();
                        }
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showOkayDialog(Context context, String title, String msg) {
        showOkayDialog(context, title, msg, new OnOkayEvent() {
            @Override
            public void onOkay() {

            }
        });
    }

    public static void showOkayNoDialog(Context context, String title, String msg, final OnOkayNoEvent func) {
        showOkayNoDialog(context, title, msg, func, "Yes", "No");
    }

    public static void showOkayNoDialog(Context context, String title, String msg, final OnOkayNoEvent func, String positiveButtonName, String negativeButtonName) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(title)
                .content(msg)
                .cancelable(true)
                .theme(Theme.LIGHT)
                .positiveText(positiveButtonName)
                .positiveColor(Color.GREEN)
                .negativeColor(Color.RED)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (func != null) func.onOkay();
                    }
                })
                .negativeText(negativeButtonName)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (func != null) func.onNo();
                    }
                });
        builder.show();
    }
}
