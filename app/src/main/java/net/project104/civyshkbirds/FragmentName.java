package net.project104.civyshkbirds;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.project104.civyshkbirds.R;


public class FragmentName extends Fragment {
    ComboName combo;

    //This views are removed in onDestroyView(), so not a memleak
    RelativeLayout[] compositeButtons;
    Button[] butAnswers;
    ImageView[] iconAnswers;
    ImageView[] pictureAnswers;

    int lastLongPressedAnswer;

    View.OnClickListener listenerAnswer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            checkAnswer(ActivityMain.getCompositeIndex(view), false);
        }
    };

    View.OnTouchListener stateListener = new View.OnTouchListener(){
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

    private void checkAnswer(int idx, boolean repeated){
        disableAnswerButtons();
        combo.setSelectedAnswer(idx);
        showIcon(combo.correctAnswer, true);
        if(combo.correctAnswer != idx){
            showIcon(idx, false);
        }else if(!repeated){
            ((ActivityGame) getActivity()).score();
        }
        ((ActivityGame) getActivity()).showNextButton();
        if(!repeated) {
            ((ActivityGame) getActivity()).initCountdown();
        }
    }

    public FragmentName() {    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_name, container, false);
        compositeButtons = new RelativeLayout[]{
                (RelativeLayout) rootView.findViewById(R.id.composite0),
                (RelativeLayout) rootView.findViewById(R.id.composite1),
                (RelativeLayout) rootView.findViewById(R.id.composite2),
                (RelativeLayout) rootView.findViewById(R.id.composite3)};
        butAnswers = new Button[4];
        iconAnswers = new ImageView[4];
        pictureAnswers = new ImageView[4];

        for(int i=0; i<compositeButtons.length; i++){
            RelativeLayout compo = compositeButtons[i];
            butAnswers[i] = (Button) compo.findViewWithTag("button");
            iconAnswers[i] = (ImageView) compo.findViewWithTag("icon");
            pictureAnswers[i] = (ImageView) compo.findViewWithTag("picture");
        }
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        combo = ((ActivityGame) getActivity()).getNameCombo();

        ((TextView) getView().findViewById(R.id.question)).setText(combo.getQuestion(getResources()));
        //((ImageButton) getView().findViewById(R.id.question)).setOnClickListener();

        //I think imageLoader loads images when the view's size is known, not now
        ActivityMain.ImagesLoader imagesLoader = new ActivityMain.ImagesLoader(getResources());

        for(int i=0; i<butAnswers.length; i++){
            butAnswers[i].setOnClickListener(listenerAnswer);
            butAnswers[i].setOnTouchListener(stateListener);
            //butAnswers[i].setOnLongClickListener(longListener);
            registerForContextMenu(butAnswers[i]);
            imagesLoader.addImage(pictureAnswers[i], combo.getAnswer(i).getID(), false);
        }

        imagesLoader.imageViews.get(0).getViewTreeObserver().addOnPreDrawListener(imagesLoader);

        if(combo.selectedAnswer >= 0){
            checkAnswer(combo.selectedAnswer, true);
        }
    }

    @Override
    public void onDestroyView(){
        compositeButtons = null;
        butAnswers = null;
        iconAnswers = null;
        pictureAnswers = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_picture_to_bird, menu);
        lastLongPressedAnswer = ActivityMain.getCompositeIndex(v);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_show_bird:
                //ActivityMain.startPictureInfoActivity((ActivityAnimation) getActivity(), combo.getAnswer(lastLongPressedAnswer));
                ActivityMain.startCardActivity((ActivityAnimation) getActivity(), combo.getAnswer(lastLongPressedAnswer).latinName);
                return true;
            default:
                return super.onContextItemSelected(item);
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
