package pl.nstrefa.arkus.recipemaster;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by arkus on 12/12/15.
 */
public class RetrieveRecipeTask extends AsyncTask<String, Void, JSONObject> {

    private Exception exception;
    protected JSONObject recipe;

    protected JSONObject doInBackground(String... siteUrl) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String wholeSite = new String();
        try {
            url = new URL(siteUrl[0]);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                wholeSite += line;
            }
            Log.d("getRecipe", wholeSite);
        } catch (MalformedURLException mue) {

        } catch (IOException ioe) {

        }
        try {
            return new JSONObject(wholeSite);
        } catch(JSONException je) {
            return new JSONObject();
        }
    }

    protected void onPostExecute(JSONObject object) {
        // TODO: check this.exception
        // TODO: do something with the recipe
    }
}
