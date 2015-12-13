package pl.nstrefa.arkus.recipemaster;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    protected String userName;
    protected Uri userPicture;
    protected CallbackManager callbackManager;
    static final int RETURN_FROM_PIZZA_RECIPE = 0;

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
                userPicture = Profile.getCurrentProfile().getProfilePictureUri(50, 50);
                Snackbar.make(findViewById(R.id.activity_main), "Zalogowano jako " + userName,Snackbar.LENGTH_LONG).setAction("Action",null).show();
                //Toast.makeText(getApplicationContext(), "Zalogowano jako " + userName, Toast.LENGTH_LONG).show();
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

        if(Profile.getCurrentProfile() != null) {
            userName = Profile.getCurrentProfile().getName();
            userPicture = Profile.getCurrentProfile().getProfilePictureUri(50,50);
        }

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
                        break;
                    case View.GONE:
                        fb.setVisibility(View.VISIBLE);
                        recipe.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        ImageButton pizzaImage = (ImageButton) findViewById(R.id.pizzaImage);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Profile.getCurrentProfile() == null)
                    facebookLogin();
                else {
                    logOutPopup();
                }
            }
        });

        recipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPizzaRecipeActivity(v);
            }
        });

        pizzaImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPizzaRecipeActivity(v);
            }
        });
    }

    private void logOutPopup() {
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup, (ViewGroup) findViewById(R.id.popup));
        final PopupWindow popupWindow = new PopupWindow(layout,1100,400,true);
        popupWindow.setAnimationStyle(-1);
        popupWindow.showAtLocation(layout, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        Button yes = (Button) layout.findViewById(R.id.logOutYesButton);
        Button cancel = (Button) layout.findViewById(R.id.logOutCancelButton);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                userName = null;
                userPicture = null;
                popupWindow.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
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
        LoginManager.getInstance().logInWithReadPermissions(this, null);
        //Toast.makeText(getApplicationContext(), "facebookLogin()", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        if(requestCode == RETURN_FROM_PIZZA_RECIPE) {
            if(resultCode == RESULT_FIRST_USER) {
                userName = data.getStringExtra("username");
                userPicture = (Uri) data.getExtras().get("userpicture");
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if(userName != null && userPicture != null) {
            savedInstanceState.putString("username", userName);
            savedInstanceState.putString("userpicture", userPicture.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            userName = savedInstanceState.getString("username");
            userPicture = Uri.parse(savedInstanceState.getString("userpicture"));
        } else {
            // Probably initialize members with default values for a new instance
        }

    }

    protected void startPizzaRecipeActivity(View v) {
        Intent i = new Intent(v.getContext(), PizzaRecipe.class);
        i.putExtra("username", userName);
        i.putExtra("userpicture", userPicture);
        try {
            JSONObject recipeJSON = new RetrieveRecipeTask().execute("http://mooduplabs.com/test/info.php").get();
            if(recipeJSON.getString("title") == null) {
                throw new JSONException("No JSONObject downloaded");
            }
            i.putExtra("recipeJSON", recipeJSON.toString());
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(i, RETURN_FROM_PIZZA_RECIPE);
        } catch (InterruptedException ie) {
            Toast.makeText(getApplicationContext(),"Interrupted", Toast.LENGTH_LONG).show();
        } catch (ExecutionException ee) {
            Toast.makeText(getApplicationContext(),"Failed to execute", Toast.LENGTH_LONG).show();
        }
        catch (JSONException jsone) {
            Toast.makeText(getApplicationContext(),"Can't download recipe. Is your connection active?", Toast.LENGTH_LONG).show();
            //TODO: if error, don't open this activity.
        }
    }
}
