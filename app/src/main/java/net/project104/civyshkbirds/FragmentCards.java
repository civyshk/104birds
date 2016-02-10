package net.project104.civyshkbirds;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.project104.civyshkbirds.R;


public class FragmentCards extends Fragment {

    AdapterCard mAdapter;

    public FragmentCards() {    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cards, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GridView gridview = (GridView) getView().findViewById(R.id.gvCards);

        //gridview.setNumColumns(2);//commented for a reason

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //we have the position, so get the birdName or the Bird
                //launch new activity
                ((ActivityCards) getActivity()).launchCardActivity(position);

            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        mAdapter = new AdapterCard(getActivity());
        mAdapter.loadBirds(((ActivityCards) getActivity()).getBirdsByFamily(), getActivity());
        //Warning, note it doesn't get birds with only cheeps (without pictures)

        GridView gridview = (GridView) getView().findViewById(R.id.gvCards);
        gridview.setAdapter(mAdapter);

        super.onResume();
    }

    @Override
    public void onPause(){
        //gridview holds adapter, which holds context. Is gridview removed? I don't know, so I delete context
        //mAdapter.setContext(null);
        mAdapter = null;
        super.onPause();
    }

    @Override
    public void onDestroyView(){
        GridView gridview = (GridView) getView().findViewById(R.id.gvCards);
        gridview.setAdapter(null);
        gridview.setOnItemClickListener(null);
        super.onDestroyView();
    }

}
