package net.project104.civyshkbirds;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

public class PlayerFragment extends Fragment{

    private enum MediaPlayerState{
        NULL, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACK_COMPLETED
    }
    private MediaPlayer mCheepPlayer;
    MediaPlayerState cheepPlayerState = MediaPlayerState.NULL;
    int currentUserVol;//0-10
    static final int maxUserVol = 10;
    static final float minPlayerVol = 0.05f;
    List<Cheep> mCheeps;
    int mCheepIndex = 0;
    boolean mustStartPlayer = false;
    boolean mVisible = true;
    boolean mIsSavedPlayerState = false;

    // memory leaks in this listeners are avoided by removing the listeners in OnDestroyView

    View.OnClickListener listenerPlayer = new View.OnClickListener(){
        //This holds and implicit reference to activity
        @Override
        public void onClick(View v) {
            int id = ((View) v.getParent()).getId();
            if(id == R.id.compositePlayPause){
                onPlayPausePlayer();
            }else if(id == R.id.compositeStop){
                onStopPlayer();
            }else if(id == R.id.compositeVolUp){
                onVolUpPlayer();
            }else if(id == R.id.compositeVolDown){
                onVolDownPlayer();
            }else if(id == R.id.compositePlaylistPrev){
                onPlaylist("prev");
            }else if(id == R.id.compositePlaylistNext){
                onPlaylist("next");
            }
        }
    };

    View.OnTouchListener touchListenerPlayer = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    ((ViewGroup) v.getParent()).findViewWithTag("picture").setAlpha(0.5f);
                    if(getActivity() instanceof ActivityGame) {
                        ((ActivityGame) getActivity()).cancelCountdown();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    ((ViewGroup) v.getParent()).findViewWithTag("picture").setAlpha(1.f);
                    break;
            }
            return false;
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        if(savedInstanceState != null){
            mVisible = savedInstanceState.getBoolean("Visible");
            if(mVisible){
                mustStartPlayer = savedInstanceState.getBoolean("MustStartPlayer");
                mCheepIndex = savedInstanceState.getInt("CheepIndex");
                mCheeps = (List) savedInstanceState.getSerializable("Cheeps");

                setUpPlayer(mCheeps, mCheepIndex, currentUserVol, mustStartPlayer);

                //createCheepPlayer();
                updateVolumeMeter();
            }else{
                hidePlayer();
            }
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        //cancelPlayingCheep();
        savedInstanceState.putBoolean("Visible", mVisible);
        if (mVisible) {
            if(!mIsSavedPlayerState){
                mIsSavedPlayerState = true;
                mustStartPlayer = (cheepPlayerState == MediaPlayerState.STARTED);
            }
            savedInstanceState.putBoolean("MustStartPlayer", mustStartPlayer);
            savedInstanceState.putInt("CheepIndex", mCheepIndex);
            savedInstanceState.putSerializable("Cheeps", (Serializable) mCheeps);
            //savedInstanceState.putInt("CurrentUserVol", currentUserVol);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause(){
        if(!mIsSavedPlayerState){
            mIsSavedPlayerState = true;
            mustStartPlayer = (cheepPlayerState == MediaPlayerState.STARTED);
        }
        stopCheepPlayer();
        //cancelPlayingCheep();
        saveUserVol();
        super.onPause();
    }

    @Override
    public void onDestroyView(){
        removeListeners();
        super.onDestroyView();
    }

    @Override
    public void onResume(){
        int defaultUserVol = getActivity().getResources().getInteger(R.integer.default_user_vol);
        currentUserVol = getActivity().getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).getInt(getString(R.string.preferences_user_vol), defaultUserVol);
        super.onResume();
    }

    public void onPlayPausePlayer(){
        if(cheepPlayerState == MediaPlayerState.STARTED){
            pauseCheepPlayer();
        }else{
            startCheepPlayer();
        }
    }

    public void onStopPlayer(){
        stopCheepPlayer();
    }

    public void onVolUpPlayer(){
        if(currentUserVol < maxUserVol){
            currentUserVol += 1;
            float scalarVol = calculateScalarVol(minPlayerVol, maxUserVol, currentUserVol);
            mCheepPlayer.setVolume(scalarVol, scalarVol);
            updateVolumeMeter();
        }
    }

    public void onVolDownPlayer() {
        if (currentUserVol > 0) {
            currentUserVol -= 1;
            float scalarVol = calculateScalarVol(minPlayerVol, maxUserVol, currentUserVol);
            mCheepPlayer.setVolume(scalarVol, scalarVol);
            updateVolumeMeter();
        }
    }

    public void onPlaylist(String where){
        if(where.equals("next")){
            goNextCheep();
        }else if(where.equals("prev")){
            goPrevCheep();
        }
    }

    private void goPrevCheep(){
        mCheepIndex -= 1;
        if(mCheepIndex < 0){
            mCheepIndex = mCheeps.size() -1;
        }
        MediaPlayerState oldState = cheepPlayerState;
        cancelPlayingCheep();
        createCheepPlayer();
        if(oldState == MediaPlayerState.STARTED){
            startCheepPlayer();
        }
    }

    private void goNextCheep(){
        mCheepIndex += 1;
        if(mCheepIndex == mCheeps.size()){
            mCheepIndex = 0;
        }
        MediaPlayerState oldState = cheepPlayerState;
        cancelPlayingCheep();
        createCheepPlayer();
        if(oldState == MediaPlayerState.STARTED){
            startCheepPlayer();
        }
    }

    public void startCheepPlayer(){
        if (cheepPlayerState == MediaPlayerState.STOPPED){
            try{
                mCheepPlayer.prepare();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        mCheepPlayer.start();
        cheepPlayerState = MediaPlayerState.STARTED;
        updatePlayButton();
    }

    public void pauseCheepPlayer(){
        if(cheepPlayerState == MediaPlayerState.STARTED){
            mCheepPlayer.pause();
            cheepPlayerState = MediaPlayerState.PAUSED;
        }
        updatePlayButton();
    }

    public void stopCheepPlayer(){
        if(cheepPlayerState == MediaPlayerState.STARTED){
            mCheepPlayer.stop();
            cheepPlayerState = MediaPlayerState.STOPPED;
            updatePlayButton();
        }
    }

    public void updateVolumeMeter(){
        ((ViewMeterForeground) getView().
                findViewById(R.id.compositeVolume).
                findViewWithTag("foreground")).requestLayout();
    }

    private void updatePlayButton(){
        int iconID;
        if(cheepPlayerState == MediaPlayerState.STARTED) {
            iconID = R.drawable.icon_play_pause_playing;
        }else{
            iconID = R.drawable.icon_play_pause;
        }
        ActivityMain.setScaledImageResource(getResources(),
                (ImageView) getView().findViewById(R.id.compositePlayPause).findViewWithTag("picture"),
                iconID, false);
    }

    static public float calculateScalarVol(float playerMinVol, int userMaxVol, int userVol){
        if(userVol <= 0){
            return 0.0f;
        }else{
            // meter_vertical = minPlayer * r^(user/(userMax-1))
            // r = (1/minPlayer)^(1/(userMax-1))
            return (float)(Math.pow(playerMinVol, 1.0 - (userVol-1)/(userMaxVol - 1.0)));
        }
    }

    public void createCheepPlayer(){
        mCheepPlayer = MediaPlayer.create(getActivity(), mCheeps.get(mCheepIndex).ID);
        float playerVol = calculateScalarVol(minPlayerVol, maxUserVol, currentUserVol);
        mCheepPlayer.setVolume(playerVol, playerVol);
        mCheepPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            //implicit reference to containing fragment
            @Override
            public void onCompletion(MediaPlayer mp) {
                //remove this listener to avoid memory leaks (in a hidden saved instance of this fragment)
                cheepPlayerState = MediaPlayerState.PLAYBACK_COMPLETED;
                updatePlayButton();
            }
        });
        cheepPlayerState = MediaPlayerState.PREPARED;
        if (mustStartPlayer){
            startCheepPlayer();
        }
        updatePlayButton();

        ((TextView) getView().findViewById(R.id.tvAuthor)).setText(mCheeps.get(mCheepIndex).author);
        ((TextView) getView().findViewById(R.id.tvContact)).setText(Html.fromHtml(
                "<a href=\"http://" + mCheeps.get(mCheepIndex).contact + "\">" + mCheeps.get(mCheepIndex).contact + "</a>"
        ));
    }

    public void cancelPlayingCheep(){
        if(mCheepPlayer != null){
            mCheepPlayer.release();
            mCheepPlayer = null;
            cheepPlayerState = MediaPlayerState.NULL;
            updatePlayButton();
        }
    }

    public void setUpPlayer(List<Cheep> cheeps, int idx, int vol, boolean start){
        ViewGroup compositePlayer = (ViewGroup) getView().findViewById(R.id.compositePlayer);
        int[] playerCompositesIDs = {R.id.compositePlayPause, R.id.compositeStop, R.id.compositeVolUp, R.id.compositeVolDown, R.id.compositePlaylistPrev, R.id.compositePlaylistNext};
        for(int i=0; i<playerCompositesIDs.length; i++) {
            ViewGroup compositeButton = (ViewGroup) compositePlayer.findViewById(playerCompositesIDs[i]);
            Button button = (Button) compositeButton.findViewWithTag("button");
            //remove these listeners
            button.setOnClickListener(listenerPlayer);
            button.setOnTouchListener(touchListenerPlayer);
        }

        //this is the ViewMeterForeground, in case I want to remove the PlayerFragment later
        ((ViewMeterForeground) compositePlayer.
                findViewById(R.id.compositeVolume).
                findViewWithTag("foreground")).setPlayerFragment(this);

        ((TextView) compositePlayer.findViewById(R.id.tvContact)).setMovementMethod(LinkMovementMethod.getInstance());

        mustStartPlayer = start;

        mCheeps = cheeps;
        if(mCheeps.size() < 2){
            //TODO disable buttons & lower alpha to 0.5
            hidePlaylistButtons();
        }
        mCheepIndex = idx;

        currentUserVol = vol;
        createCheepPlayer();
    }

    private void removeListeners(){
        //remove button listeners
        ViewGroup compositePlayer = (ViewGroup) getView().findViewById(R.id.compositePlayer);
        if(compositePlayer != null) {
            int[] playerCompositesIDs = {R.id.compositePlayPause, R.id.compositeStop, R.id.compositeVolUp, R.id.compositeVolDown, R.id.compositePlaylistPrev, R.id.compositePlaylistNext};
            for (int i = 0; i < playerCompositesIDs.length; i++) {
                ViewGroup compositeButton = (ViewGroup) compositePlayer.findViewById(playerCompositesIDs[i]);
                Button button = (Button) compositeButton.findViewWithTag("button");
                button.setOnClickListener(null);
                button.setOnTouchListener(null);
            }

            //remove association between fragment and volumemeter
            ((ViewMeterForeground) compositePlayer.
                    findViewById(R.id.compositeVolume).
                    findViewWithTag("foreground")).setPlayerFragment(null);
        }
        //remove player listener to update playButton
        //It's probably not necessary as mCheepPlayer is already set to null
        if(mCheepPlayer != null) {
            mCheepPlayer.setOnCompletionListener(null);
        }
    }

    private void hidePlaylistButtons(){
        getView().findViewById(R.id.compositePlaylistPrev).setVisibility(View.GONE);
        getView().findViewById(R.id.compositePlaylistNext).setVisibility(View.GONE);
    }

    public void hidePlayer(){
        mVisible = false;
        View viewPlayer = getView().findViewById(R.id.layoutPlayer);
        if(viewPlayer == null){
            viewPlayer = getView().findViewById(R.id.compositePlayer);
        }
        if(viewPlayer != null){
            viewPlayer.setVisibility(View.GONE);
        }
    }

    private void saveUserVol(){
        SharedPreferences.Editor preferences = getActivity().getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).edit();
        preferences.putInt(getString(R.string.preferences_user_vol), currentUserVol);
        preferences.commit();
    }

}
