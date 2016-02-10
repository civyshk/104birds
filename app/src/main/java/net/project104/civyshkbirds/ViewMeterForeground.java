package net.project104.civyshkbirds;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ViewMeterForeground extends View{

    //I guess it gets removed when the view gets removed when the container fragment gets removed
    PlayerFragment player;

    public ViewMeterForeground(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.player = null;
    }

    public void setPlayerFragment(PlayerFragment player){
        this.player = player;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int maxWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //View bg = ((View) getParent()).findViewWithTag("background");
        //int desiredSize = fragment.currentUserVol * bg.getLayoutParams().height / 10;

        //modes not used. Useful for match_parent or wrap_content, check this:
        if (widthMode == MeasureSpec.EXACTLY) {
            width = maxWidth;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            if(player == null) {
                width = 0;
            }else{
                width = player.currentUserVol*maxWidth/player.maxUserVol;
            }
        } else {
            if(player == null) {
                width = 0;
            }else{
                width = 0;
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = maxHeight;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            if(player == null) {
                height = 0;
            }else{
                height = player.currentUserVol*maxHeight/player.maxUserVol;
            }
        } else {
            if(player == null) {
                height = 0;
            }else{
                height = 0;
            }
        }

        this.setMeasuredDimension(width, height);
    }
}
