package pl.nstrefa.arkus.recipemaster;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

class RetrieveRecipeTask extends AsyncTask<String, Void, JSONObject> {

    protected JSONObject doInBackground(String... siteUrl) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String json = new String();
        try {
            url = new URL(siteUrl[0]);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                json += line;
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            return new JSONObject(json);
        } catch(JSONException je) {
            return new JSONObject();
        }
    }

}
