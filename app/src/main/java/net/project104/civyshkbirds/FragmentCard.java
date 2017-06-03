package net.project104.civyshkbirds;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class FragmentCard extends PlayerFragment {
    public static final String TAG = FragmentCard.class.getSimpleName();
    static final String BIRD_PARAM_NAME = "Bird";
    static final String HAS_PREVIOUS_PARAM_NAME = "HasPrevious";
    static final String HAS_NEXT_PARAM_NAME = "HasNext";

    AdapterPicture mAdapter;

    Bird bird;
    boolean hasPrevious, hasNext;
    Bundle receivedBundle;
    Map<View, Picture> picturesByView = new HashMap<>(); //I don't think these views leak memory
    //In case they're saved by view and not by hash, when a picture is destroyed (in adapterPicture)
    //the map association view-pic is removed
    Picture lastLongPressedPicture;

    View.OnClickListener clickListenerPicture = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ActivityMain.startPictureInfoActivity((ActivityAnimation) getActivity(), picturesByView.get(view));
        }
    };

    static View.OnTouchListener touchListenerPicture = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.color.image_border_pressed);
            } else if (action == MotionEvent.ACTION_UP) {
                v.setBackgroundResource(R.color.image_border);
            } else if (action == MotionEvent.ACTION_CANCEL) {
                //TOFIX event gets cancelled if I move finger
                v.setBackgroundResource(R.color.image_border);
            } else {
                //I can't find a difference when returning true or false... Both work, I think
            }
            return false;
        }
    };

    View.OnClickListener clickListenerNavigation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.butCardPrev:
                    ((ActivityCards) getActivity()).showAdjacentFragment(bird, -1);
                    break;
                case R.id.butCardNext:
                    ((ActivityCards) getActivity()).showAdjacentFragment(bird, 1);
                    break;
            }
        }
    };

    ImageView ivCardPrev, ivCardNext;
    View.OnTouchListener touchListenerNavigation = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                switch (v.getId()) {
                    case R.id.butCardPrev:
                        ivCardPrev.setAlpha(0.5f);
                        break;
                    case R.id.butCardNext:
                        ivCardNext.setAlpha(0.5f);
                        break;
                }
                return false;
            } else if (action == MotionEvent.ACTION_UP) {
                switch (v.getId()) {
                    case R.id.butCardPrev:
                        ivCardPrev.setAlpha(1f);
                        break;
                    case R.id.butCardNext:
                        ivCardNext.setAlpha(1f);
                        break;
                }
                return false;
            }
            return false;
        }
    };

    public FragmentCard() {
        // Required empty public constructor
        super();
    }

    public static FragmentCard newInstance(Bird bird, boolean hasPrevious, boolean hasNext) {
        Bundle args = new Bundle();
        args.putSerializable(BIRD_PARAM_NAME, bird);
        args.putSerializable(HAS_PREVIOUS_PARAM_NAME, hasPrevious);
        args.putSerializable(HAS_NEXT_PARAM_NAME, hasNext);
        FragmentCard fragment = new FragmentCard();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            receivedBundle = getArguments();
        } else {
            receivedBundle = savedInstanceState;
        }
        bird = (Bird) receivedBundle.getSerializable(BIRD_PARAM_NAME);
        hasPrevious = receivedBundle.getBoolean(HAS_PREVIOUS_PARAM_NAME);
        hasNext = receivedBundle.getBoolean(HAS_NEXT_PARAM_NAME);
        picturesByView.clear();//not sure if needed

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.tvLatinName)).setText(bird.latinName);
        ivCardPrev = (ImageView) view.findViewById(R.id.imgCardPrev);
        ivCardNext = (ImageView) view.findViewById(R.id.imgCardNext);

        if(hasPrevious) {
            ((Button) view.findViewById(R.id.butCardPrev)).setOnClickListener(clickListenerNavigation);
            ((Button) view.findViewById(R.id.butCardPrev)).setOnTouchListener(touchListenerNavigation);
        }else{
            view.findViewById(R.id.butFramePrev).setVisibility(View.INVISIBLE);
        }

        if(hasNext) {
            ((Button) view.findViewById(R.id.butCardNext)).setOnClickListener(clickListenerNavigation);
            ((Button) view.findViewById(R.id.butCardNext)).setOnTouchListener(touchListenerNavigation);
        }else{
            view.findViewById(R.id.butFrameNext).setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        String commonName = ActivityMain.getCommonName(activity, bird.latinName);
        if (!commonName.equals("")) {
            ((TextView) getView().findViewById(R.id.tvCommonName)).setText(commonName);
        } else {
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

        if (!bird.cheeps.isEmpty()) {
            int defaultUserVol = activity.getResources().getInteger(R.integer.default_user_vol);
            int userVol = activity.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).getInt(getString(R.string.preferences_user_vol), defaultUserVol);
            setUpPlayer(bird.cheeps, 0, userVol, false);
        } else {
            hidePlayer();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(BIRD_PARAM_NAME, bird);
        savedInstanceState.putSerializable(HAS_PREVIOUS_PARAM_NAME, hasPrevious);
        savedInstanceState.putSerializable(HAS_NEXT_PARAM_NAME, hasNext);
        super.onSaveInstanceState(savedInstanceState);
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

    public void addListeners(View view, Picture pic) {
        picturesByView.put(view, pic);
        view.setOnClickListener(clickListenerPicture);
        view.setOnTouchListener(touchListenerPicture);
    }

    public void removeListeners(View view) {
        //remove all from addListeners
        picturesByView.remove(view);
        view.setOnClickListener(null);
        view.setOnTouchListener(null);
    }

    public void onNavigationCard(View v) {

    }

}
