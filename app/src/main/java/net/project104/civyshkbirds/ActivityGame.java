package net.project104.civyshkbirds;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.project104.civyshkbirds.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ActivityGame extends ActivityAnimation {

    Map<String, Bird> birdsByName;
    Map<String, List<Bird>> birdsByFamily;

    static final Random rand;
    static {
        rand = new Random();
    }

    private enum SelectionType {
        PICTURES_SAME_FAMILY, PICTURES_ANY_FAMILY, CHEEPS
    }

    Handler handler;
    Runnable countdown;
    boolean canAdvanceToNextFragment;

    private class GameInfo {
        int numQuestions, questionIndex, correctAnswers;
        String gameType;
        boolean onlyFamily;
        ComboPicture comboPicture;
        ComboCheep comboCheep;
        ComboName comboName;

        GameInfo() {
            numQuestions = 10;
            questionIndex = 0;
            correctAnswers = 0;
            gameType = "";
            onlyFamily = true;
            resetCombos();
        }

        public void score() {            correctAnswers++;        }
        public void advanceIndex() {            questionIndex++;        }
        public void resetIndex() {            questionIndex = 0;        }

        public void setComboPicture(ComboPicture combo) {
            comboPicture = combo;
            comboName = null;
            comboCheep = null;
        }

        public void setComboName(ComboName combo) {
            comboPicture = null;
            comboName = combo;
            comboCheep = null;
        }

        public void setComboCheep(ComboCheep combo) {
            comboPicture = null;
            comboName = null;
            comboCheep = combo;
        }

        public void resetCombos() {
            comboPicture = null;
            comboCheep = null;
            comboName = null;
        }
    }

    GameInfo currentGameInfo;
    Fragment currentFragment;

    Button butTitle;
    ImageView ivNext;

    View.OnTouchListener buttonStateListener = new View.OnTouchListener() {
        //TODO check if I remove the listener
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                //((ImageView) v).setImageResource(R.drawable.icon_next_pressed);
                cancelCountdown();
                ivNext.setAlpha(0.5f);
                return false;
            } else if (action == MotionEvent.ACTION_UP) {
                //((ImageView) v).setImageResource(R.drawable.icon_next);
                ivNext.setAlpha(1.f);
                return false;
            }
            return false;
        }
    };

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle;
        FragmentTransaction fragmentTransaction = null;
        currentGameInfo = new GameInfo();
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
            canAdvanceToNextFragment = false;
            fragmentTransaction = getFragmentManager().beginTransaction();
        } else {
            bundle = savedInstanceState;
            canAdvanceToNextFragment = bundle.getBoolean("CanAdvance");
            currentGameInfo.questionIndex = bundle.getInt("QuestionIndex");
            currentGameInfo.correctAnswers = bundle.getInt("CorrectAnswers");
        }
        super.onCreate(savedInstanceState, bundle, R.layout.activity_game);

        birdsByName = new HashMap<String, Bird>();
        birdsByFamily = new HashMap<String, List<Bird>>();
        ActivityMain.inflateBirds(this, birdsByName, birdsByFamily);

        currentGameInfo.numQuestions = bundle.getInt("NumQuestions");
        currentGameInfo.gameType = bundle.getString("GameType");

        if (savedInstanceState != null) {
            if (currentGameInfo.gameType.equals("picture")) {
                currentGameInfo.setComboPicture((ComboPicture) savedInstanceState.getSerializable("ComboPicture"));
            } else if (currentGameInfo.gameType.equals("name")) {
                currentGameInfo.setComboName((ComboName) savedInstanceState.getSerializable("ComboName"));
            } else if (currentGameInfo.gameType.equals("cheep")) {
                currentGameInfo.setComboCheep((ComboCheep) savedInstanceState.getSerializable("ComboCheep"));
            }
        }

        if (currentGameInfo.gameType.equals("picture") || currentGameInfo.gameType.equals("name")) {
            currentGameInfo.onlyFamily = bundle.getBoolean("OnlyFamily");
        }

        butTitle = (Button) findViewById(R.id.compositeTitle).findViewWithTag("button");
        ivNext = (ImageView) findViewById(R.id.imgNext);
        findViewById(R.id.butNext).setOnTouchListener(buttonStateListener);

        if(!ActivityMain.areTranslationsAvailable(getResources())) {
            SharedPreferences.Editor preferences =
                    getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).edit();
            preferences.putBoolean(getString(R.string.preferences_latin_names), true);
            preferences.commit();
        }

        if(savedInstanceState == null) {
            Bundle bundleToFragment = new Bundle();
            Fragment gameFragment = null;
            if (currentGameInfo.questionIndex == currentGameInfo.numQuestions) {
                gameFragment = new FragmentResults();
                //((FragmentResults) gameFragment).preventCountdown();
                //((FragmentResults) gameFragment).enableNextButton();
                bundleToFragment.putInt("CorrectAnswers", currentGameInfo.correctAnswers);
                bundleToFragment.putInt("NumQuestions", currentGameInfo.numQuestions);
                gameFragment.setArguments(bundleToFragment);
                fragmentTransaction.add(R.id.gameFrame, gameFragment, "gameFragment");
            } else if (currentGameInfo.gameType.equals("picture")) {
                gameFragment = new FragmentPictureCheep();
                bundleToFragment.putString("PictureOrCheep", "picture");
                gameFragment.setArguments(bundleToFragment);
                fragmentTransaction.add(R.id.gameFrame, gameFragment, "gameFragment");
            } else if (currentGameInfo.gameType.equals("name")) {
                gameFragment = new FragmentName();
                gameFragment.setArguments(bundleToFragment);
                fragmentTransaction.add(R.id.gameFrame, gameFragment, "gameFragment");
            } else if (currentGameInfo.gameType.equals("cheep")) {
                gameFragment = new FragmentPictureCheep();
                bundleToFragment.putString("PictureOrCheep", "cheep");
                int defaultUserVol = getResources().getInteger(R.integer.default_user_vol);
                int userVol = getSharedPreferences(getString(R.string.preferences_file_name), MODE_PRIVATE).getInt(getString(R.string.preferences_user_vol), defaultUserVol);
                bundleToFragment.putInt("UserVolume", userVol);
                gameFragment.setArguments(bundleToFragment);
                fragmentTransaction.add(R.id.gameFrame, gameFragment, "gameFragment");
            } else {
                assert false : "Unknown game type";
            }
            currentFragment = gameFragment;
            fragmentTransaction.commit();
        }
        writeInfo();
        handler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        //cancelPlayingCheep();
        cancelCountdown();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putBoolean("CanAdvance", canAdvanceToNextFragment);

        savedInstanceState.putString("GameType", currentGameInfo.gameType);
        savedInstanceState.putInt("QuestionIndex", currentGameInfo.questionIndex);
        savedInstanceState.putInt("CorrectAnswers", currentGameInfo.correctAnswers);
        savedInstanceState.putInt("NumQuestions", currentGameInfo.numQuestions);

        if (currentGameInfo.gameType.equals("picture") || currentGameInfo.gameType.equals("name")) {
            savedInstanceState.putBoolean("OnlyFamily", currentGameInfo.onlyFamily);
        }

        if (currentGameInfo.gameType.equals("picture")) {
            savedInstanceState.putSerializable("ComboPicture", currentGameInfo.comboPicture);
        } else if (currentGameInfo.gameType.equals("name")) {
            savedInstanceState.putSerializable("ComboName", currentGameInfo.comboName);
        } else if (currentGameInfo.gameType.equals("cheep")) {
            savedInstanceState.putSerializable("ComboCheep", currentGameInfo.comboCheep);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //savedInstanceState is fully used in onCreate()
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        menu.findItem(R.id.menu_item_latin_names).setVisible(ActivityMain.areTranslationsAvailable(getResources()));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        ActivityMain.checkMenuItems(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.menu_item_difficult) {
            boolean isHard = !item.isChecked();
            currentGameInfo.onlyFamily = isHard;
            item.setChecked(isHard);
            SharedPreferences.Editor preferences =
                    getSharedPreferences(getString(R.string.preferences_file_name),
                            Context.MODE_PRIVATE).edit();
            preferences.putBoolean(getString(R.string.preferences_only_family), isHard);
            preferences.commit();
            return true;
        }else if(id == R.id.menu_item_latin_names){
            boolean isLatin = !item.isChecked();
            item.setChecked(isLatin);
            SharedPreferences.Editor preferences =
                    getSharedPreferences(getString(R.string.preferences_file_name),
                            Context.MODE_PRIVATE).edit();
            preferences.putBoolean(getString(R.string.preferences_latin_names), isLatin);
            preferences.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<Bird> getRandomBirds(int n, SelectionType selectionType) {
        List<Bird> selectedBirds = new ArrayList<Bird>();
        switch (selectionType) {

            //get birds with pictures, in the same family
            case PICTURES_SAME_FAMILY:
                int numFamilies = birdsByFamily.size();
                String family = "";
                boolean enoughBirdsInFamily = false;
                //get random family
                //this while assumes that at least one family has size >= n
                while (!enoughBirdsInFamily) {
                    family = (String) birdsByFamily.keySet().toArray()[rand.nextInt(numFamilies)];
                    enoughBirdsInFamily = areEnoughBirds(family, n);
                }
                //get 'n' random birds in that family
                while (selectedBirds.size() < n) {
                    int birdIndex = rand.nextInt(birdsByFamily.get(family).size());
                    Bird selectedBird = birdsByFamily.get(family).get(birdIndex);
                    if (!selectedBirds.contains(selectedBird) && isNamedBird(selectedBird)) {
                        selectedBirds.add(selectedBird);
                    }
                }
                break;

            case PICTURES_ANY_FAMILY:
                //get birds with pictures, in any family
                //this while assumes that there are enough birds with picture.
                while (selectedBirds.size() < n) {
                    int birdIndex = rand.nextInt(birdsByName.size());
                    Bird selectedBird = (Bird) birdsByName.values().toArray()[birdIndex];
                    if (!selectedBirds.contains(selectedBird)
                            && !selectedBird.pictures.isEmpty()
                            && isNamedBird(selectedBird)) {
                        selectedBirds.add(selectedBird);
                    }
                }
                break;

            case CHEEPS:
                //get birds with cheeps
                //this while assumes that there are enough birds with cheep.
                while (selectedBirds.size() < n) {
                    int birdIndex = rand.nextInt(birdsByName.size());
                    Bird selectedBird = (Bird) birdsByName.values().toArray()[birdIndex];
                    if (!selectedBirds.contains(selectedBird)
                            && !selectedBird.cheeps.isEmpty()
                            && isNamedBird(selectedBird)) {
                        selectedBirds.add(selectedBird);
                    }
                }
                break;
        }
        return selectedBirds;
    }

    private boolean areEnoughBirds(String family, int n){
        boolean isLatin = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).
                getBoolean(getString(R.string.preferences_latin_names), false);
        if(isLatin) {
            return birdsByFamily.get(family).size() >= n;
        }
        int numberNamedBirds = 0;
        int i=0;
        boolean areEnough = false;
        List<Bird> birds = birdsByFamily.get(family);
        while(!areEnough && i < birds.size()){
            if(isNamedBird(birds.get(i))){
                numberNamedBirds++;
                if(numberNamedBirds >= n){
                    areEnough = true;
                }
            }
            i++;
        }
        return areEnough;
    }

    private boolean isNamedBird(Bird bird){
        Resources res = getResources();
        boolean isLatin = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).
                getBoolean(getString(R.string.preferences_latin_names), false);
        if(isLatin) {
            return true;
        }else{
            boolean found;
            try{
                res.getString(res.getIdentifier(ActivityMain.androidize(bird.latinName), "string", getPackageName()));
                found = true;
            }catch(Resources.NotFoundException e){
                found = false;
            }
            return found;
        }
    }

    public ComboPicture getPictureCombo() {
        if (currentGameInfo.comboPicture != null) {
            return currentGameInfo.comboPicture;
        } else {
            List<Bird> selectedBirds = getRandomBirds(4, currentGameInfo.onlyFamily ? SelectionType.PICTURES_SAME_FAMILY : SelectionType.PICTURES_ANY_FAMILY);

            ComboPicture combo = new ComboPicture();
            for (Bird bird : selectedBirds) {
                combo.addAnswer(getDisplayName(bird.latinName));
            }
            int correctAnswer = rand.nextInt(selectedBirds.size());
            combo.setCorrectAnswer(correctAnswer);
            combo.setPicture(selectedBirds.get(correctAnswer).getRandomPicture());
            currentGameInfo.setComboPicture(combo);
            return combo;
        }
    }

    public ComboName getNameCombo() {
        if (currentGameInfo.comboName != null) {
            return currentGameInfo.comboName;
        } else {
            List<Bird> selectedBirds = getRandomBirds(4, currentGameInfo.onlyFamily ? SelectionType.PICTURES_SAME_FAMILY : SelectionType.PICTURES_ANY_FAMILY);

            ComboName combo = new ComboName();
            for (Bird bird : selectedBirds) {
                combo.addPicture(bird.getRandomPicture());
            }
            int correctAnswer = rand.nextInt(selectedBirds.size());
            combo.setCorrectAnswer(correctAnswer);
            combo.setQuestion(getDisplayName(selectedBirds.get(correctAnswer).latinName));
            currentGameInfo.setComboName(combo);
            return combo;
        }
    }

    public ComboCheep getCheepCombo() {
        if (currentGameInfo.comboCheep != null) {
            return currentGameInfo.comboCheep;
        } else {
            List<Bird> selectedBirds = getRandomBirds(2, SelectionType.CHEEPS);

            ComboCheep combo = new ComboCheep();
            for (Bird bird : selectedBirds) {
                combo.addAnswer(getDisplayName(bird.latinName));
            }
            int correctAnswer = rand.nextInt(selectedBirds.size());
            combo.setCorrectAnswer(correctAnswer);
            combo.setCheep(selectedBirds.get(correctAnswer).getRandomCheep());
            currentGameInfo.setComboCheep(combo);
            return combo;
        }
    }

    private String getDisplayName(String latinName) {
        boolean isLatin = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE).
                getBoolean(getString(R.string.preferences_latin_names), false);
        if(isLatin){
            return latinName;
        }
        Resources res = getResources();
        try {
            return res.getString(res.getIdentifier(ActivityMain.androidize(latinName), "string", getPackageName()));
        } catch (Resources.NotFoundException e) {
            return latinName;
        }
    }

    public void score() {
        currentGameInfo.score();
    }

    public void finnishFragment(View v) {
        if (!canAdvanceToNextFragment) {
            return;
        } else {
            canAdvanceToNextFragment = false;
        }
        if (v != null) {
            cancelCountdown();
        }
        cancelPlayingCheep();
        currentGameInfo.resetCombos();//so that new gameFragment takes a new combo
        currentGameInfo.advanceIndex();
        if (currentGameInfo.questionIndex < currentGameInfo.numQuestions) {
            Fragment nextFragment = null;
            Bundle bundle = new Bundle();

            bundle.putString("GameType", currentGameInfo.gameType);

            if (currentGameInfo.gameType.equals("picture")) {
                nextFragment = new FragmentPictureCheep();
                bundle.putString("PictureOrCheep", "picture");
                nextFragment.setArguments(bundle);
            } else if (currentGameInfo.gameType.equals("name")) {
                nextFragment = new FragmentName();
                nextFragment.setArguments(bundle);
            } else if (currentGameInfo.gameType.equals("cheep")) {
                nextFragment = new FragmentPictureCheep();
                bundle.putString("PictureOrCheep", "cheep");
                bundle.putInt("UserVolume", getCurrentUserVol());
                nextFragment.setArguments(bundle);
            } else {
                assert false : "Unknown game type";
            }

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.gameFrame, nextFragment, "gameFragment");
            hideNextButton();
            currentFragment = nextFragment;
            fragmentTransaction.commit();
            writeInfo();
        } else if (currentGameInfo.questionIndex == currentGameInfo.numQuestions) {
            Fragment nextFragment = new FragmentResults();
            Bundle bundle = new Bundle();
            bundle.putInt("CorrectAnswers", currentGameInfo.correctAnswers);
            bundle.putInt("NumQuestions", currentGameInfo.numQuestions);
            nextFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.gameFrame, nextFragment, "gameFragment");
            hideNextButton();
            currentFragment = nextFragment;
            fragmentTransaction.commit();
            writeInfo();
        } else {
            currentGameInfo.resetIndex();
            Intent intent = new Intent(this, ActivityMain.class);
            intent.putExtra("PlayTime", getAnimatorPlayTime());
            //This flag makes ActivityMain be the only one, so that BackButton will exit app.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void initCountdown() {
        countdown = new Runnable() {
            @Override
            public void run() {
                finnishFragment(null);
            }
        };
        handler.postDelayed(countdown, getResources().getInteger(R.integer.next_countdown));
    }

    public void cancelCountdown() {
        handler.removeCallbacks(countdown);
        countdown = null;
    }

    public void showNextButton() {
        findViewById(R.id.butFrameNext).setVisibility(View.VISIBLE);
        canAdvanceToNextFragment = true;
    }

    public void hideNextButton() {
        findViewById(R.id.butFrameNext).setVisibility(View.INVISIBLE);
    }

    public void writeInfo() {
        String title;
        if (currentGameInfo.questionIndex < currentGameInfo.numQuestions) {
            setTitleWeight("small");
            title = getResources().getString(R.string.x_y);
            title = String.format(title, currentGameInfo.questionIndex + 1, currentGameInfo.numQuestions);
        } else {
            setTitleWeight("large");
            title = getResources().getString(R.string.title_fragment_results);
        }
        butTitle.setText(title);
    }

    private void setTitleWeight(String size){
        View gameBar = findViewById(R.id.gameBar);
        if(gameBar == null){
            return;
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) gameBar.getLayoutParams();
        switch(size){
            case "small":
                params.weight = getResources().getInteger(R.integer.game_title_weight_small);
                break;
            case "large":
                params.weight = getResources().getInteger(R.integer.game_title_weight_big);
                break;
        }
        gameBar.setLayoutParams(params);
    }

    private int getCurrentUserVol() {
        if (currentFragment instanceof FragmentPictureCheep) {
            return ((FragmentPictureCheep) currentFragment).currentUserVol;
        } else {
            return -1;
        }
    }

    private void cancelPlayingCheep() {
        if (currentFragment instanceof FragmentPictureCheep) {
            ((FragmentPictureCheep) currentFragment).cancelPlayingCheep();
        }
    }
}

