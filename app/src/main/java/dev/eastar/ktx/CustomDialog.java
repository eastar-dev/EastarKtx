package dev.eastar.ktx;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.log.Log;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

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
        setContentView(R.layout.custom_dialog);
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
        CompoundButton.OnCheckedChangeListener checkBoxListener;
        String checkBoxText;
        boolean messageHasHtml = false;
        private OnClickListener positiveButtonListener;
        private OnClickListener negativeButtonListener;

        public Builder(@NonNull Context context) {
            super(context);
        }

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



            AlertDialog contentView = dialog;
            FrameLayout frTitleArea = contentView.findViewById(R.id.fr_title_area);
            TextView tvTitle = contentView.findViewById(R.id.tv_dialog_title);

            if (title != null && !"".equals(title)) {
                frTitleArea.setVisibility(View.VISIBLE);
                if (logo.equals(title)) {
                    contentView.findViewById(R.id.ll_default_header).setVisibility(View.VISIBLE);
                    contentView.findViewById(R.id.tv_dialog_title).setVisibility(View.GONE);
                    contentView.findViewById(R.id.xbutton).setVisibility(View.GONE);
                } else {
                    contentView.findViewById(R.id.ll_default_header).setVisibility(View.GONE);
                    contentView.findViewById(R.id.tv_dialog_title).setVisibility(View.VISIBLE);
                    contentView.findViewById(R.id.xbutton).setVisibility(View.VISIBLE);
                    contentView.findViewById(R.id.xbutton).setOnClickListener(view -> dialog.cancel());
                    tvTitle.setText(title);
                }
            } else {
                contentView.findViewById(R.id.ll_default_header).setVisibility(View.GONE);
                contentView.findViewById(R.id.tv_dialog_title).setVisibility(View.VISIBLE);
                frTitleArea.setVisibility(View.GONE);
                contentView.findViewById(R.id.fr_dialog_msg).setBackgroundResource(R.drawable.top_rounding_background);
            }

            TextView tvMsg = contentView.findViewById(R.id.tv_dialog_msg);
            tvMsg.setMovementMethod(new ScrollingMovementMethod());

            if (!(message == null || "".equals(message))) {
                if (messageHasHtml) {
                    tvMsg.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tvMsg.setText(Html.fromHtml(message.toString(), Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        tvMsg.setText(Html.fromHtml(message.toString()));
                    }
                } else {
                    tvMsg.setVisibility(View.VISIBLE);
                    tvMsg.setText(message);
                }
            } else {
                tvMsg.setVisibility(View.GONE);
            }

            Button btnPositive = contentView.findViewById(R.id.btn_dialog_positive);
            ViewKt.setGone(btnPositive, !(positiveButtonText != null && !positiveButtonText.toString().isEmpty()));
            btnPositive.setText(positiveButtonText);
            btnPositive.setOnClickListener(v -> {
                if (positiveButtonListener != null)
                    positiveButtonListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                contentView.dismiss();
            });

            Button btnNegative = contentView.findViewById(R.id.btn_dialog_negative);
            ViewKt.setGone(btnNegative, !(negativeButtonText != null && !negativeButtonText.toString().isEmpty()));
            btnNegative.setText(negativeButtonText);
            btnNegative.setOnClickListener(v -> {
                if (negativeButtonListener != null)
                    negativeButtonListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                contentView.dismiss();
            });

            CheckBox checkBox = contentView.findViewById(R.id.cb_check_msg);
            if (checkBoxText != null && !checkBoxText.isEmpty()) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setText(checkBoxText);
                checkBox.setOnCheckedChangeListener(checkBoxListener);
            } else {
                checkBox.setVisibility(View.GONE);
            }

            Dialog dlg = super.create();
            Log.e(dlg);

            return dialog;
        }
    }
}
