package com.megatech.fms.helpers;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.BuildConfig;
import com.megatech.fms.FMSApplication;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.LCRDataModel;
import com.megatech.fms.model.FlightData;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.SettingModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClient {
    private String API_BASE_URL = BuildConfig.API_BASE_URL;
    private String token;

    public HttpClient()
    {
        this.token = FMSApplication.getApplication().getUser().getToken();
    }
    public HttpClient(String token)
    {
        this.token = token;
    }

    public JSONObject login(String username, String password) {
        String loginUrl = API_BASE_URL + "/token";
        String contentType = "application/x-www-form-urlencoded";
        try {
            String data = sendPOST(loginUrl, "grant_type=password&username=" + username + "&password=" + password);// executeUrl(loginUrl,contentType,"POST", "grant_type=password&username="+username+"&password="+password);
            try {
                JSONObject obj = new JSONObject(data);
                return obj;
            } catch (JSONException e) {

            }
        } catch (IOException e) {
        }
        return null;
    }
    public boolean putRefuelData(LCRDataModel dataModel)
    {
        return true;
    }

    public FlightData getFlightData(Integer id)
    {
        String url = API_BASE_URL +"api/flights/";
        String contentType = "application/json";
        try {
            url = url+ id.toString();
            String data = sendGET(url);
            try {
                JSONObject json = new JSONObject(data);
                if (json.has("AircraftCode")) {
                    FlightData flight = new FlightData();
                    flight.setAircraftCode(json.getString("AircraftCode"));
                    flight.setCode(json.getString("Code"));
                    return flight;
                }
            }catch (JSONException e){}
        }
        catch (IOException e)
        {}
        return null;
    }

    public RefuelItemData getRefuelItem(Integer id)
    {
        String url = API_BASE_URL + "api/refuels/";
        String contentType = "application/json";
        try{
            url = url+id.toString();
            String data = sendGET(url);
            try {
                JSONObject json = new JSONObject(data);
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
                RefuelItemData item = gson.fromJson(data, RefuelItemData.class);
                return item;
            }catch (JSONException e){
                return  null;

            }

        }
        catch (Exception e)
        {

        }
        return  null;
    }

    private JSONArray getUrl(String url)
    {
        return new JSONArray();
    }
    private JSONArray putData(String url, JSONArray data)
    {
        return new JSONArray();
    }
    private final String USER_AGENT = "Mozilla/5.0";

    private String sendGET(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (this.token!=null)
        con.setRequestProperty("Authorization", "bearer "+ this.token);
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            return response.toString();
        } else {
            return "GET request not worked";
        }

    }

    private String sendPOST(String url,String params ) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (this.token!=null)
            con.setRequestProperty("Authorization", "bearer "+ this.token);
        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(params.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            return response.toString();
        } else {
            return "POST request not worked";
        }
    }

    public String executeUrl(String targetURL,String contentType,String method, String urlParameters) {
        int timeout=5000;
        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type",contentType);

            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            // Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200 ) {
                // Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                return response.toString();
            }
            else
                return connection.getResponseMessage();

        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
    public List<RefuelItemData> getRefuelList()
    {
        return  getRefuelList(false);
    }
    public List<RefuelItemData> getRefuelList(boolean others) {
        String url = API_BASE_URL + "api/refuels?truckNo="+FMSApplication.getApplication().getTruckNo()+"&o="+(others?"1":"0");

        List<RefuelItemData> lst = new ArrayList<RefuelItemData>();
        try {
            String data = sendGET(url);
            JSONArray arr = new JSONArray(data);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            if (arr.length()>0)
            {
                for (int i=0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    RefuelItemData item = new RefuelItemData();
                    /*item.setEstimateAmount(o.getDouble("EstimateAmount"));
                    item.setFlightCode(o.getString("FlightCode"));
                    item.setAircraftCode(o.getString("AircraftCode"));
                    item.setParkingLot(o.getString("Parking"));
                    item.setFlightId(o.getInt("FlightId"));
                    */
                    item = gson.fromJson(o.toString(), RefuelItemData.class);
                    lst.add(item);
                }
            }
        }
        catch (Exception e)
        {
            Log.e("getRefuelList", e.getMessage());
            return  lst;
        }
        return lst;

    }

    public RefuelItemData postRefuel(RefuelItemData refuelData) {
        String url = API_BASE_URL + "api/refuels";
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            String parm = gson.toJson(refuelData);
            String data = sendPOST(url, parm);
            JSONObject o = new JSONObject(data);
            return  gson.fromJson(data, RefuelItemData.class);
        }
        catch (Exception e)
        {
            Log.e("postRefuel", e.getMessage());

        }
        return  null;

    }

    public void updateTruckAmount(String truckNo, float currentAmount) {

        String url = API_BASE_URL+"api/trucks/amount";
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            String parm = gson.toJson(new SettingModel(truckNo,currentAmount));
            String data = sendPOST(url, parm);

        }
        catch (Exception e)
        {
            Log.e("updateTruckAmount", e.getMessage());
        }
    }


    public double getTruckAmount(String truckNo) {

        String url = API_BASE_URL+"api/trucks?truckNo="+truckNo;
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = sendGET(url);

            SettingModel model = gson.fromJson(data, SettingModel.class);
            if (model!=null)
            {
                FMSApplication.getApplication().setCurrentAmount(model.getCurrentAmount());
                return model.getCurrentAmount();
            }

        }
        catch (Exception e)
        {
            Log.e("updateTruckAmount", e.getMessage());
        }
        return 0;
    }

    public List<String> getIMEIList() {
        String url = API_BASE_URL+"files/imei.txt";
        try {



            String data = sendGET(url);
            return Arrays.asList(data.split(","));


        }
        catch (Exception e)
        {
            Log.e("getIMEIList", e.getMessage());
        }
        return null;
    }

    public List<AirlineModel> getAirlines() {
        String url = API_BASE_URL+"api/airlines";
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = sendGET(url);

            JSONArray arr = new JSONArray(data);
            List<AirlineModel> lst = new ArrayList<AirlineModel>();
            if (arr.length()>0)
            {

                for (int i=0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);

                    AirlineModel item = gson.fromJson(o.toString(), AirlineModel.class);
                    lst.add(item);
                }
            }
            return lst;
        }
        catch (Exception e)
        {
            Log.e("updateTruckAmount", e.getMessage());
        }
        return null;
    }
}
