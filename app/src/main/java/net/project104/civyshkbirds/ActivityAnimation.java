package net.project104.civyshkbirds;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ActivityAnimation extends AppCompatActivity {


    private boolean withStars;

    public static class BackgroundColorSetter implements ValueAnimator.AnimatorUpdateListener {
        View view;

        public BackgroundColorSetter(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            view.setBackgroundColor(((Integer) animation.getAnimatedValue()).intValue());
        }
    }

    /*
    public static class MarginSetter implements ValueAnimator.AnimatorUpdateListener{
        View view;
        RelativeLayout.LayoutParams params;
        public MarginSetter(View view) {
            this.view = view;
            params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        }
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            params.leftMargin = params.rightMargin = params.topMargin = params.bottomMargin =
                    ((Integer) animation.getAnimatedValue()).intValue();
            view.setLayoutParams(params);
        }
    }
    */

    public static class SizeSetter implements ValueAnimator.AnimatorUpdateListener {
        View view;
        RelativeLayout.LayoutParams params;

        public SizeSetter(View view) {
            this.view = view;
            params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            params.height = params.width = ((Integer) animation.getAnimatedValue()).intValue();
            view.setLayoutParams(params);
        }
    }

    public static class AlphaSetter implements ValueAnimator.AnimatorUpdateListener {
        View view;

        public AlphaSetter(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            view.setAlpha(((Float) animation.getAnimatedValue()).floatValue());
        }
    }

    /*public static class AnimatorRepeater extends AnimatorListenerAdapter{
        ValueAnimator[] mAnimators;
        public AnimatorRepeater(ValueAnimator... animators){
            mAnimators = animators;
        }
        @Override
        public void onAnimationEnd(Animator animation) {
            for(Animator a: mAnimators){
                if(a.isRunning()){
                    a.end();
                }
                a.start();
            }
            animation.start();
        }
        public void start(Animator animation){
            onAnimationEnd(animation);
        }
        public void setCurrentPlayTime(long time){
            for(ValueAnimator animator : mAnimators){
                animator.setCurrentPlayTime(time);
            }
        }
    }*/

    static final int[] skyColoursIDs = {R.color.sky_0, R.color.sky_10, R.color.sky_25, R.color.sky_30, R.color.sky_35, R.color.sky_45, R.color.sky_55, R.color.sky_65, R.color.sky_70, R.color.sky_75, R.color.sky_85, R.color.sky_100};
    static final int[] durations = {10, 15, 5, 5, 10, 10, 10, 5, 5, 10, 15};

    ValueAnimator dayNightAnimator;
    ValueAnimator starsAnimator;

    protected void onCreate(Bundle savedInstanceState, Bundle bundleWithTime, int layout) {
        super.onCreate(savedInstanceState);

        setContentView(layout);

        //ANIMATIONS
        long playTime = 0;
        if (bundleWithTime != null) {
            playTime = bundleWithTime.getLong("PlayTime", 0);
        }
        Resources res = getResources();
        final int durationCycle = res.getInteger(R.integer.day_night_duration);

        //SKY
        int[] colours = new int[skyColoursIDs.length];
        for (int i = 0; i < skyColoursIDs.length; i++) {
            colours[i] = res.getColor(skyColoursIDs[i]);
        }
        dayNightAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),
                colours[0], colours[1], colours[2], colours[3], colours[4],
                colours[5], colours[6], colours[7], colours[8], colours[9],
                colours[10], colours[11]);
        //dayNightAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colours); ¿bugged? it doesn't work this way, with ofObject(Evaluator, Object... values)
        dayNightAnimator.setDuration(10 * durationCycle * ActivityMain.sum(durations));
        dayNightAnimator.setRepeatCount(ValueAnimator.INFINITE);
        dayNightAnimator.setRepeatMode(ValueAnimator.RESTART);
        dayNightAnimator.setInterpolator(new LinearInterpolator());
        dayNightAnimator.start();
        dayNightAnimator.setCurrentPlayTime(playTime);

        //STARS
        starsAnimator = ValueAnimator.ofFloat(0.f, 0.f, 1.f, 1.f, 1.f, 0.f, 0.f);
        starsAnimator.setDuration(10 * durationCycle * ActivityMain.sum(durations));
        starsAnimator.setRepeatCount(ValueAnimator.INFINITE);
        starsAnimator.setRepeatMode(ValueAnimator.RESTART);
        starsAnimator.setInterpolator(new LinearInterpolator());
        starsAnimator.start();
        starsAnimator.setCurrentPlayTime(playTime);

        ImageView ivStars = (ImageView) findViewById(R.id.ivStars);
        if (ivStars != null) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
            withStars = preferences.getBoolean(getString(R.string.preferences_background_stars), true);
            if (withStars) {
                ivStars.setVisibility(View.VISIBLE);
            } else {
                ivStars.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        //can leak memory, so remove listeners later
        dayNightAnimator.addUpdateListener(new BackgroundColorSetter(findViewById(R.id.rootView)));

        //activity layout may or may not include the stars background
        View ivStars = findViewById(R.id.ivStars);
        if(withStars && ivStars != null) {
            starsAnimator.addUpdateListener(new AlphaSetter(ivStars));
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        dayNightAnimator.removeAllUpdateListeners();
        starsAnimator.removeAllUpdateListeners();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //This method must be called from subclasses

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_animation, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        //This method must be called by subclasses
        boolean withStars =
                getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
                        .getBoolean(getString(R.string.preferences_background_stars), true);
        menu.findItem(R.id.menu_item_background_stars).setChecked(withStars);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_background_stars:
                boolean withStars = !item.isChecked();
                item.setChecked(withStars);
                SharedPreferences.Editor preferences =
                        getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).edit();
                preferences.putBoolean(getString(R.string.preferences_background_stars), withStars);
                preferences.apply();
                if (withStars) {
                    starsAnimator.start();
                    starsAnimator.setCurrentPlayTime(getAnimatorPlayTime());
                }else{
                    starsAnimator.setCurrentPlayTime(0L);
                    starsAnimator.cancel();
                }
                return true;
            case R.id.menu_item_about:
                Intent intent = new Intent(this, ActivityAbout.class);
                intent.putExtra("PlayTime", getAnimatorPlayTime());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("PlayTime", getAnimatorPlayTime());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //savedInstanceState is fully used in onCreate()
        super.onRestoreInstanceState(savedInstanceState);
    }

    public long getAnimatorPlayTime() {
        return dayNightAnimator.getCurrentPlayTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unbindDrawables(findViewById(R.id.rootView));
        //System.gc();
    }

    private void unbindDrawables(View view) {
        //Got from https://stackoverflow.com/questions/4102758/outofmemory-exception-when-loading-bitmap-from-external-storage/4134307#4134307

        //TODO Could I bitmap.release() instead of System.gc() ?

        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

}

