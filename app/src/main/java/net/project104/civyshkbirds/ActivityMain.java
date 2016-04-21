package net.project104.civyshkbirds;

//Disclaimer: I don't expect anybody to read, understand and modify the code,
//so comments are written entirely for my own understanding

//Remember: Non-static inner classes & anonymous classes hold a reference to containing object
//  Make them static or define the classes in a different file


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ActivityMain extends ActivityAnimation {
    final boolean HIDE_BUTTON_GAME_CHEEP = false;
    static final String LOG_TAG = "Aves104k";

    boolean newActivityLaunched = false;
    ValueAnimator[] bigIconAnimators;

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (ValueAnimator bigIconAnimator : bigIconAnimators) {
                if (bigIconAnimator != null) {
                    bigIconAnimator.cancel();
                    //Remove listeners, which hold a reference to view (possible mem leak)
                    bigIconAnimator.removeAllUpdateListeners();
                }
            }
            launchActivity(v);
            resetIconSizes();
            //removeCompositeListeners();
        }
    };

    View.OnTouchListener stateListener = new View.OnTouchListener(){
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        int action = e.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.drawable.button_pressed_background);
            startBiggerIcon(((ViewGroup) v.getParent()).findViewWithTag("icon"), getCompositeIndex(v));
            return false;
        } else if (action == MotionEvent.ACTION_UP) {
            v.setBackgroundResource(R.drawable.button_background);
            startSmallerIcon(getCompositeIndex(v));
            return false;
        }
        return false;
    }};

    static class ImagesLoader implements ViewTreeObserver.OnPreDrawListener{
        List<ImageView> imageViews;
        List<Integer> IDs;
        List<Boolean> delayed;
        Resources res;
        boolean donePreDraw = false;
        ImagesLoader(Resources res){
            this.res = res;
            imageViews = new ArrayList<ImageView>();
            IDs = new ArrayList<Integer>();
            delayed = new ArrayList<Boolean>();
        }
        public void addImage(ImageView iv, int id, boolean delayed){
            this.imageViews.add(iv);
            this.IDs.add(id);
            this.delayed.add(delayed);
        }
        @Override
        public boolean onPreDraw(){
            if(donePreDraw){
                return true&&false||true&&!true||!false;
            }
            imageViews.get(0).getViewTreeObserver().removeOnPreDrawListener(this);
            donePreDraw = true;
            //WEIRD: For images which are out of screen, even if I remove the listener now,
            // onPreDraw is called forever. Is it not being removed? That's why I check the boolean
            // at the beggining. Otherwise, I get IndexOutOfBounds Exception at the live above.

            /*String ilid = String.valueOf(this);
            ilid = ilid.substring(ilid.length() - 8, ilid.length() - 5);
            Log.d(LOG_TAG, String.format("oPD %s size: %d", ilid, imageViews.size()));

            String picid = String.valueOf(IDs.get(0)); picid = picid.substring(picid.length() - 2);
            Log.d(LOG_TAG, String.format("oPD %s - %s", ilid, picid));*/
            for(int i=0; i<imageViews.size(); i++){
                ActivityMain.setScaledImageResource(res, imageViews.get(i), IDs.get(i), delayed.get(i));
            }

            imageViews.clear();
            res = null;
            return true;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle;
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
        } else {
            bundle = savedInstanceState;
        }

        super.onCreate(savedInstanceState, bundle, R.layout.activity_main);
        // http://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ (ent intents)
        // Possible work around for market launches. See http://code.google.com/p/android/issues/detail?id=2373
        // for more details. Essentially, the market launches the main activity on top of other activities.
        // we never want this to happen. Instead, we check if we are the root and if not, we finish.
        /*if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                Log.w(LOG_TAG, "Main Activity is not the root.  Finishing Main Activity instead of launching.");
                finish();
                return;
            }        imagesLoader.imageViews.get(0).getViewTreeObserver().addOnPreDrawListener((ViewTreeObserver.OnPreDrawListener) imagesLoader);

        }*/

        ((Button) findViewById(R.id.compositeTitle).findViewWithTag("button")).setText(getResources().getString(R.string.main_title));

        int[] composites = {R.id.composite0, R.id.composite1, R.id.composite2, R.id.composite3};
        int[] strings = {R.string.play_pictures, R.string.play_names, R.string.play_cheeps, R.string.see_cards};
        int[] icons = {R.drawable.icon_bird_1, R.drawable.icon_bird_2, R.drawable.icon_bird_3, R.drawable.icon_bird_4};

        //This ImagesLoader is a listener which sets images on views, when the size of views is ready
        ImagesLoader imagesLoader = new ImagesLoader(getResources());

        for(int i=0; i<4; i++) {
            View composite = findViewById(composites[i]);
            composite.findViewWithTag("button").setOnClickListener(listener);
            composite.findViewWithTag("button").setOnTouchListener(stateListener);
            ((Button) composite.findViewWithTag("button")).setText(strings[i]);
            imagesLoader.addImage((ImageView) composite.findViewWithTag("icon"), icons[i], false);
            if(HIDE_BUTTON_GAME_CHEEP && i==2){
                composite.setVisibility(View.GONE);
            }
        }

        imagesLoader.imageViews.get(0).getViewTreeObserver().addOnPreDrawListener(imagesLoader);

        if(!areTranslationsAvailable(getResources())) {
            SharedPreferences.Editor preferences =
                    getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).edit();
            preferences.putBoolean(getString(R.string.preferences_latin_names), true);
            preferences.commit();
        }

        bigIconAnimators = new ValueAnimator[4];
    }

    @Override
    public void onResume(){
        super.onResume();
        newActivityLaunched = false;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        menu.findItem(R.id.menu_item_latin_names).setVisible(areTranslationsAvailable(getResources()));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        checkMenuItems(this, menu);
        return true;
    }

    static public void checkMenuItems(Activity activity, Menu menu){
        boolean isHard =
                activity.getSharedPreferences(activity.getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
                        .getBoolean(activity.getString(R.string.preferences_only_family), false);
        menu.findItem(R.id.menu_item_difficult).setChecked(isHard);

        boolean isLatin =
                activity.getSharedPreferences(activity.getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
                        .getBoolean(activity.getString(R.string.preferences_latin_names), false);
        menu.findItem(R.id.menu_item_latin_names).setChecked(isLatin);//I hope this doesn't unhide the item, if hidden ///unhide == opposite of hyde == jekyll
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_item_difficult) {
            boolean isHard = !item.isChecked();
            item.setChecked(isHard);
            SharedPreferences.Editor preferences =
                    getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).edit();
            preferences.putBoolean(getString(R.string.preferences_only_family), isHard);
            preferences.commit();
            return true;
        }else if(id == R.id.menu_item_latin_names){
            boolean isLatin = !item.isChecked();
            item.setChecked(isLatin);
            SharedPreferences.Editor preferences =
                    getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).edit();
            preferences.putBoolean(getString(R.string.preferences_latin_names), isLatin);
            preferences.commit();
            return true;
        }else if(id == R.id.menu_item_about){
            Intent intent = new Intent(this, ActivityAbout.class);
            intent.putExtra("PlayTime", getAnimatorPlayTime());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchActivity(View view) {
        if(newActivityLaunched){
            return;
        }else{
            newActivityLaunched = true;
        }
        Intent intent = null;
        int idx = getCompositeIndex(view);
        boolean onlyFamily;
        switch(idx) {
        case 0:
            intent = new Intent(this, ActivityGame.class);
            intent.putExtra("GameType", "picture");
            intent.putExtra("NumQuestions", 10);
            intent.putExtra("CorrectAnswers", 0);
            onlyFamily =
                    getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
                    .getBoolean(getString(R.string.preferences_only_family), false);
            intent.putExtra("OnlyFamily", onlyFamily);
        break;
        case 1:
            intent = new Intent(this, ActivityGame.class);
            intent.putExtra("GameType", "name");
            intent.putExtra("NumQuestions", 10);
            intent.putExtra("CorrectAnswers", 0);
            onlyFamily =
                    getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
                            .getBoolean(getString(R.string.preferences_only_family), false);
            intent.putExtra("OnlyFamily", onlyFamily);
        break;
        case 2:
            intent = new Intent(this, ActivityGame.class);
            intent.putExtra("GameType", "cheep");
            intent.putExtra("NumQuestions", 5);
            intent.putExtra("CorrectAnswers", 0);
        break;
        case 3:
            intent = new Intent(this, ActivityCards.class);
            intent.putExtra("TypeFragment", "cards");
        break;
        }
        intent.putExtra("PlayTime", getAnimatorPlayTime());

        startActivity(intent);
    }

    public void startBiggerIcon(View icon, int idx){
        Resources res = getResources();
        bigIconAnimators[idx] = ValueAnimator.ofInt((int) res.getDimension(R.dimen.button_icon_size), (int) res.getDimension(R.dimen.button_big_icon_size));
        bigIconAnimators[idx].setDuration(res.getInteger(R.integer.button_touch_duration));
        bigIconAnimators[idx].addUpdateListener(new ActivityAnimation.SizeSetter(icon));
        bigIconAnimators[idx].start();
    }

    public void startSmallerIcon(int idx){
        bigIconAnimators[idx].reverse();
    }

    public void resetIconSizes(){
        int iconSize = (int) getResources().getDimension(R.dimen.button_icon_size);
        int[] composites = {R.id.composite0, R.id.composite1, R.id.composite2, R.id.composite3};
        for(int i=0; i<4; i++) {
            View icon = findViewById(composites[i]).findViewWithTag("icon");
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) icon.getLayoutParams();
            params.height = params.width = iconSize;
            icon.setLayoutParams(params);
        }
    }

    public void removeCompositeListeners(){
        int[] composites = {R.id.composite0, R.id.composite1, R.id.composite2, R.id.composite3};
        for(int i=0; i<4; i++) {
            findViewById(composites[i]).findViewWithTag("button").setOnClickListener(null);
        }
    }

    public static int getCompositeIndex(View v){
        switch(((View) v.getParent()).getId()) {
            case R.id.composite0:                return 0;
            case R.id.composite1:                return 1;
            case R.id.composite2:                return 2;
            case R.id.composite3:                return 3;
            default:                             return -1;
        }
    }

    static int sum(int[] array){
        int sum = 0;
        for (int i : array) {
            sum += i;
        }
        return sum;
    }

    static public void inflateBirds(Context context, Map<String, Bird> birdsByName, Map<String, List<Bird>> birdsByFamily) {
        Resources res = context.getResources();
        InputStream ISPicturesDataFile = res.openRawResource(R.raw.pictures_data_file);
        if (ISPicturesDataFile != null) {
            InputStreamReader inputreader = new InputStreamReader(ISPicturesDataFile);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line;
            try {
                while ((line = buffreader.readLine()) != null) {
                    Picture pic = createPicture(context, line);
                    String name = pic.latinName;
                    Bird bird;
                    if (!birdsByName.containsKey(name)) {
                        //Need to create new bird
                        bird = new Bird(name, pic.family);
                        //Add it to 1st list
                        birdsByName.put(name, bird);
                        //Add it to 2nd list
                        if(birdsByFamily != null) {
                            if (!birdsByFamily.containsKey(pic.family)) {
                                //but its family is new, so create it
                                birdsByFamily.put(pic.family, new ArrayList<Bird>());
                            }
                            //Now yes, add bird to 2nd list
                            birdsByFamily.get(pic.family).add(bird);
                        }
                    } else {
                        //bird is already created and in lists
                        bird = birdsByName.get(name);
                    }
                    //add picture
                    bird.addPicture(pic);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InputStream ISCheepsDataFile = res.openRawResource(R.raw.cheeps_data_file);
        if (ISCheepsDataFile != null) {
            InputStreamReader inputreader = new InputStreamReader(ISCheepsDataFile);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line;
            try {
                while ((line = buffreader.readLine()) != null) {
                    Cheep cheep = createCheep(context, line);
                    String name = cheep.latinName;
                    Bird bird;
                    if (!birdsByName.containsKey(name)) {
                        bird = new Bird(name, null);
                        birdsByName.put(name, bird);
                    } else {
                        bird = birdsByName.get(name);
                    }
                    bird.addCheep(cheep);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static public Picture createPicture(Context context, String line) {
        String[] fields = line.split("\t");
        String fileName = fields[0];
        String family = fields[1];
        String latinName = fields[2];
        String author = fields[3];
        String contact = fields[4];
        String licence = fields[5];
        String wikilink = fields[6];
//        int id = context.getResources().getIdentifier(fileName, "drawable", context.getPackageName());
        int id = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
        return new Picture(id, family, latinName, author, contact, licence, wikilink);
    }

    static public Cheep createCheep(Context context, String line) {
        String[] fields = line.split("\t");
        String fileName = fields[0];
        String latinName = fields[1];
        String author = fields[2];
        String contact = fields[3];
        int id = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
        return new Cheep(id, latinName, author, contact);
    }

    static public String androidize(String str) {
        //str without special chars, only (a-zA-Z0-9_)
        return str.toLowerCase().replace(" ", "_");
    }

    static public int getCommonNameID(Context context, String latinName) {
        //returns zero if resource is not found.
        int id = context.getResources().getIdentifier(ActivityMain.androidize(latinName), "string", context.getPackageName());
        // getIdentifier returns an ID != 0 for strings that exist in other locale but not in current one
        // That's nasty, so check it and eventually fix id
        try{
            context.getResources().getString(id);
        }catch(Resources.NotFoundException e){
            id = 0;
        }
        return id;
    }

    static public String getCommonName(Context context, String latin){
        String commonName;
        try{
            commonName = context.getResources().getString(getCommonNameID(context, latin));
        }catch(Resources.NotFoundException e){
            commonName = "";
        }
        return commonName;
    }

    static public String getContactURL(String str){
        if("None".equals(str)){
            return "";
        }
        String site;
        String id;
        String[] fields = str.split(":");
        if(fields.length < 2){
            site = "";
            id = str;
        }else{
            site = fields[0];
            id = fields[1];
        }
        if(site.equals("wikimedia")){
            return "https://commons.wikimedia.org/wiki/User:" + id.replace("\\", "/");
        }else if(site.equals("flickr")) {
            return "https://www.flickr.com/people/" + id;
        }else if(site.equals("web")) {
            return "http://" + id.replace("\\", "/");
        }else if(site.equals("")){
            return str;
        }else{
            return str;
        }
    }

    static public String getUrlHtml(String str){
        if("None".equals(str)){
            return "";
        }
        String start = "<a href=\"";

        String site;
        String id;
        String[] fields = str.split(":");
        if(fields.length < 2){
            site = "";
            id = str;
        }else{
            site = fields[0];
            id = fields[1];
        }
        if(site.equals("wikimedia")){//wikimedia:Civyshk
            return start + "https://commons.wikimedia.org/wiki/User:" + id.replace("\\", "/") + "\">" + id.replace("\\", "/") + "</a>";
        }else if(site.equals("flickr")) {
            return start + "https://www.flickr.com/people/" + id + "\">" + id + "</a>";
        }else if(site.equals("web")) {//web:www.project104.net
            return start + "http://" + id.replace("\\", "/") + "\">" + id.replace("\\", "/") + "</a>";
        }else if(site.equals("http")){//http://www.project104.net
            return start + str + "\">" + id.substring(2) + "</a>";
        }else if(site.equals("")){
            return start + "http://" + str + "\">"+str+"</a>";
        }else{
            return start + "http://" + str + "\">"+str+"</a>";
        }
    }

    static public boolean areTranslationsAvailable(Resources res){
        try{
            String language = res.getString(R.string.check_language);
        }catch(Resources.NotFoundException e){
            return false;
        }
        return true;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    static public float convertDpToPixel(float dp, Context context){
        return dp * (context.getResources().getDisplayMetrics().densityDpi / 160f);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    static public float convertPixelsToDp(float px, Context context){
        return px / (context.getResources().getDisplayMetrics().densityDpi / 160f);
    }	

    static public void startPictureInfoActivity(ActivityAnimation activity, Picture pic) {
        Intent intent = new Intent(activity, ActivityPictureInfo.class);
        intent.putExtra("Picture", pic);
        intent.putExtra("PlayTime", activity.getAnimatorPlayTime());
        activity.startActivity(intent);
    }

    static public void startCardActivity(ActivityAnimation activity, String birdName){
        Intent intent = new Intent(activity, ActivityCards.class);
        intent.putExtra("BirdName", birdName);
        intent.putExtra("PlayTime", activity.getAnimatorPlayTime());
        intent.putExtra("TypeFragment", "birdName");
        activity.startActivity(intent);
    }

    /*Async loading of bitmaps:::
    */

    static private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //taken from developer.android.com

        if(reqWidth == 0 || reqHeight == 0){
            return 1;
        }

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        //My version
        int nextInSampleSize = 2;
        while((height / nextInSampleSize) > reqHeight && (width / nextInSampleSize) > reqWidth){
            inSampleSize = nextInSampleSize;
            nextInSampleSize *= 2;
        }

        /* Google version
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }*/

        return inSampleSize;
    }

    static private Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        //taken from developer.android.com

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        //Log.d(LOG_TAG, "View " + reqWidth + " x " + reqHeight + " px");

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        //return BitmapFactory.decodeResource(res, resId, options);
        //no! it could throw OOM. Reduce inSampleSize even further

        Bitmap bitmap = null;
        for (; options.inSampleSize <= 32; options.inSampleSize *= 2) {
            try {
                bitmap = BitmapFactory.decodeResource(res, resId, options);
                //Log.d(LOG_TAG, "Decoded successfully for sampleSize " + options.inSampleSize);
                break;
            } catch (OutOfMemoryError outOfMemoryError) {
                //Log.e(LOG_TAG, "outOfMemoryError while reading file for sampleSize " + options.inSampleSize + " retrying with higher value");
            }
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }

        return bitmap;
    }

    static class BitmapDecoderTask extends AsyncTask<Integer, Void, Bitmap> {
        private Resources res;
        private final WeakReference<ImageView> imageViewReference;
        private int resId = 0;
        private int width, height;

        public BitmapDecoderTask(Resources res, ImageView imageView, int w, int h) {
            this.res = res;
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
            width = w;
            height = h;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            resId = params[0];
            return decodeSampledBitmapFromResource(res, resId, width, height);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(isCancelled()){
                bitmap = null;
            }

            if (imageViewReference != null &&  bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapDecoderTask bitmapDecoderTask = getBitmapDecoderTask(imageView);
                if (this == bitmapDecoderTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static public void setScaledImageResource(Resources res, ImageView v, int resId, boolean delayed){
        final int width = v.getWidth();
        final int height = v.getHeight();
        if(!delayed){
            v.setImageBitmap(decodeSampledBitmapFromResource(res, resId, width, height));
        }else{
            //TODO
            // https://developer.android.com/intl/es/training/displaying-bitmaps/process-bitmap.html
            // http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html

            boolean needToExecuteDecoder = cancelObsoleteDecoderTask(resId, v);

            if (needToExecuteDecoder) {
                final BitmapDecoderTask task = new BitmapDecoderTask(res, v, width, height);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(task);
                v.setImageDrawable(asyncDrawable);
                task.execute(resId);
            }

        }
    }

    static public boolean cancelObsoleteDecoderTask(int resId, ImageView imageView) {
        //returns false if there's a working decoder already decoding 'resId'
        final BitmapDecoderTask bitmapWorkerTask = getBitmapDecoderTask(imageView);

        if (bitmapWorkerTask != null) {
            final int currentDecoderResId = bitmapWorkerTask.resId;
            // If currentDecoderResId is not yet set or it differs from the new resId
            if (currentDecoderResId == 0 || currentDecoderResId != resId) {
                // Cancel obsolete task
                bitmapWorkerTask.cancel(true);
            } else {
                // It's running but it's already decoding what we want (resId)
                return false;
            }
        }
        return true;
    }

    static private BitmapDecoderTask getBitmapDecoderTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapDecoderTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends ColorDrawable {
        //1. It shows a drawable while the real bitmap gets ready
        //2. It saves a reference to the last task decoding a bitmap to substitute this drawable
        private final WeakReference<BitmapDecoderTask> bitmapWorkerTaskReference;

        public AsyncDrawable(BitmapDecoderTask bitmapDownloaderTask) {
            super(Color.TRANSPARENT);
            bitmapWorkerTaskReference = new WeakReference<BitmapDecoderTask>(bitmapDownloaderTask);
        }

        public BitmapDecoderTask getBitmapDecoderTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}
