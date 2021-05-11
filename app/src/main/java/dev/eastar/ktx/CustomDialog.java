package dev.eastar.ktx;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.log.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewKt;

public class CustomDialog extends AlertDialog {

    public static String logo = "LOGO";

    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    public CustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public CustomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.custom_dialog);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.e();

    }

    public static class Builder extends AlertDialog.Builder {
        CharSequence title;
        CharSequence message;
        CharSequence positiveButtonText;
        CharSequence negativeButtonText;
        private OnClickListener positiveButtonListener;
        private OnClickListener negativeButtonListener;

        public Builder(@NonNull Context context) {            super(context);        }

        public Builder(@NonNull Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        public AlertDialog.Builder setTitle(int titleId) {
            this.title = getContext().getString(titleId);
            return this;
        }

        @Override
        public AlertDialog.Builder setTitle(@Nullable CharSequence title) {
            this.title = title;
            return this;
        }

        @Override
        public AlertDialog.Builder setMessage(int messageId) {
            this.message = getContext().getString(messageId);
            return this;
        }

        @Override
        public AlertDialog.Builder setMessage(@Nullable CharSequence message) {
            this.message = message;
            return this;
        }

        @Override
        public AlertDialog.Builder setPositiveButton(int textId, OnClickListener listener) {
            this.positiveButtonText = getContext().getText(textId);
            this.positiveButtonListener = listener;
            return super.setPositiveButton(null, listener);
        }

        @Override
        public AlertDialog.Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            this.positiveButtonText = text;
            this.positiveButtonListener = listener;
            return this;
        }

        @Override
        public AlertDialog.Builder setNegativeButton(int textId, OnClickListener listener) {
            this.negativeButtonText = getContext().getText(textId);
            this.negativeButtonListener = listener;
            return this;
        }

        @Override
        public AlertDialog.Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            this.negativeButtonText = text;
            this.negativeButtonListener = listener;
            return this;
        }


        public AlertDialog create() {
            final AlertDialog dialog = new CustomDialog(getContext());
            dialog.create();
            AlertDialog contentView = dialog;


            Button btnPositive = contentView.findViewById(R.id.button1);
            ViewKt.setGone(btnPositive, !(positiveButtonText != null && !positiveButtonText.toString().isEmpty()));
            btnPositive.setText(positiveButtonText);
            btnPositive.setOnClickListener(v -> {
                if (positiveButtonListener != null)
                    positiveButtonListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                contentView.dismiss();
            });

            Button btnNegative = contentView.findViewById(R.id.button2);
            ViewKt.setGone(btnNegative, !(negativeButtonText != null && !negativeButtonText.toString().isEmpty()));
            btnNegative.setText(negativeButtonText);
            btnNegative.setOnClickListener(v -> {
                if (negativeButtonListener != null)
                    negativeButtonListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                contentView.dismiss();
            });


            return dialog;
        }
    }
}
