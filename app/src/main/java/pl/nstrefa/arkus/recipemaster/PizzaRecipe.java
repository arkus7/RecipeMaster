package pl.nstrefa.arkus.recipemaster;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.concurrent.ExecutionException;

public class PizzaRecipe extends AppCompatActivity {

    protected JSONObject recipe;
    protected String userName;
    protected CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_pizza_recipe);
        Intent i = getIntent();
        userName = i.getStringExtra("username");
        try {
            recipe = new RetrieveRecipeTask().execute("http://mooduplabs.com/test/info.php").get();
            fillActivity(recipe);
        } catch (InterruptedException ie) {
            Toast.makeText(getApplicationContext(),"Interrupted", Toast.LENGTH_LONG).show();
        } catch (ExecutionException ee) {
            Toast.makeText(getApplicationContext(),"Failed to execute", Toast.LENGTH_LONG).show();
        } catch (JSONException jsone) {
            Toast.makeText(getApplicationContext(),"Failed reading JSON object", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), userName, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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

}
