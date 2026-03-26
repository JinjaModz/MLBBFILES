package com.neonmodz.app;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GradientKProgressHUD {

    public enum ProgressType {
        HORIZONTAL,
        CIRCULAR
    }

    public Dialog dialog;
    public GradientLinearProgressView progressView;
    public TextView titleView, msgView, percentView, cancelButton;

    public boolean isCancelled = false;

    public interface OnCancelListener {
        void onCancel();
    }

    private OnCancelListener cancelListener;

    public void setOnCancelListener(OnCancelListener listener){
        this.cancelListener = listener;
    }

    public GradientKProgressHUD(Context context, ProgressType type) {

        float dp = context.getResources().getDisplayMetrics().density;
        boolean dark = isDarkMode(context);

        LinearLayout root = new LinearLayout(context);
        root.setPadding((int) (24 * dp), (int) (20 * dp), (int) (24 * dp), (int) (20 * dp));
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);

        root.setBackground(new GradientDrawable() {{
            setColor(dark ? Color.parseColor("#1C1C1E") : Color.WHITE);
            setCornerRadius(28 * dp);
        }});

        // Title
        titleView = new TextView(context);
        titleView.setTextSize(17);
        titleView.setTextColor(dark ? Color.WHITE : Color.BLACK);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setGravity(Gravity.CENTER);

        root.addView(titleView,new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Message
        msgView = new TextView(context);
        msgView.setTextSize(14);
        msgView.setTextColor(dark ? Color.parseColor("#B0B0B0") : Color.parseColor("#444444"));
        msgView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        msgParams.setMargins(0,(int)(4*dp),0,0);

        root.addView(msgView,msgParams);

        //
        progressView = new GradientLinearProgressView(context);

        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int)(22*dp)
        );

        barParams.setMargins(0,(int)(14*dp),0,0);

        root.addView(progressView,barParams);

        // Percent text
        percentView = new TextView(context);
        percentView.setText("0% Downloaded");
        percentView.setTextSize(14);
        percentView.setTextColor(dark ? Color.WHITE : Color.BLACK);
        percentView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams percentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        percentParams.setMargins(0,(int)(6*dp),0,0);

        root.addView(percentView,percentParams);

        //
        cancelButton = new TextView(context);
        cancelButton.setText("Cancel");
        cancelButton.setTextSize(14);
        cancelButton.setTextColor(Color.WHITE);
        cancelButton.setGravity(Gravity.CENTER);
        cancelButton.setPadding((int)(16*dp),(int)(8*dp),(int)(16*dp),(int)(8*dp));

        GradientDrawable cancelBg = new GradientDrawable();
        cancelBg.setColor(Color.parseColor("#FF4444"));
        cancelBg.setCornerRadius(22 * dp);

        cancelButton.setBackground(cancelBg);

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        cancelParams.gravity = Gravity.CENTER;
        cancelParams.setMargins(0,(int)(12*dp),0,0);

        root.addView(cancelButton,cancelParams);

        cancelButton.setOnClickListener(v -> {

            isCancelled = true;

            if(cancelListener != null){
                cancelListener.onCancel();
            }

            dismiss();
        });

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(root);

        if(dialog.getWindow() != null){

            int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.75f);

            dialog.getWindow().setLayout(width,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }

    private boolean isDarkMode(Context c){
        int mode = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    //
    public void show(){
        if(dialog != null && !dialog.isShowing()){
            isCancelled = false; //
            dialog.show();
        }
    }

    public void dismiss(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void setProgress(float value){

        progressView.setProgress(value);
        percentView.setText((int)(value * 100) + "% Downloaded");

        if(value >= 1f){
            fadeOutAndDismiss();
        }
    }

    public void setPercent(int percent){

        progressView.setProgress(percent / 100f);
        percentView.setText(percent + "% Downloaded");

        if(percent >= 100){
            fadeOutAndDismiss();
        }
    }

    private void fadeOutAndDismiss(){

        if(dialog == null || !dialog.isShowing()) return;

        View root = dialog.findViewById(android.R.id.content);

        if(root == null) return;

        AlphaAnimation fade = new AlphaAnimation(1f,0f);
        fade.setDuration(700);
        fade.setFillAfter(true);

        root.startAnimation(fade);

        new Handler().postDelayed(this::dismiss,700);
    }

    //

    public static class GradientLinearProgressView extends View {

        private Paint paintBg,paintProgress;
        private float progress = 0f;
        private float gradientShift = 0f;
        private ValueAnimator animator;

        public GradientLinearProgressView(Context context){

            super(context);

            paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintBg.setColor(Color.parseColor("#333333"));
            paintBg.setStyle(Paint.Style.FILL);

            paintProgress = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintProgress.setStyle(Paint.Style.FILL);

            animator = ValueAnimator.ofFloat(0f,1f);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(2000);

            animator.addUpdateListener(animation -> {

                gradientShift = (float)animation.getAnimatedValue();
                invalidate();

            });

            animator.start();
        }

        public void setProgress(float value){

            progress = Math.max(0f,Math.min(1f,value));
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas){

            float width = getWidth();
            float height = getHeight();
            float radius = height / 2f;

            RectF bgRect = new RectF(0,0,width,height);
            canvas.drawRoundRect(bgRect,radius,radius,paintBg);

            float filledWidth = width * progress;

            if(filledWidth > 0){

                float shift = width * gradientShift;

                LinearGradient gradient = new LinearGradient(
                        -shift,0,filledWidth - shift,0,
                        new int[]{
                                Color.parseColor("#00BCD4"),
                                Color.parseColor("#2196F3")
                        },
                        null,
                        Shader.TileMode.MIRROR
                );

                paintProgress.setShader(gradient);

                RectF fillRect = new RectF(0,0,filledWidth,height);

                canvas.drawRoundRect(fillRect,radius,radius,paintProgress);
            }
        }
    }
}