package net.project104.civyshkbirds;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.project104.civyshkbirds.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class ActivityCards extends ActivityAnimation {
    //This activity works both for FragmentCards and FragmentCard

    //FragmentCards fields
    Map<String, Bird> birdsByName;
    TreeMap<String, List<Bird>> birdsByFamily;
    List<Bird> sortedBirds;
    String typeFragment;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle;
        FragmentTransaction fragmentTransaction = null;
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
            fragmentTransaction = getFragmentManager().beginTransaction();
        } else {
            bundle = savedInstanceState;
        }
        super.onCreate(savedInstanceState, bundle, R.layout.activity_cards);

        typeFragment = bundle.getString("TypeFragment");
        switch(typeFragment){
            case "cards":
                sortedBirds = new ArrayList<>();
                birdsByName = new HashMap<String, Bird>();
                birdsByFamily = new TreeMap<String, List<Bird>>(new Comparator<String>(){
                        @Override
                        public int compare(String lhs, String rhs) {
                        return lhs.compareTo(rhs);
                    }
                    });//Comparator unneeded, but included for future uses.
                ActivityMain.inflateBirds(this, birdsByName, birdsByFamily);
                for(String family : birdsByFamily.navigableKeySet()){
                    sortedBirds.addAll(birdsByFamily.get(family));
                }
                if(savedInstanceState == null) {
                    fragmentTransaction.add(R.id.rootSingleFragment, new FragmentCards());
                }
                break;
            case "card":
                if(savedInstanceState == null) {
                    FragmentCard fragmentCard = new FragmentCard();
                    fragmentCard.setArguments(bundle);
                    fragmentTransaction.add(R.id.rootSingleFragment, fragmentCard);
                }
                break;
            case "birdName":
                if(savedInstanceState == null) {
                    birdsByName = new HashMap<String, Bird>();
                    ActivityMain.inflateBirds(this, birdsByName, null);
                    String birdName = bundle.getString("BirdName");
                    Bird bird = birdsByName.get(birdName);
                    bundle.putSerializable("Bird", bird);
                    FragmentCard fragmentCard = new FragmentCard();
                    fragmentCard.setArguments(bundle);
                    fragmentTransaction.add(R.id.rootSingleFragment, fragmentCard);
                }
                break;
        }
        if(savedInstanceState == null) {
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString("TypeFragment", typeFragment);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
       //     return true;
      //  }

        return super.onOptionsItemSelected(item);
    }

    public TreeMap<String, List<Bird>> getBirdsByFamily(){
        return birdsByFamily;
    }

    public void launchCardActivity(int position){
        Intent intent = new Intent(this, ActivityCards.class);
        Bird bird = sortedBirds.get(position);
        intent.putExtra("Bird", bird);
        intent.putExtra("TypeFragment", "card");
        intent.putExtra("PlayTime", getAnimatorPlayTime());
        startActivity(intent);
    }


}

