package pl.nstrefa.arkus.recipemaster;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.fb);
                FloatingActionButton recipe = (FloatingActionButton) findViewById(R.id.getrecipe);
                switch (fb.getVisibility()) {
                    case View.VISIBLE :
                        fb.setVisibility(View.GONE);
                        recipe.setVisibility(View.GONE);
                        break;
                    case View.GONE:
                        fb.setVisibility(View.VISIBLE);
                        recipe.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

}
