package com.loyid.orangedict;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class ViewWordActivity extends AppCompatActivity {
    private static final String TAG = ViewWordActivity.class.getSimpleName();
    private ViewWordActivityFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_word);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.floating_add_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment != null) {
                    mFragment.editGrammar();
                }
            }
        });

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putLong("grammar_id", getIntent().getLongExtra("grammar_id", -1));

            ViewWordActivityFragment fragment = new ViewWordActivityFragment();
            mFragment = fragment;
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
