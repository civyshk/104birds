package net.project104.civyshkbirds;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.project104.civyshkbirds.R;

import java.util.ArrayList;
import java.util.List;


public class ActivityPictureInfo extends ActivityAnimation {

    Picture mPicture;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle;
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
        } else {
            bundle = savedInstanceState;
        }
        super.onCreate(savedInstanceState, bundle, R.layout.activity_picture_info);

        mPicture = (Picture) bundle.getSerializable("Picture");
        ((TextView) findViewById(R.id.tvLatinName)).setText(mPicture.latinName);
        String commonName = getCommonName(mPicture.latinName);
        if(!commonName.equals("")){
            ((TextView) findViewById(R.id.tvCommonName)).setText(commonName);
        }else{
            findViewById(R.id.tvCommonName).setVisibility(View.GONE);
        }

        boolean publicDomain = showLicence(mPicture.licence);
        String authorField;
        if(!publicDomain){
            authorField = getResources().getString(R.string.author);
        }else{
            authorField = getResources().getString(R.string.user);
        }

        String contact = ActivityMain.getContactURL(mPicture.contact);
        if(contact != null) {
            ((TextView) findViewById(R.id.tvAuthor)).setText(
                    Html.fromHtml(authorField + ": <a href=\"" + ActivityMain.getContactURL(mPicture.contact)
                            + "\">" + mPicture.author));
        }else{
            ((TextView) findViewById(R.id.tvAuthor)).setText((authorField + ": "+ mPicture.author));
        }
        ((TextView) findViewById(R.id.tvAuthor)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.tvWikimediaPicture)).setText(
            Html.fromHtml("<a href=\"" + getFullWikilink(mPicture.wikilink) + "\">" +
                    getResources().getString(R.string.see_wikimedia_picture) + "</a>")
        );
        ((TextView) findViewById(R.id.tvWikimediaPicture)).setMovementMethod(LinkMovementMethod.getInstance());
        //((ImageViewScaling) findViewById(R.id.imgPicture)).setImageResource(mPicture.getID());
        //ActivityMain.setScaledImageResource(getResources(), (ImageViewScaling) findViewById(R.id.imgPicture), mPicture.getID(), false);
        ActivityMain.ImagesLoader imagesLoader = new ActivityMain.ImagesLoader(getResources());
        imagesLoader.addImage((ImageView) findViewById(R.id.imgPicture), mPicture.getID(), false);
        imagesLoader.imageViews.get(0).getViewTreeObserver().addOnPreDrawListener(imagesLoader);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putSerializable("Picture", mPicture);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_picture_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    private boolean showLicence(String str){
        String url = "";
        Resources res = getResources();
        String title = res.getString(R.string.creative_commons);
        View composite = findViewById(R.id.compositeLicence);
        boolean publicDomain = false;
        if(str.substring(0,2).equals("CC")) {
            url = "https://creativecommons.org/licenses/";
            List<String> fields = new ArrayList<String>();
            assert str.length() % 2 == 0;
            while (str.length() > 0) {
                fields.add(str.substring(0, 2));
                str = str.substring(2);
            }
            for (String field : fields) {
                if (field.equals("CC")) {
                    // pass
                } else if (field.equals("BY")) {
                    //composite.findViewWithTag("Attribution").setVisibility(View.VISIBLE);
                    url += "by";
                } else if (field.equals("SA")) {
                    composite.findViewWithTag("shareAlike").setVisibility(View.VISIBLE);
                    url += "-sa";
                } else if (field.equals("NC")) {
                    Log.d(ActivityMain.LOG_TAG, "Using picture with no-commercial licence");
                }else if (field.equals("PD")){
                    composite.findViewWithTag("attribution").setVisibility(View.GONE);
                    url = "https://creativecommons.org/publicdomain/zero";
                    title += " Zero";
                    ActivityMain.setScaledImageResource(getResources(), (ImageView) composite.findViewWithTag("title").findViewWithTag("image"), R.drawable.icon_cc_zero, false);
                    publicDomain = true;
                } else {
                    title += " " + field.charAt(0) + "." + field.charAt(1);
                    url += "/" + field.charAt(0) + "." + field.charAt(1);
                }
            }
        }else if(str.equals("PD")){
            title = getResources().getString(R.string.public_domain);
            url = "https://en.wikipedia.org/wiki/Public_domain";
            composite.findViewWithTag("attribution").setVisibility(View.GONE);
            ActivityMain.setScaledImageResource(getResources(), (ImageView) composite.findViewWithTag("title").findViewWithTag("image"), R.drawable.icon_pd, false);
            publicDomain = true;
        }else{
            Log.d(ActivityMain.LOG_TAG, "Using picture without CC licence");
        }
        ((TextView) composite.findViewWithTag("title").findViewWithTag("text")).setText(
            Html.fromHtml(
                "<a href=\""+ url +"\">" + title + "</a>"));
        ((TextView) composite.findViewWithTag("title").findViewWithTag("text")).setMovementMethod(LinkMovementMethod.getInstance());
        return publicDomain;
    }



    static public String getFullWikilink(String wikiFileName){
        return "https://commons.wikimedia.org/wiki/File:" + wikiFileName;
    }

    public String getCommonName(String latinName){//method also in ActivityGame
        Resources res = getResources();
        String commonName;
        try {
            commonName = res.getString(res.getIdentifier(androidize(latinName), "string", getPackageName()));
        }catch(Resources.NotFoundException e){
            commonName = "";
        }
        return commonName;
    }

    static public String androidize(String str){//method also in ActivityGame
        //str without special chars, only (a-zA-Z0-9_)
        return str.toLowerCase().replace(" ", "_");
    }

}
