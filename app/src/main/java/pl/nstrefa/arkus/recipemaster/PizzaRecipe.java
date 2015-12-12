package pl.nstrefa.arkus.recipemaster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.concurrent.ExecutionException;

public class PizzaRecipe extends AppCompatActivity {

    protected JSONObject recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_recipe);
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

    }

    protected void fillActivity(JSONObject recipe) throws JSONException {
        if(recipe != null) {
            TextView recipeName = (TextView) findViewById(R.id.recipeName);
            TextView description = (TextView) findViewById(R.id.description);
            TextView ingredientsList = (TextView) findViewById(R.id.ingredientsList);
            TextView preparingList = (TextView) findViewById(R.id.preparingList);
            JSONArray ingredients = recipe.getJSONArray("ingredients");
            JSONArray preparing = recipe.getJSONArray("preparing");
//            JSONArray images = recipe.getJSONArray("images");
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
        }
    }

}
