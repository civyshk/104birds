package net.project104.civyshkbirds;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.project104.civyshkbirds.R;

import java.util.List;

public class AdapterPicture extends PagerAdapter {

    // references to images
    private List<Picture> pictures;
    private FragmentCard fragment;

    public AdapterPicture(FragmentCard f) {
        //this adapter is stored in a view, so I expect that view being deleted when the fragment is destroyed,
        //thus deleting this adapter as well. It seems 'fragment' doesn't leak any mem.
        fragment = f;
        pictures = null;//It'll be just a reference to a previously populated list.
    }

    public int getCount() {
        return pictures.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public Object instantiateItem(ViewGroup container, int position) {

        Context context = container.getContext();

        FrameLayout frame = new FrameLayout(context);
        frame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageViewScaling imageView = new ImageViewScaling(context);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        imageView.setLayoutParams(params);
        int p = (int) context.getResources().getDimension(R.dimen.line_width);
        imageView.setPadding(p, p, p, p);
        imageView.setBackgroundResource(R.color.image_border);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        //This IF(pos>1) avoids a weird bug* if there are more than 2 pictures
        if(position > 1) {
            //This loads the picture, it just doesn't optimize the load
            ActivityMain.setScaledImageResource(context.getResources(), imageView, pictures.get(position).getID(), false);
        }else{
            // *WEIRD BUG: For images which are out of screen, even if I remove the listener in oPD(),
            // onPreDraw is called forever. Is it not being removed? I can only think of some race condition
            // which I can't find.
            ActivityMain.ImagesLoader imagesLoader = new ActivityMain.ImagesLoader(context.getResources());
            imagesLoader.addImage(imageView, pictures.get(position).getID(), false);
            imageView.getViewTreeObserver().addOnPreDrawListener(imagesLoader);
        }


        /*String picid = String.valueOf(pictures.get(position).getID());
        picid = picid.substring(picid.length() - 2);
        String ilid = String.valueOf(imagesLoader);
        ilid = ilid.substring(ilid.length() - 8, ilid.length() - 5);
        Log.d(ActivityMain.LOG_TAG, String.format("new %s - %s (%d)", ilid, picid, position));
*/

        fragment.addListeners(imageView, pictures.get(position));
        fragment.registerForContextMenu(imageView);

        frame.addView(imageView);
        container.addView(frame, 0);

        return frame;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        /*String id = String.valueOf(pictures.get(position).getID());
        id = id.substring(id.length() - 2);
        Log.d(ActivityMain.LOG_TAG, String.format("dstry %s (%d)", id, position));*/

        View imageView = ((ViewGroup) object).getChildAt(0);

        fragment.removeListeners(imageView);
        fragment.unregisterForContextMenu(imageView);

        ((ViewPager) container).removeView((FrameLayout) object);
    }

    //from http://blog.neteril.org/blog/2013/10/14/android-tip-viewpager-with-protruding-children/
    //Don't use this because viewpager with only 1 child, shakes uglyly when sliding.
    /*@Override
    public float getPageWidth (int position)
    {
        return 0.95f;
    }*/
	
    public void loadPictures(Bird bird){
        pictures = bird.pictures; //bird.pictures is supposed not to change ever.
    }

}