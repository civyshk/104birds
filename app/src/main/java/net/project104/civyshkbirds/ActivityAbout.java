package net.project104.civyshkbirds;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class ActivityAbout extends ActivityAnimation{

    protected void onCreate(Bundle savedInstanceState){
        Bundle bundle;
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
        } else {
            bundle = savedInstanceState;
        }
        super.onCreate(savedInstanceState, bundle, R.layout.activity_about);

        TextView tvInfo = (TextView) findViewById(R.id.tvAuthor);
        Resources res = getResources();
        InputStream inputStream = res.openRawResource(R.raw.about_author_data_file);
        if(inputStream != null){
            InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line;
            try {
                String header = res.getString(R.string.about_author_header);
                String name = buffreader.readLine();
                String license = buffreader.readLine();
                String url = buffreader.readLine();
                tvInfo.setMovementMethod(LinkMovementMethod.getInstance());
                tvInfo.setText(Html.fromHtml("<b>"+header+"</b><br><br>"+name+"<br>"+license+"<br>"+
                        ActivityMain.getUrlHtml(url) +
                        "<br><br>"+res.getString(R.string.app_icon_attribution)));
            }catch (IOException e){
                //Mala suerte
            }
        }

        int[] textViews = {R.id.tvPeopleAudio, R.id.tvPeoplePictures};
        String[] headers = {res.getString(R.string.about_audio_header), res.getString(R.string.about_pictures_header)};
        int[] dataFileIDs = {R.raw.about_audio_data_file, R.raw.about_pictures_data_file};

        for(int i=0; i<textViews.length; i++) {
            tvInfo = (TextView) findViewById(textViews[i]);
            StringBuilder finalText = new StringBuilder();
            finalText.append("<b>" + headers[i] + "</b><br>");
            inputStream = res.openRawResource(dataFileIDs[i]);
            if (inputStream != null) {
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                try {
                    while ((line = buffreader.readLine()) != null) {
                        String[] tokens = line.split(Pattern.quote("\t"));
                        if (tokens.length == 3) {
                            finalText.append("<br>" + tokens[0] + " " + ActivityMain.getUrlHtml(tokens[1]) + " " + tokens[2]);
                        }
                    }
                } catch (IOException e) {
                    //Mala suerte
                }finally{
                    tvInfo.setMovementMethod(LinkMovementMethod.getInstance());
                    tvInfo.setText(Html.fromHtml(finalText.toString()));
                }
            }
        }
    }

}