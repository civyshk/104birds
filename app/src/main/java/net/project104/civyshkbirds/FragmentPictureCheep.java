package net.project104.civyshkbirds;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentPictureCheep extends PlayerFragment {

    Bundle bundleFromActivity;
    ComboPicture comboPicture;
    ComboCheep comboCheep;
    String gameType;

    boolean hideMenuItemDifficult = false;

    //These are removed in onDestroyView so don't leak memory
    RelativeLayout[] compositeButtons;
    Button[] butAnswers;
    ImageView[] iconAnswers;


    //TODO Check if i remove listeners
    //@Overrides PlayerFragment.touchListenerPlayer in order to cancelCountdown()
    View.OnTouchListener touchListenerPlayer = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    ((ViewGroup) v.getParent()).findViewWithTag("picture").setAlpha(0.5f);
                    ((ActivityGame) getActivity()).cancelCountdown();
                    break;
                case MotionEvent.ACTION_UP:
                    ((ViewGroup) v.getParent()).findViewWithTag("picture").setAlpha(1.f);
                    break;
            }
            return false;
        }
    };


    View.OnClickListener listenerAnswer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            checkAnswer(ActivityMain.getCompositeIndex(view), false);
        }
    };

    OnTouchListener touchListenerAnswer = new OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getAction();
            if(action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.drawable.button_pressed_background);
                ((ActivityGame) getActivity()).cancelCountdown();
            } else if (action == MotionEvent.ACTION_UP) {
                v.setBackgroundResource(R.drawable.button_background);
            }else{
                return true;
            }
            return false;
        }
    };

    OnTouchListener touchImageListener = new OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getAction();
            if(action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.color.image_border_pressed);
                ((ActivityGame) getActivity()).cancelCountdown();
                return false;
            } else if (action == MotionEvent.ACTION_UP) {
                v.setBackgroundResource(R.color.image_border);
                return false;
            }
            return false;
        }
    };

    public FragmentPictureCheep() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bundleFromActivity = getArguments();
        gameType = bundleFromActivity.getString("PictureOrCheep");

        View rootView = null;
        if(gameType.equals("picture")){
            rootView = inflater.inflate(R.layout.fragment_picture, container, false);
            compositeButtons = new RelativeLayout[4];
            compositeButtons[0] = (RelativeLayout) rootView.findViewById(R.id.composite0);
            compositeButtons[1] = (RelativeLayout) rootView.findViewById(R.id.composite1);
            compositeButtons[2] = (RelativeLayout) rootView.findViewById(R.id.composite2);
            compositeButtons[3] = (RelativeLayout) rootView.findViewById(R.id.composite3);
            butAnswers = new Button[4];
            iconAnswers = new ImageView[4];
        }else if(gameType.equals("cheep")){
            hideMenuItemDifficult = true;
            if(savedInstanceState != null){
                //combine 2 bundles
                bundleFromActivity.putBoolean("MustStartPlayer", savedInstanceState.getBoolean("MustStartPlayer"));
                //next 2 items are not used, as they are retrieved from cheepCombo from activity
                bundleFromActivity.putInt("CheepIndex", savedInstanceState.getInt("CheepIndex"));
                bundleFromActivity.putSerializable("Cheeps", savedInstanceState.getSerializable("Cheeps"));
            }else {
                bundleFromActivity.putBoolean("MustStartPlayer", true);
            }
            rootView = inflater.inflate(R.layout.fragment_cheep, container, false);
            compositeButtons = new RelativeLayout[2];
            compositeButtons[0] = (RelativeLayout) rootView.findViewById(R.id.composite0);
            compositeButtons[1] = (RelativeLayout) rootView.findViewById(R.id.composite1);
            butAnswers = new Button[2];
            iconAnswers = new ImageView[2];

        }else{
            assert false;
        }

        for(int i=0; i<compositeButtons.length; i++){
            RelativeLayout compo = compositeButtons[i];
            butAnswers[i] = (Button) compo.findViewWithTag("button");
            iconAnswers[i] = (ImageView) compo.findViewWithTag("icon");
        }
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState){
        //savedInstanceState.putString("GameType", gameType);

        //this doesn't set up FragmentPlayer as savedInstanceState == null
        super.onActivityCreated(savedInstanceState);
        if(gameType.equals("picture")) {
            comboPicture = ((ActivityGame) getActivity()).getPictureCombo();
            comboCheep = null;
            hidePlayer();

            View rootView = getView();
            rootView.findViewById(R.id.imgQuestion).setOnTouchListener(touchImageListener);
            registerForContextMenu(rootView.findViewById(R.id.imgQuestion));
            ActivityMain.ImagesLoader imagesLoader = new ActivityMain.ImagesLoader(getResources());
            imagesLoader.addImage((ImageView) rootView.findViewById(R.id.imgQuestion), comboPicture.getPicture().getID(), false);
            imagesLoader.imageViews.get(0).getViewTreeObserver().addOnPreDrawListener(imagesLoader);

            for(int i=0; i<butAnswers.length; i++){
                butAnswers[i].setText(comboPicture.getAnswer(i));
                butAnswers[i].setOnClickListener(listenerAnswer);
                butAnswers[i].setOnTouchListener(touchListenerAnswer);
            }
            if(comboPicture.selectedAnswer >= 0){
                checkAnswer(comboPicture.selectedAnswer, true);
            }
        }else{//cheep
            comboCheep = ((ActivityGame) getActivity()).getCheepCombo();
            comboPicture = null;

            List<Cheep> questionCheep = new ArrayList<>();
            questionCheep.add(comboCheep.cheep);
            setUpPlayer(questionCheep, 0, bundleFromActivity.getInt("UserVolume"), bundleFromActivity.getBoolean("MustStartPlayer"));

            for(int i=0; i<butAnswers.length; i++){
                butAnswers[i].setText(comboCheep.getAnswer(i));
                butAnswers[i].setOnClickListener(listenerAnswer);
                butAnswers[i].setOnTouchListener(touchListenerAnswer);
            }
            if(comboCheep.selectedAnswer >= 0){
                checkAnswer(comboCheep.selectedAnswer, true);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        compositeButtons = null;
        butAnswers = null;
        iconAnswers = null;

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_picture_to_bird, menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.menu_item_difficult).setVisible(!hideMenuItemDifficult);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_show_bird:
			    //ActivityMain.startPictureInfoActivity((ActivityAnimation) getActivity(), comboPicture.getPicture());
                ActivityMain.startCardActivity((ActivityAnimation) getActivity(), comboPicture.getPicture().latinName);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void checkAnswer(int idx, boolean repeated){
        disableAnswerButtons();
        int correctAnswer;
        if(gameType.equals(("picture"))){
            correctAnswer = comboPicture.correctAnswer;
            comboPicture.setSelectedAnswer(idx);
        }else{
            correctAnswer = comboCheep.correctAnswer;
            comboCheep.setSelectedAnswer(idx);
        }
        showIcon(correctAnswer, true);
        if(correctAnswer != idx){
            showIcon(idx, false);
        }else if(!repeated){
            ((ActivityGame) getActivity()).score();
        }
        ((ActivityGame) getActivity()).showNextButton();
        if(!repeated) {
            ((ActivityGame) getActivity()).initCountdown();
        }
    }

    public void showIcon(int idx, boolean correct){
        //Don't replace this setScaledImageResource with ActivityMain.ImagesLoader. View size is already available
        ActivityMain.setScaledImageResource(getResources(), iconAnswers[idx], correct ? R.drawable.icon_true : R.drawable.icon_false, false);
    }

    private void disableAnswerButtons(){
        for(Button button : butAnswers){
            button.setOnClickListener(null);
        }
    }

}
