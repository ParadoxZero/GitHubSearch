package ml.paradox.githubsearch;

import android.app.AlertDialog;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

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
                Log.i("search","dialog");
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
    private void searchGithub(){
        Log.i("search",url);
        Toast.makeText(this,url,Toast.LENGTH_SHORT).show();
        url = "https://api.github.com/search/repositories?q=";
    }

    private void getRepoList(JSONObject obj){

    }

    private void createSearchOptionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setView(R.layout.options_alert);
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

    private class getResultsGithub extends AsyncTask<String,Void,JSONObject> {

        private Context context = null ;
        private JSONObject res ;

        getResultsGithub(Context c){
            super();
            context = c;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            RequestQueue queue = Volley.newRequestQueue(context);
            JSONObject ob = new JSONObject();
            JsonObjectRequest request = new JsonObjectRequest(params[0], ob, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    res = response;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context,"Error connecting to internet",Toast.LENGTH_SHORT).show();
                    res = new JSONObject();
                }
            });
            return res;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            getRepoList(jsonObject);
        }
    }

}

