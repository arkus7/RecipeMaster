package pl.nstrefa.arkus.recipemaster;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.Space;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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
            String logged = "Logged as " + userName;
            user.setText(logged);
        } else {
            Space space = (Space) findViewById(R.id.space);
            space.setVisibility(View.GONE);
            //TODO: remove space
        }
        if(userPicture != null) {
            Picasso.with(getApplicationContext()).load(userPicture).into((ImageView) findViewById(R.id.userPicture));
        }
        setResult(RESULT_FIRST_USER, i);
        //Toast.makeText(getApplicationContext(), userName, Toast.LENGTH_LONG).show();

    }

    protected void fillActivity(JSONObject recipe) throws JSONException {
        if(recipe != null) {
            TextView recipeName = (TextView) findViewById(R.id.recipeName);
            TextView description = (TextView) findViewById(R.id.description);
            TextView ingredientsList = (TextView) findViewById(R.id.ingredientsList);
            TextView preparingList = (TextView) findViewById(R.id.preparingList);

            JSONArray ingredients = recipe.getJSONArray("ingredients");
            JSONArray preparing = recipe.getJSONArray("preparing");
            JSONArray images = recipe.getJSONArray("imgs");

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
}
