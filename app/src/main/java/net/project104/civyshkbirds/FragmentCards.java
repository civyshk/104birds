package net.project104.civyshkbirds;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridView gridview = (GridView) view.findViewById(R.id.gvCards);

        //gridview.setNumColumns(2);//commented for a reason

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ((ActivityCards) getActivity()).showFragmentCard(position);
            }
        });


        mAdapter = new AdapterCard(getActivity());
        mAdapter.loadBirds(((ActivityCards) getActivity()).getBirdsByFamily(), getActivity());

        gridview = (GridView) getView().findViewById(R.id.gvCards);
        gridview.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroyView(){
        GridView gridview = (GridView) getView().findViewById(R.id.gvCards);
        gridview.setAdapter(null);
        gridview.setOnItemClickListener(null);
        super.onDestroyView();
    }

}
