package net.project104.civyshkbirds;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;


public class ActivityCards extends ActivityAnimation {
    //This activity works both for FragmentCards and FragmentCard

    private static final String TAG = ActivityCards.class.getSimpleName();

    Map<String, Bird> birdsByName;
    TreeMap<String, List<Bird>> birdsByFamily;
    List<Bird> sortedBirds;
    String typeFragment;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = null;
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
            fragmentTransaction = fragmentManager.beginTransaction();
        } else {
            bundle = savedInstanceState;
        }
        super.onCreate(savedInstanceState, bundle, R.layout.activity_cards);

        if (bundle != null) {
            typeFragment = bundle.getString("TypeFragment");
        } else {
            //Probably coming from UP action bar button. Sadly, no bundle to get info from, so just get FragmentCards
            //And that gives me an untraceable crash. So currently every UP button redirects to ActivityMain, waiting for a better fix
            typeFragment = "cards";
        }

        sortedBirds = new ArrayList<Bird>();
        birdsByName = new HashMap<String, Bird>();
        birdsByFamily = new TreeMap<String, List<Bird>>(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });//Comparator unneeded, but included for future uses.

        ActivityMain.inflateBirds(this, birdsByName, birdsByFamily);
        for (String family : birdsByFamily.navigableKeySet()) {
            sortedBirds.addAll(birdsByFamily.get(family));
        }

        switch (typeFragment) {
            case "cards":
                if (savedInstanceState == null) {//This IF avoids duplicated fragments, I guess
                    Fragment oldFragment = fragmentManager.findFragmentByTag(FragmentCard.TAG);
                    if (oldFragment != null) {
                        fragmentTransaction.remove(oldFragment);
                    }
                    fragmentTransaction.replace(R.id.rootSingleFragment, new FragmentCards());
                }
                break;
            case "card"://not used anymore, probably
                if (savedInstanceState == null) {
                    FragmentCard fragmentCard = new FragmentCard();
                    fragmentCard.setArguments(bundle);
                    fragmentTransaction.replace(R.id.rootSingleFragment, fragmentCard, FragmentCard.TAG);
                }
                break;
            case "birdName"://coming from a game, probably
                if (savedInstanceState == null) {
                    String birdName = bundle.getString("BirdName");
                    Bird bird = birdsByName.get(birdName);
                    int position = sortedBirds.indexOf(bird);
                    FragmentCard fragmentCard = FragmentCard.newInstance(bird, position - 1 >= 0, position + 1 < sortedBirds.size());
                    fragmentTransaction.replace(R.id.rootSingleFragment, fragmentCard);
//                    fragmentManager.popBackStack("FragmentGame", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                break;
        }
        if (savedInstanceState == null) {
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("TypeFragment", typeFragment);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_cards, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public TreeMap<String, List<Bird>> getBirdsByFamily() {
        return birdsByFamily;
    }

//    public void launchCardActivity(int position){
//        //Start a new activity instead of replacing a fragment??? don't use this. remove
//        Intent intent = new Intent(this, ActivityCards.class);
//        Bird bird = sortedBirds.get(position);
//        intent.putExtra("Bird", bird);
//        intent.putExtra("TypeFragment", "card");
//        intent.putExtra("PlayTime", getAnimatorPlayTime());
//        startActivity(intent);
//    }

    void showFragmentCard(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bird bird = sortedBirds.get(position);

        FragmentCard fragmentCard = FragmentCard.newInstance(bird, position - 1 >= 0, position + 1 < sortedBirds.size());
        fragmentTransaction.replace(R.id.rootSingleFragment, fragmentCard, FragmentCard.TAG);
        if(typeFragment.equals("cards")){
            String backStackTag = "FragmentCards";
            fragmentManager.popBackStack(backStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.addToBackStack(backStackTag);
        }
        fragmentTransaction.commit();
    }

    public void showAdjacentFragment(Bird bird, int advance) {
        //bird comes from a Fragment, and it is a different instance of Birds contained in sortedBirds
        //so don't use currentPosition = sortedBirds.indexOf(bird);
        int currentPosition = 0;
        for (Bird sortedBird : sortedBirds) {
            if (sortedBird.latinName.equals(bird.latinName)) {
                break;
            }
            currentPosition++;
        }

        if (currentPosition >= 0 && currentPosition < sortedBirds.size()) {
            int newPosition = currentPosition + advance;
            if (newPosition >= 0 && newPosition < sortedBirds.size()) {
                showFragmentCard(newPosition);
            }
        } else {
            Log.w(TAG, "List sortedBirds doesn't contain a bird. How?");
        }
    }
}

