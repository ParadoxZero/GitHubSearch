package ml.paradox.githubsearch;/*
 * Created by Zero on 15-Jun-16.
 */

import org.json.JSONObject;

public class gitHubRepo {
    public int id = 0;
    public String name ="";
    public String fullName = "";
    public String url = "";
    public String description = "" ;
    public int stars = 0 ;
    public int forks = 0 ;
    public int watching = 0 ;
    public String language = "" ;
    //public JSONObject owner = null ;
    public gitHubRepo(JSONObject obj){
        try {
            id = (int) obj.get("id");
            name = (String) obj.get("name");
            fullName = (String) obj.get("full_name");
            url = (String) obj.get("html_url");
            description = (String) obj.get("description");
            stars = (int) obj.get("stargazers_count");
            forks = (int) obj.get("forks_count");
            watching = (int) obj.get("watchers_count");
            language = (String) obj.get("language");
        }
        catch(Exception e){

        }

        /*try{
            owner = (JSONObject) obj.get("owner");
        }catch(org.json.JSONException e){}
        */
    }
}
