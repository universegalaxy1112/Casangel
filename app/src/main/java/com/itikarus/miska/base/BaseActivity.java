package com.itikarus.miska.base;

import android.annotation.SuppressLint;
import android.app.Dialog;

import androidx.fragment.app.Fragment;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.LayoutRes;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.itikarus.miska.R;
import com.itikarus.miska.library.CustomProgressDialog;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
    }


    public void Crouton(final String msg, final boolean result) {

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        TextView tv = snackBarView.findViewById(R.id.snackbar_text);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(17);
        if (result) {
            snackBarView.setBackgroundColor(Color.WHITE);
        } else {
            snackBarView.setBackgroundColor(Color.parseColor("#f8d915"));
        }
        snackbar.show();
    }

    public void restartActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        restartActivity(intent);
    }

    public void restartActivity(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    public static final int RL = 0;
    public static final int LR = 1;
    public static final int TB = 2;
    public static final int BT = 3;
    public static final int NT = 4;

    public void restartActivity(Class<?> cls, int direct) {
        Intent intent = new Intent(this, cls);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        switch (direct) {
            case LR:
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                break;
            case RL:
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                break;
            case TB:
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                break;
            case BT:
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_top);
                break;
        }

        finish();
    }

    public void restartActivity(Intent intent, int direct) {
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        switch (direct) {
            case LR:
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                break;
            case RL:
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                break;
            case TB:
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                break;
            case BT:
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_top);
                break;
        }

        finish();
    }

    public void restartActivityWithClear(Class<?> cls, int direct) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        switch (direct) {
            case LR:
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                break;
            case RL:
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                break;
            case TB:
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                break;
            case BT:
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_top);
                break;
        }

        finish();
    }

    public void restartActivityWithClear(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    public void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void startActivity(Class<?> cls, int direct) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        switch (direct) {
            case LR:
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                break;
            case RL:
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                break;
            case TB:
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                break;
            case BT:
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_top);
                break;
        }
    }

    public void finish(int direct) {
        finish();

        switch (direct) {
            case LR:
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                break;
            case RL:
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                break;
            case TB:
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                break;
            case BT:
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_top);
                break;
        }
    }

    public boolean checkEditText(EditText edit, Out<String> value) {
        if (edit == null) return false;
        edit.setError(null);
        String string = edit.getText().toString();
        if (TextUtils.isEmpty(string)) {
            edit.setError(getString(R.string.required_field));
            edit.requestFocus();
            return false;
        }
        value.set(string);
        return true;
    }

    public boolean checkEmail(EditText edit, Out<String> value) {
        if (!checkEditText(edit, value)) return false;
        String string = value.get();
        boolean res = !TextUtils.isEmpty(string) && android.util.Patterns.EMAIL_ADDRESS.matcher(string).matches();
        if (!res) {
            edit.setError(getString(R.string.string_msg_email_invalid));
            edit.requestFocus();
            return false;
        }
        return true;
    }

    public boolean checkTextView(TextView textView, Out<String> value) {
        if (textView == null) return false;
        textView.setError(null);
        String string = textView.getText().toString();
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        value.set(string);
        return true;
    }

    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    public void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public static class Out<T> {
        T s;

        public void set(T value) {
            s = value;
        }

        public T get() {
            return s;
        }

        public Out() {
        }
    }

    private CustomProgressDialog customProgressDialog;

    public void showCustomProgressDialog() {
        if (customProgressDialog == null)
            customProgressDialog = new CustomProgressDialog(this, getString(R.string.wait));
        customProgressDialog.show();
    }

    public void showCustomProgressDialog(String message) {
        customProgressDialog = new CustomProgressDialog(this, message);
        customProgressDialog.setCancelable(false);
        customProgressDialog.show();
    }

    private ProgressDialog progressDialog;

    public void showProgressDialog() {
        showProgressDialog("Waiting...");
    }

    public void showProgressDialog(String message) {
        hideProgressDialog();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //progressDialog.setTitle(getString(R.string.wait));
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (customProgressDialog != null) customProgressDialog.dismiss();
        if (progressDialog != null) progressDialog.dismiss();
    }


    public void setFragment(Fragment fragment, int id) {
        try {
            transitionFragment(fragment, id);

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void transitionFragment(Fragment fragment, int id) {

        androidx.fragment.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(id, fragment, fragment.getClass().getSimpleName());
        fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

}
