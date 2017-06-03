package net.project104.civyshkbirds;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

public class AdapterCard extends BaseAdapter {

    // references to our images
    private List<Integer> imageIDs, commonNameIDs;
    private Map<Integer, String> latinNames;
    private WeakReference<Context> context;

    public AdapterCard(Context c) {
        context = new WeakReference<Context>(c);
        imageIDs = new ArrayList<>();
        commonNameIDs = new ArrayList<>();
        latinNames = new HashMap<>();
    }

    public int getCount() {
        return imageIDs.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        /* - LinearLayout
        *    - FrameLayout
        *      - ImageView
        *    - TextView
        */
        Context c = context.get();
        if(c == null){
            return null;
        }
        Resources res = c.getResources();

        LinearLayout layout;
        FrameLayout frame;
        ImageView imageView;
        TextView textView;

        if(convertView == null){
            layout = new LinearLayout(c);
            layout.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, (int) res.getDimension(R.dimen.grid_row_height)));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(0, 0, 0, 0);
            layout.setBackgroundResource(R.color.transparent);

            int p = (int) res.getDimension(R.dimen.line_width_top);
            frame = new FrameLayout(c);
            frame.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 3.f));
            frame.setPadding(p, p, p, p);
            frame.setBackgroundResource(R.color.image_border);

            imageView = new ImageView(c);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            textView = new TextView(c);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.f));
            textView.setBackgroundResource(R.drawable.button_background);
            textView.setGravity(View.TEXT_ALIGNMENT_GRAVITY);

            frame.addView(imageView);
            layout.addView(frame);
            layout.addView(textView);
        }else{
            layout = (LinearLayout) convertView;
            frame = (FrameLayout) layout.getChildAt(0);
            imageView = (ImageView) frame.getChildAt(0);
            textView = (TextView) layout.getChildAt(1);
        }

        String name;
        if(commonNameIDs.get(position) != 0){
            name = res.getString(commonNameIDs.get(position));
        }else{
            name = latinNames.get(position);
        }

        //ActivityMain.setScaledImageResource(context.getResources(), imageView, imageIDs.get(position), false);
        ActivityMain.ImagesLoader imagesLoader = new ActivityMain.ImagesLoader(c.getResources());
        imagesLoader.addImage(imageView, imageIDs.get(position), true);
        imagesLoader.imageViews.get(0).getViewTreeObserver().addOnPreDrawListener(imagesLoader);
        textView.setText(name);
        return layout;
    }
	
    public void loadBirds(TreeMap<String, List<Bird>> birdsByFamily, Context c){
		//List<String> sortedFamilies = new ArrayList<String>(birdsByFamily.keySet());
        NavigableSet<String> sortedFamilies = birdsByFamily.navigableKeySet();
		//Collections.sort(sortedFamilies);
        for(String family : sortedFamilies){
            for(Bird bird : birdsByFamily.get(family)){
				imageIDs.add(bird.getThumbID());
                int commonNameID = ActivityMain.getCommonNameID(c, bird.latinName);
                if(commonNameID == 0){
                    //in case there's not common name, I save latinName for this position
                    latinNames.put(commonNameIDs.size(), bird.latinName);
                }
                commonNameIDs.add(commonNameID);
            }
        }
    }
}