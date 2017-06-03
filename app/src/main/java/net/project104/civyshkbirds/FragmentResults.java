package net.project104.civyshkbirds;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentResults extends Fragment {

    Runnable mCountdown;
    Handler mHandler;
    boolean mMustShowNextButton;
    int mCorrectAnswers, mNumQuestions;

    public FragmentResults(){
        Log.d("104", this.toString() + " constructed");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mHandler = new Handler();

        //canInitCountdown = true;

        Bundle bundle;
        if(savedInstanceState == null){
            bundle = getArguments();
            mMustShowNextButton = false;
        }else{
            bundle = savedInstanceState;
            mMustShowNextButton = true;
        }

        mCorrectAnswers = bundle.getInt("CorrectAnswers");
        mNumQuestions = bundle.getInt("NumQuestions");
        String str = getResources().getString(R.string.youve_got_x_correct_answers);
        str = String.format(str, mCorrectAnswers, mNumQuestions);

        View rootView = inflater.inflate(R.layout.fragment_results, container, false);
        ((TextView) rootView.findViewById(R.id.tvResult)).setText(str);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(mMustShowNextButton) {
            ((ActivityGame) getActivity()).showNextButton();
        }else{
            initCountdown();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt("NumQuestions", mNumQuestions);
        savedInstanceState.putInt("CorrectAnswers", mCorrectAnswers);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        cancelCountdown();
        super.onPause();
    }

    public void initCountdown(){
        mCountdown = new Runnable() {
            @Override
            public void run() {
                ((ActivityGame) getActivity()).showNextButton();
            }
        };
        mHandler.postDelayed(mCountdown, getResources().getInteger(R.integer.next_countdown));
    }

    public void cancelCountdown(){
        if (mHandler != null) {
            mHandler.removeCallbacks(mCountdown);
        }
        mCountdown = null;
    }

    /*public void preventCountdown(){
        canInitCountdown = false;
    }*/

    /*public void enableNextButton(){
        //called from ActivityGame, indicates this fragment to show nextButton at onActivityCreated()
        mMustShowNextButton = true;
    }*/
}
