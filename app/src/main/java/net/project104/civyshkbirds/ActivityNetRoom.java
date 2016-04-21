package net.project104.civyshkbirds;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.project104.swartznetlibrary.JmdHelper;
import net.project104.swartznetlibrary.NetHandler;
import net.project104.swartznetlibrary.NetNode;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ActivityNetRoom extends AppCompatActivity {
    public final String TAG = getClass().getSimpleName();

    private NetGame mNetGame;


    private NetPlayer mPlayer;

    boolean mSelfHosting;
    Map<NetNode, ValueAnimator> mBlinkAnimatorsByPlayer;
    Map<NetNode, WeakReference<ViewGroup>> mWidgetsByPlayer;


    static class AlphaSetter implements ValueAnimator.AnimatorUpdateListener{
        WeakReference<View> mView;
        public AlphaSetter(View view){
            this.mView = new WeakReference<>(view);
        }
        @Override
        public void onAnimationUpdate(ValueAnimator animation){
            View view = mView.get();
            if(view != null) {
                view.setAlpha((Float) animation.getAnimatedValue());
            }
        }
    }

    //TODO implement these listeners

    static class RoomNsdRegistrationListener implements JmdHelper.RegistrationListener {
        private WeakReference<ActivityNetRoom> mActivity;

        public RoomNsdRegistrationListener(ActivityNetRoom activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onServiceRegistered(String name) {
            ActivityNetRoom activity = mActivity.get();
            if (activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.you_are_the_host));
                activity.setTvText(tvName, name);

                //TODO add self player
                activity.addPlayerWidget(activity.mPlayer);
            }
        }

        @Override
        public void onRegistrationFailed() {
            ActivityNetRoom activity = mActivity.get();
            if (activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_hosting_failed));
                activity.setTvText(tvName, "");
            }
        }
    }

    static class RoomNsdDiscoveryListener implements JmdHelper.DiscoveryListener {
        private WeakReference<ActivityNetRoom> mActivity;
        public RoomNsdDiscoveryListener(ActivityNetRoom activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onDiscoveryStarted() {
            ActivityNetRoom activity = mActivity.get();
            if(activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_discovery_started));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onServiceFound() {
            ActivityNetRoom activity = mActivity.get();
            if(activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_discovery_succeeded));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onServiceLost() {
            ActivityNetRoom activity = mActivity.get();
            if(activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_host_stopped_announcing));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onDiscoveryStopped() {
            ActivityNetRoom activity = mActivity.get();
            if(activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_discovery_stopped));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onStartDiscoveryFailed() {
            ActivityNetRoom activity = mActivity.get();
            if(activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_discovery_failed));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onStopDiscoveryFailed() {
            ActivityNetRoom activity = mActivity.get();
            if(activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.cant_stop_discovery));
                activity.setTvText(tvName, "");
            }
        }
    }

    static class RoomNsdResolveListener implements JmdHelper.ResolveListener{
        private WeakReference<ActivityNetRoom> mActivity;
        public RoomNsdResolveListener(ActivityNetRoom activity){
            mActivity = new WeakReference<ActivityNetRoom>(activity);
        }

        @Override
        public void onServiceResolved(String name, InetAddress address, int port) {
            ActivityNetRoom activity = mActivity.get();
            if(activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.server_found));
                activity.setTvText(tvName, name);
                //TODO client should connect to that resolved service //TODO
                //TODO
                //TODO
                activity.mNetGame.startClient(address, port, activity.getResources().getString(R.string.player_name));
            }else{
                //TODO there's a server ready but activity is off. Then? Start connection too?
                //Leave a message in a queue, and activityRoom should connect when it's up again?
                //How?
            }
        }

        @Override
        public void onResolveFailed() {
            ActivityNetRoom activity = mActivity.get();
            if(activity != null) {
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.cant_get_server_info));
                activity.setTvText(tvName, "");
            }
        }
    }

    public NetGame.RoomListener getRoomListener(){
        return new NetGame.RoomListener() {
            private WeakReference<ActivityNetRoom> mActivity
                    = new WeakReference<>(ActivityNetRoom.this);

            @Override
            public void onPlayerAdd(NetPlayer player) {
                ActivityNetRoom activity = mActivity.get();
                if (activity != null) {
                    activity.addPlayerWidget(player);
                }
            }

            @Override
            public void onPlayerRemove(NetPlayer player) {
                ActivityNetRoom activity = mActivity.get();
                if (activity != null) {

                }
            }

            @Override
            public void onPlayerSwitchRequested(NetPlayer player, int team) {
                ActivityNetRoom activity = mActivity.get();
                if (activity != null) {

                }
            }

            @Override
            public void onPlayerSwitchConfirmed(NetPlayer player, int team) {
                ActivityNetRoom activity = mActivity.get();
                if (activity != null) {

                }
            }

            @Override
            public void onPlayerSwitchRejected(NetPlayer player) {
                ActivityNetRoom activity = mActivity.get();
                if (activity != null) {

                }
            }

            @Override
            public void onStartGame(int seconds) {
                ActivityNetRoom activity = mActivity.get();
                if (activity != null) {
                    Toast.makeText(activity, "Start in " + String.valueOf(seconds), Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Bundle bundle;
        if(savedInstanceState == null){
            bundle = getIntent().getExtras();
        }else{
            bundle = savedInstanceState;
        }

        mSelfHosting = bundle.getBoolean("SelfHosting");
        mBlinkAnimatorsByPlayer = new HashMap<>();
        mWidgetsByPlayer = new HashMap<>();

        //TODO mPlayer = new NetPlayer(null, -1);

        //start NetConnection & NSD

        Log.d(TAG, "initialize NetGame");
        MyApplication app = (MyApplication) getApplication();
        mNetGame = app.getNetGame(
                getResources().getString(R.string.playerName),
                getRoomListener());

        if(mSelfHosting){
            mNetGame.startServer(this);
        }else{
            mNetGame.startClient(this);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        //TODO this is just for testing purposes. Imrpove
        mNsdHelper.tearDown();
        mNetConnection.tearDown();
    }

    private void setTvText(final TextView tv, final String str){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                tv.setText(str);
            }
        });
    }

    private void addPlayerWidget(final NetPlayer player){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                ViewGroup widget;
                if(player.getTeam() == 0) {
                    ViewGroup rootLayout = (ViewGroup) findViewById(R.id.playersLeft);
                    widget = (ViewGroup) getLayoutInflater().inflate(R.layout.player_widget_left, rootLayout);
                }else{
                    ViewGroup rootLayout = (ViewGroup) findViewById(R.id.playersRight);
                    widget = (ViewGroup) getLayoutInflater().inflate(R.layout.player_widget_right, rootLayout);
                }
                mWidgetsByPlayer.put(player, new WeakReference<>(widget));

                final ImageButton butSwitch = (ImageButton) widget.findViewWithTag("button");
                final TextView tvPlayer = (TextView) widget.findViewWithTag("name");
                final int bgColor;
                boolean buttonVisible = false;
                if(mSelfHosting){
                    if(player == mPlayer){
                        bgColor = ContextCompat.getColor(ActivityRoom.this, R.color.roomBgHostLocal);
                    }else{
                        bgColor = ContextCompat.getColor(ActivityRoom.this, R.color.roomBgRemote);
                    }
                    buttonVisible = true;
                }else{
                    if(player == mPlayer){
                        bgColor = ContextCompat.getColor(ActivityRoom.this, R.color.roomBgLocal);
                        buttonVisible = true;
                    }else if (player.isHost()){
                        bgColor = ContextCompat.getColor(ActivityRoom.this, R.color.roomBgHost);
                    }else{
                        bgColor = ContextCompat.getColor(ActivityRoom.this, R.color.roomBgRemote);
                    }
                }
                tvPlayer.setText(player.getName());
                tvPlayer.setBackgroundColor(bgColor);
                if (buttonVisible) {
                    butSwitch.setVisibility(View.VISIBLE);
                }
            }
        });
        /*
        ViewGroup widget;
        if(player.getTeam() == 0) {
            ViewGroup rootLayout = (ViewGroup) findViewById(R.id.playersLeft);
            widget = (ViewGroup) getLayoutInflater().inflate(R.layout.player_widget_left, rootLayout);
        }else{
            ViewGroup rootLayout = (ViewGroup) findViewById(R.id.playersRight);
            widget = (ViewGroup) getLayoutInflater().inflate(R.layout.player_widget_right, rootLayout);
        }
        mWidgetsByPlayer.put(player, new WeakReference<>(widget));

        final ImageButton butSwitch = (ImageButton) widget.findViewWithTag("button");
        final TextView tvPlayer = (TextView) widget.findViewWithTag("name");
        final int bgColor;
        boolean buttonVisible = false;
        if(mSelfHosting){
            if(player == mPlayer){
                bgColor = ContextCompat.getColor(this, R.color.roomBgHostLocal);
            }else{
                bgColor = ContextCompat.getColor(this, R.color.roomBgRemote);
            }
            buttonVisible = true;
        }else{
            if(player == mPlayer){
                bgColor = ContextCompat.getColor(this, R.color.roomBgLocal);
                buttonVisible = true;
            }else if (player.isHost()){
                bgColor = ContextCompat.getColor(this, R.color.roomBgHost);
            }else{
                bgColor = ContextCompat.getColor(this, R.color.roomBgRemote);
            }
        }

        if(Looper.getMainLooper().getThread() == Thread.currentThread()){
            tvPlayer.setText(player.getName());
            tvPlayer.setBackgroundColor(bgColor);
            if (buttonVisible) {
                butSwitch.setVisibility(View.VISIBLE);
            }
        }else {
            tvPlayer.post(new Runnable() {
                @Override
                public void run() {
                    tvPlayer.setText(player.getName());
                    tvPlayer.setBackgroundColor(bgColor);
                }
            });
            if (buttonVisible) {
                butSwitch.post(new Runnable() {
                    @Override
                    public void run() {
                        butSwitch.setVisibility(View.VISIBLE);
                    }
                });
            }
        }*/
    }

    private void removePlayerWidget(){

    }

    private void startSwitchBlink(NetPlayer player){
        View blinkButton = mWidgetsByPlayer.get(player).get();
        mBlinkAnimatorsByPlayer.put(player, startSwitchBlink(blinkButton));
    }

    private void stopSwitchBlink(NetPlayer player){
        mBlinkAnimatorsByPlayer.get(player).cancel();
    }

    private ValueAnimator startSwitchBlink(View button){
        Resources res = getResources();
        ValueAnimator animator = ValueAnimator.ofFloat(1.f, 0.f);
        animator.setDuration(res.getInteger(R.integer.switch_blink_duration));
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new AlphaSetter(button));
        animator.start();
        return animator;
    }
}
