package ml.paradox.githubsearch;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {



    private String sort = "" ;
    private String url = "https://api.github.com/search/repositories?q=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SearchView searchView = (SearchView)(findViewById(R.id.search));
        //searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()>0){
                    url = url + query;
                    Log.i("search", url);
                    searchGithub();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        LinearLayout linearLayoutOfSearchView = (LinearLayout) searchView.getChildAt(0);
        ImageButton optionsButton = new ImageButton(this);
        optionsButton.setImageResource(R.drawable.rounded_bg_less);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(100,100);
        optionsButton.setLayoutParams(params);
        linearLayoutOfSearchView.addView(optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("search", "dialog");
                createSearchOptionDialog();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_default:
                if (checked)
                    sort = "";
                break;
            case R.id.radio_stars:
                if (checked)
                    sort = "&sort=stars";
                break;
            case R.id.radio_forks:
                if (checked)
                    sort = "&sort=forks";
                break;
            case R.id.radio_updated:
                if (checked)
                    sort = "&sort=updated";
        }
    }
    // Private functions
    private void displayResults(ArrayList<gitHubRepo> repos){
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        container.removeAllViews();

        for(gitHubRepo g : repos){
            View result = getLayoutInflater().inflate(R.layout.result_item,null);
            ResultClickListener listener = new ResultClickListener();
            listener.init(this,g.url);
            result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("clicked","clicked!");
                }
            });
            ((TextView) result.findViewById(R.id.repo_name)).setText(g.name);
            ((TextView)result.findViewById(R.id.repo_full_name)).setText(g.fullName);
            ((TextView)result.findViewById(R.id.forks_count)).setText("Forks: "+Integer.toString(g.forks));
            ((TextView)result.findViewById(R.id.stars_count)).setText("Stars: "+Integer.toString(g.stars));
            ((TextView)result.findViewById(R.id.watchers_count)).setText("Watching: "+Integer.toString(g.watching));

            String details = "Language: " + g.language ;
            details = details +"\nDescription\n" + g.description ;
            ((TextView)result.findViewById(R.id.result_details)).setText(details);
            container.addView(result);
        }
    }
    private void searchGithub(){
        url = url.replace(" ","%20");
        Toast.makeText(this,url,Toast.LENGTH_SHORT).show();
        new getResultsGithub(this).execute(url);
        url = "https://api.github.com/search/repositories?q=";
    }
    private int min(int a,int b){
        if(a<b) return a ;
        return b ;
    }
    private void getRepoList(JSONObject obj){
        try {
            JSONArray repoList = obj.getJSONArray("items");
            ArrayList<gitHubRepo> repositaries = new ArrayList<>();
            for(int i=0;i< min(repoList.length(),20);++i){
                JSONObject jb = repoList.getJSONObject(i);
                repositaries.add(new gitHubRepo(jb));
            }
            displayResults(repositaries);
        }catch(org.json.JSONException e){
            /* TODO: Error message */
        }
    }

    private void createSearchOptionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View root = getLayoutInflater().inflate(R.layout.options_alert,null);
        builder.setView(root);
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText lan = (EditText) root.findViewById(R.id.language);
                EditText title = (EditText) root.findViewById(R.id.alert_title);
                CheckBox c = (CheckBox) root.findViewById(R.id.decending);
                String name = title.getText().toString();
                if(name.length()>0) {
                    url = url + name ;
                    Boolean desc = c.isChecked();
                    String language = lan.getText().toString();
                    if (language.length()>0) url = url + "+language:" + language;
                    url = url + sort;
                    if (!desc)
                        url = url + "&order=asc";
                    searchGithub();
                }
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private class getResultsGithub extends AsyncTask<String,Void,Void> {

        private Context context = null ;
        private JSONObject res ;

        getResultsGithub(Context c){
            super();
            context = c;
        }
        @Override
        protected Void doInBackground(String... params) {
            RequestQueue queue = Volley.newRequestQueue(context);
            JSONObject ob = new JSONObject();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, params[0], ob,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            getRepoList(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context,"Error connecting to internet",Toast.LENGTH_SHORT).show();
                            Log.i("Voley",error.toString());
                            res = new JSONObject();
                        }
            });

            queue.add(request);
            return null;
        }

    }

    private class ResultClickListener implements View.OnClickListener{
        String url = "";
        Context c ;
        public void init(Context context,String link){
            url = link ;
            c = context;
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(c,url,Toast.LENGTH_SHORT);
        }
    }


}

