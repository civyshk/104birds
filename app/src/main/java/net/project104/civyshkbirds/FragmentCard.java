package net.project104.civyshkbirds;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import net.project104.civyshkbirds.R;

import java.util.HashMap;
import java.util.Map;

public class FragmentCard extends PlayerFragment {

    AdapterPicture mAdapter;

    Bird bird;
    Bundle receivedBundle;
    Map<View, Picture> picturesByView; //I don't think these views leak memory
        //In case they're saved by view and not by hash, when a picture is destroyed (in adapterPicture)
        //the map association view-pic is removed
    Picture lastLongPressedPicture;

    View.OnClickListener listenerPicture = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ActivityMain.startPictureInfoActivity((ActivityAnimation) getActivity(), picturesByView.get(view));
        }
    };

    static View.OnTouchListener touchListenerPicture = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getAction();
            if(action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.color.image_border_pressed);
            } else if (action == MotionEvent.ACTION_UP) {
                v.setBackgroundResource(R.color.image_border);
            }else if(action == MotionEvent.ACTION_CANCEL) {
                //TOFIX event gets cancelled if I move finger
                v.setBackgroundResource(R.color.image_border);
            }else{
                //I can't find a difference when returning true or false... Both work, I think
            }
            return false;
        }
    };

    public FragmentCard() {
        // Required empty public constructor
        super();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(savedInstanceState == null) {
            receivedBundle = getArguments();
        }else{
            receivedBundle = savedInstanceState;
        }
        bird = (Bird) receivedBundle.getSerializable("Bird");

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_card, container, false);

        ((TextView) rootView.findViewById(R.id.tvLatinName)).setText(bird.latinName);
        picturesByView = new HashMap<>();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        String commonName = ActivityMain.getCommonName(activity, bird.latinName);
        if(!commonName.equals("")){
            ((TextView) getView().findViewById(R.id.tvCommonName)).setText(commonName);
        }else{
            getView().findViewById(R.id.tvCommonName).setVisibility(View.GONE);
        }

        ViewPagerCustom viewPager = (ViewPagerCustom) getView().findViewById(R.id.viewPager);
        //from http://blog.neteril.org/blog/2013/10/14/android-tip-viewpager-with-protruding-children/
        viewPager.setClipToPadding(false);
        viewPager.setPageMargin((int) activity.getResources().getDimension(R.dimen.view_pager_page_margin));
        mAdapter = new AdapterPicture(this);
        mAdapter.loadPictures(bird);
        viewPager.setAdapter(mAdapter);
        //if(bird.pictures.size() < 2){
            //viewPager.setPagingEnabled(false);
            //I never need to disable it.
        //}

        if(!bird.cheeps.isEmpty()){
            int defaultUserVol = activity.getResources().getInteger(R.integer.default_user_vol);
            int userVol = activity.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).getInt(getString(R.string.preferences_user_vol), defaultUserVol);
            setUpPlayer(bird.cheeps, 0, userVol, false);
        }else{
            hidePlayer();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("Bird", bird);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){

        super.onResume();
    }

    @Override
    public void onPause(){
        //viewpager holds adapter, which holds context. Is viewpager removed? I don't know, so I delete context
        //if i remove, i should set it back in onresume
        //mAdapter.setContext(null);
        //mAdapter = null;
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_picture_to_picture, menu);
        lastLongPressedPicture = picturesByView.get(v);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_show_picture:
                ActivityMain.startPictureInfoActivity((ActivityAnimation) getActivity(), lastLongPressedPicture);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void addListeners(View view, Picture pic){
        picturesByView.put(view, pic);
        view.setOnClickListener(listenerPicture);
        view.setOnTouchListener(touchListenerPicture);
    }

    public void removeListeners(View view){
        //remove all from addListeners
        picturesByView.remove(view);
        view.setOnClickListener(null);
        view.setOnTouchListener(null);
    }

}
