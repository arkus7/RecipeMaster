package pl.nstrefa.arkus.recipemaster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
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
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    static final int RETURN_FROM_PIZZA_RECIPE = 0;
    private String userName;
    private Uri userPicture;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;
    private boolean alreadyRunning = false;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fb
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                accessToken = currentAccessToken;
            }
        };
        accessToken = AccessToken.getCurrentAccessToken();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {


            @Override
            public void onSuccess(LoginResult loginResult) {
                if (Profile.getCurrentProfile() == null) {
                    profileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            userName = currentProfile.getName();
                            userPicture = currentProfile.getProfilePictureUri(100, 100);
                            showLoggedAs();
                            profileTracker.stopTracking();
                        }
                    };
                    profileTracker.startTracking();
                } else if(AccessToken.getCurrentAccessToken() != null) {
                    Profile profile = Profile.getCurrentProfile();
                    userName = profile.getName();
                    userPicture = profile.getProfilePictureUri(100, 100);
                }

                if(userName != null) { showLoggedAs(); }
                FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.facebook);
                fb.setTitle(getResources().getString(R.string.facebook_logout));
            }

            @Override
            public void onCancel() {
                Log.v("facebook - onCancel", "cancelled");
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.login_aborted), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.v("facebook - onError", error.getMessage());
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.login_error), Toast.LENGTH_LONG).show();
            }
        });

        //fb
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //fb logged as

        if(Profile.getCurrentProfile() != null) {
            userName = Profile.getCurrentProfile().getName();
            userPicture = Profile.getCurrentProfile().getProfilePictureUri(50, 50);
        }

        try {
            Intent i = getIntent();
            alreadyRunning =  i.getBooleanExtra("alreadyrunning", false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if((!alreadyRunning && accessToken != null) || (userName != null && !alreadyRunning)) {
            showLoggedAs();
            alreadyRunning = true;
        }

        //fb logged as
        window = getWindow();
        final FloatingActionsMenu actionButton = (FloatingActionsMenu) findViewById(R.id.actionMenu);
        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.facebook);
        if(userName != null) {
            fb.setTitle(getResources().getString(R.string.facebook_logout));
        }
        FloatingActionButton recipe = (FloatingActionButton) findViewById(R.id.getRecipe);

        actionButton.setOnFloatingActionsMenuUpdateListener(
                new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            FrameLayout fl = (FrameLayout) findViewById(R.id.activity_main_content);
            @Override
            public void onMenuExpanded() {
                fl.setAlpha(0.2f);
                setStatusBarColor(R.color.colorPrimaryAlpha);
                fl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionButton.collapse();
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                fl.setAlpha(1.0f);
                setStatusBarColor(R.color.colorPrimaryDark);
            }
        });

        ImageButton pizzaImage = (ImageButton) findViewById(R.id.pizzaImage);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AccessToken.getCurrentAccessToken() == null) {
                    facebookLogin();
                } else {
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
                if(!actionButton.isExpanded()) {
                    startPizzaRecipeActivity(v);
                } else {
                    actionButton.collapse();
                }
            }
        });
    }

    private void showLoggedAs() {
        String message = getResources().getString(R.string.logged_in_as) + " " + userName;
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

    private void facebookLogOut() {
        LoginManager.getInstance().logOut();
        userName = null;
        userPicture = null;
        accessToken = null;
    }

    private void logOutPopup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.com_facebook_loginview_log_out_action))
                .setMessage(getResources().getString(R.string.logout_popup_message))
                .setIcon(R.drawable.facebook_box)
                .setPositiveButton(getResources().getString(R.string.logout_popup_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                facebookLogOut();
                                FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.facebook);
                                fb.setTitle(getResources().getString(R.string.facebook_login));
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.logged_out), Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.logout_popup_no), null)
                .show();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        if(requestCode == RETURN_FROM_PIZZA_RECIPE) {
            if(resultCode == RESULT_FIRST_USER) {
                FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.actionMenu);
                menu.collapseImmediately();
                userName = data.getStringExtra("username");
                userPicture = (Uri) data.getExtras().get("userpicture");
                alreadyRunning = data.getBooleanExtra("alreadyrunning", true);
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if(userName != null && userPicture != null) {
            savedInstanceState.putString("username", userName);
            savedInstanceState.putString("userpicture", userPicture.toString());
            savedInstanceState.putBoolean("alreadyrunning", alreadyRunning);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            userName = savedInstanceState.getString("username");
            userPicture = Uri.parse(savedInstanceState.getString("userpicture"));
            alreadyRunning = savedInstanceState.getBoolean("alreadyrunning");
        } else {
            userName = null;
            userPicture = null;
            alreadyRunning = false;
        }

    }

    protected void startPizzaRecipeActivity(View v) {
        Intent i = new Intent(v.getContext(), PizzaRecipe.class);
        i.putExtra("username", userName);
        i.putExtra("userpicture", userPicture);
        i.putExtra("alreadyrunning", alreadyRunning);
        try {
            JSONObject recipeJSON = new RetrieveRecipeTask().
                    execute(getResources().getString(R.string.recipe_url)).get();
            if(recipeJSON.getString("title") == null) {
                throw new JSONException("No JSONObject downloaded");
            }
            i.putExtra("recipeJSON", recipeJSON.toString());
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(i, RETURN_FROM_PIZZA_RECIPE);
        } catch (InterruptedException ie) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error_interrupted),
                    Toast.LENGTH_LONG).show();
        } catch (ExecutionException ee) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error_failed_to_execute),
                    Toast.LENGTH_LONG).show();
        }
        catch (JSONException jsone) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error_no_connection),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        if(profileTracker != null) {
            profileTracker.stopTracking();
        }
    }

    private void setStatusBarColor(int colorResID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            int intColor = ContextCompat.getColor(getApplicationContext(), colorResID);
            String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
            window.setStatusBarColor(Color.parseColor(hexColor));
        }
    }
}
