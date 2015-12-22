package pl.nstrefa.arkus.recipemaster;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class PizzaRecipe extends AppCompatActivity {

    protected JSONObject recipe;
    protected String userName;
    protected Uri userPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_recipe);
        Intent i = getIntent();
        userName = i.getStringExtra("username");
        userPicture = (Uri) i.getExtras().get("userpicture");
        try {
            recipe =  new JSONObject(i.getStringExtra("recipeJSON"));
            fillActivity(recipe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(userName != null) {
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.fb_bar);
            stub.inflate();
            TextView user = (TextView) findViewById(R.id.userName);
            String logged = getResources().getString(R.string.logged_as) + " " + userName;
            user.setText(logged);
        } else {
            Space space = (Space) findViewById(R.id.space);
            space.setVisibility(View.GONE);
        }
        if(userPicture != null) {
            Picasso.with(getApplicationContext()).load(userPicture).
                    into((ImageView) findViewById(R.id.userPicture));
        }
        setResult(RESULT_FIRST_USER, i);
    }

    protected void fillActivity(final JSONObject recipe) throws JSONException {
        if(recipe != null) {
            TextView recipeName = (TextView) findViewById(R.id.recipeName);
            TextView description = (TextView) findViewById(R.id.description);
            TextView ingredientsList = (TextView) findViewById(R.id.ingredientsList);
            TextView preparingList = (TextView) findViewById(R.id.preparingList);

            JSONArray ingredients = recipe.getJSONArray("ingredients");
            JSONArray preparing = recipe.getJSONArray("preparing");
            final JSONArray images = recipe.getJSONArray("imgs");

            recipeName.setText(recipe.getString("title"));
            description.setText(recipe.getString("description"));

            ingredientsList.setText("");
            String ing = new String();
            for(int i = 0; i < ingredients.length(); i++) {
                ing += "- " + ingredients.getString(i) + "\n";
            }
            ingredientsList.setText(ing);
            preparingList.setText("");
            String pre = new String();
            for(int i = 0; i < preparing.length(); i++) {
                pre += Integer.toString(i+1) + ". " + preparing.getString(i) + "\n";
            }
            preparingList.setText(pre);
            for(int i = 0; i < images.length(); i++) {
                ImageView iv = new ImageView(getApplicationContext());
                switch (i) {
                    case 0: iv = (ImageView) findViewById(R.id.pizzaImage1);
                            break;
                    case 1: iv = (ImageView) findViewById(R.id.pizzaImage2);
                        break;
                    case 2: iv = (ImageView) findViewById(R.id.pizzaImage3);
                        break;
                }
                Picasso.with(getApplicationContext()).load(images.getString(i)).into(iv);
                final int imageid = i;
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            //File[] aDirArray = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
                            imageDownload(getApplicationContext(), images.getString(imageid) , recipe.getString("title") + Integer.toString(imageid + 1) + ".jpg");
                            Toast.makeText(getApplicationContext(), "Pobrano obrazek nr " + Integer.toString(imageid + 1),Toast.LENGTH_SHORT).show();
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //imageDownload(getApplicationContext(), Integer.toString(i) + ".jpg");
            }
        }
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, getIntent());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void imageDownload(Context ctx, String url, String pictureName){
        Picasso.with(ctx)
                .load(url)
                .into(getTarget(pictureName));
    }

    //target to save
    private static Target getTarget(final String pictureName) {
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                + File.separator + "Camera" + File.separator + "RecipeMaster_" + pictureName);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                            ostream.flush();
                            ostream.close();
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }

}
