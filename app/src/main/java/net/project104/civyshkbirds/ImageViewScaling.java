package net.project104.civyshkbirds;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ImageViewScaling extends ImageView {

    public ImageViewScaling(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewScaling(final Context c){
        super(c);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Drawable d = this.getDrawable();

        if (d != null) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            //modes not used. Useful for match_parent or wrap_content, check this:
            /*
            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desiredWidth, widthSize);
            } else {
                width = desiredWidth;
            }
            */
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            final int width;
            final int height;

            float containerRatio = (float) widthSize/heightSize;
            float imageRatio = (float) d.getIntrinsicWidth() / d.getIntrinsicHeight();

            if(containerRatio > imageRatio){
                height = heightSize;
                width = (int) Math.floor(height * imageRatio);
            }else{
                width = widthSize;
                height = (int) Math.floor(width / imageRatio);
            }
            this.setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}