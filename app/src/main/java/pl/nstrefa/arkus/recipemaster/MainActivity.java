package pl.nstrefa.arkus.recipemaster;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    protected String userName;
    protected CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fb
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            private ProfileTracker profileTracker;

            @Override
            public void onSuccess(LoginResult loginResult) {
                if (Profile.getCurrentProfile() == null) {
                    profileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            Log.v("facebook - profile", currentProfile.getFirstName());
                            profileTracker.stopTracking();
                        }
                    };
                    profileTracker.startTracking();
                } else {
                    Profile profile = Profile.getCurrentProfile();
                    Log.v("facebook - profile", profile.getFirstName());
                }
                userName = Profile.getCurrentProfile().getName();
                Toast.makeText(getApplicationContext(), "Zalogowano jako " + userName,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Log.v("facebook - onCancel", "cancelled");
                Toast.makeText(getApplicationContext(), "Anulowano", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.v("facebook - onError", error.getMessage());
                Toast.makeText(getApplicationContext(), "Błąd logowania", Toast.LENGTH_LONG).show();
            }
        });

        //fb
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Logs 'install' and 'app activate' App Events.
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.fb);
        final FloatingActionButton recipe = (FloatingActionButton) findViewById(R.id.getrecipe);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                switch (fb.getVisibility()) {
                    case View.VISIBLE :
                        fb.setVisibility(View.GONE);
                        recipe.setVisibility(View.GONE);
                        LoginManager.getInstance().logOut();
                        break;
                    case View.GONE:
                        fb.setVisibility(View.VISIBLE);
                        recipe.setVisibility(View.VISIBLE);
                        facebookLogin();
                        break;
                }
            }
        });
        ImageButton pizzaImage = (ImageButton) findViewById(R.id.pizzaImage);
        pizzaImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), PizzaRecipe.class);
                i.putExtra("username", userName);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    protected void facebookLogin() {
        LoginManager lm = LoginManager.getInstance();
        lm.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
        Toast.makeText(getApplicationContext(), "facebookLogin()", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }
}
