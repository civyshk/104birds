package net.project104.civyshkbirds;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ActivityNetRoom extends AppCompatActivity {
    public final String TAG = getClass().getSimpleName();

    private NetConnection mNetConnection;
    private JmdHelper mNsdHelper;

    Drawable mDrawableSwitchLeft, mDrawableSwitchRight;
    boolean mSelfHosting;
    NetPlayer mPlayer;
    Map<NetPlayer, ValueAnimator> mBlinkAnimatorsByPlayer;
    Map<NetPlayer, WeakReference<ViewGroup>> mWidgetsByPlayer;

    private TextEllipsis mTextEllipsis;

    static class TextEllipsis implements Runnable {
        int i=-1;
        WeakReference<TextView> mTextView;
        String mText;

        public TextEllipsis(TextView textView, String text){
            mTextView = new WeakReference<>(textView);
            mText = text;
        }

        @Override
        public void run(){
            i += 1;
            if(i>=4){
                i=0;
            }
            TextView tv = mTextView.get();
            if(tv != null){
                String points = (i==0?"":new String(new char[i]).replace("\0", "."));//http://stackoverflow.com/a/16812721
                String text = mText + points;
                tv.setText(text);
                tv.postDelayed(this, 250);
            }
            //if tv=null? callback is lost. problem?
        }
    }

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

    static class RoomNsdRegistrationListener implements NsdHelper.RegistrationListener {
        private WeakReference<ActivityRoom> mActivity;

        public RoomNsdRegistrationListener(ActivityRoom activity) {
            mActivity = new WeakReference<ActivityRoom>(activity);
        }

        @Override
        public void onServiceRegistered(String name) {
            ActivityRoom activity = mActivity.get();
            if (activity != null) {
                activity.stopTextEllipsis();
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
            ActivityRoom activity = mActivity.get();
            if (activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_hosting_failed));
                activity.setTvText(tvName, "");
            }
        }
    }

    static class RoomNsdDiscoveryListener implements NsdHelper.DiscoveryListener {
        private WeakReference<ActivityRoom> mActivity;
        public RoomNsdDiscoveryListener(ActivityRoom activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onDiscoveryStarted() {
            ActivityRoom activity = mActivity.get();
            if(activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_discovery_started));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onServiceFound() {
            ActivityRoom activity = mActivity.get();
            if(activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_discovery_succeeded));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onServiceLost() {
            ActivityRoom activity = mActivity.get();
            if(activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_host_stopped_announcing));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onDiscoveryStopped() {
            ActivityRoom activity = mActivity.get();
            if(activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_discovery_stopped));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onStartDiscoveryFailed() {
            ActivityRoom activity = mActivity.get();
            if(activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.game_discovery_failed));
                activity.setTvText(tvName, "");
            }
        }

        @Override
        public void onStopDiscoveryFailed() {
            ActivityRoom activity = mActivity.get();
            if(activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.cant_stop_discovery));
                activity.setTvText(tvName, "");
            }
        }
    }

    static class RoomNsdResolveListener implements NsdHelper.ResolveListener{
        private WeakReference<ActivityRoom> mActivity;
        public RoomNsdResolveListener(ActivityRoom activity){
            mActivity = new WeakReference<ActivityRoom>(activity);
        }

        @Override
        public void onServiceResolved(String name, InetAddress address, int port) {
            ActivityRoom activity = mActivity.get();
            if(activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.server_found));
                activity.setTvText(tvName, name);
                //TODO client should connect to that resolved service //TODO
                //TODO
                //TODO
                activity.mNetConnection.startClient(address, port, activity.getResources().getString(R.string.player_name));
            }else{
                //TODO there's a server ready but activity is off. Then? Start connection too?
                //Leave a message in a queue, and activityRoom should connect when it's up again?
                //How?
            }
        }

        @Override
        public void onResolveFailed() {
            ActivityRoom activity = mActivity.get();
            if(activity != null) {
                activity.stopTextEllipsis();
                TextView tvInfo = ((TextView) activity.findViewById(R.id.tvRoomInfo));
                TextView tvName = ((TextView) activity.findViewById(R.id.tvRoomName));
                activity.setTvText(tvInfo, activity.getResources().getString(R.string.cant_get_server_info));
                activity.setTvText(tvName, "");
            }
        }
    }

    static class MyRoomListener implements NetConnection.RoomListener {
        private WeakReference<ActivityRoom> mActivity;
        public MyRoomListener(ActivityRoom activity){
            mActivity = new WeakReference<ActivityRoom>(activity);
        }

        @Override
        public void onPlayerAdd(NetPlayer player){
            ActivityRoom activity = mActivity.get();
            if(activity != null){
                activity.addPlayerWidget(player);
            }
        }

        @Override
        public void onPlayerRemove(NetPlayer player) {
            ActivityRoom activity = mActivity.get();
            if(activity != null){

            }
        }

        @Override
        public void onPlayerReqSwitch(NetPlayer player) {
            ActivityRoom activity = mActivity.get();
            if(activity != null){

            }
        }

        @Override
        public void onPlayerSwitchTeam(NetPlayer player, int team) {
            ActivityRoom activity = mActivity.get();
            if(activity != null){

            }
        }

        @Override
        public void onPlayerSwitchRejected(NetPlayer player) {
            ActivityRoom activity = mActivity.get();
            if(activity != null){

            }
        }

        @Override
        public void onStartGame(int seconds){
            ActivityRoom activity = mActivity.get();
            if (activity != null) {
                Toast.makeText(activity, "Start in " + String.valueOf(seconds), Toast.LENGTH_LONG).show();
            }
        }
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
        mDrawableSwitchLeft = ContextCompat.getDrawable(this, R.drawable.switch_team_left);
        mDrawableSwitchRight = ContextCompat.getDrawable(this, R.drawable.switch_team_right);

        //start NetConnection & NSD

        Log.d(TAG, "start NSD");
        MyApplication app = (MyApplication) getApplication();
        mNsdHelper = app.getNsdHelper();
        Log.d(TAG, "start connection");
        mNetConnection = app.getNetConnection();
        mNetConnection.setPlayersListener(new MyRoomListener(this));
        if (mSelfHosting) {
            Log.d(TAG, "self host");
            startTextEllipsis(getResources().getString(R.string.initializing_server));
            mNsdHelper.initializeRegistrationListeners(new RoomNsdRegistrationListener(this));
            mNetConnection.startServer(mNsdHelper, getResources().getString(R.string.player_name));
            mPlayer = mNetConnection.getPlayer();
        }else{
            Log.d(TAG, "client");
            startTextEllipsis(getResources().getString(R.string.searching_server));
            /**
            * TODO discover services
            * resolve them, filter them
            * start client
            */
        //TODO Something goes very slow here
            mNsdHelper.initializeDiscoveryListeners(new RoomNsdDiscoveryListener(this), new RoomNsdResolveListener(this));
            mNsdHelper.discoverServices();
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        //TODO this is just for testing purposes. Imrpove
        mNsdHelper.tearDown();
        mNetConnection.tearDown();
    }

    private void startTextEllipsis(String text){
        TextView tvInfo = ((TextView) findViewById(R.id.tvRoomInfo));
        tvInfo.removeCallbacks(mTextEllipsis);
        mTextEllipsis = new TextEllipsis(tvInfo, text);
        tvInfo.post(mTextEllipsis);
    }

    private void stopTextEllipsis(){
        TextView tvInfo = ((TextView) findViewById(R.id.tvRoomInfo));
        tvInfo.removeCallbacks(mTextEllipsis);
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

    public void onRequestSwitchTeam(){

    }

    //A estos métodos los llama el NetConnection. ¿Meterlos en un listener?
    public void onSwitchTeamConfirmed(NetPlayer player){

    }

    public void onSwitchTeamRejected(NetPlayer player){

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
