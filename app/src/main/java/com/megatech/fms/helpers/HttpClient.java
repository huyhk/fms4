package com.megatech.fms.helpers;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.BuildConfig;
import com.megatech.fms.FMSApplication;
import com.megatech.fms.data.entity.ParkingLot;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.FlightData;
import com.megatech.fms.model.LCRDataModel;
import com.megatech.fms.model.PermissionModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.ShiftModel;
import com.megatech.fms.model.TruckModel;
import com.megatech.fms.model.UserModel;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    public Boolean checkToken(String token) {
        String url = API_BASE_URL + "/api/user/check";
        String contentType = "application/json";
        try {
            String data = sendGET(url);
        } catch (Exception ex) {
        }
        return true;
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
            Log.e("ERROR", e.getMessage());
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

    public List<UserModel> getUsers() {
        String url = API_BASE_URL + "api/users";

        List<UserModel> lst = new ArrayList<UserModel>();
        try {
            String data = sendGET(url);
            JSONArray arr = new JSONArray(data);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            lst = Arrays.asList(gson.fromJson(data, UserModel[].class));
        } catch (Exception ex) {

        }
        return lst;
    }
    public List<TruckModel> getTrucks() {
        String url = API_BASE_URL + "api/trucks";

        List<TruckModel> lst = new ArrayList<TruckModel>();
        try {
            String data = sendGET(url);
            JSONArray arr = new JSONArray(data);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            lst = Arrays.asList(gson.fromJson(data, TruckModel[].class));
        } catch (Exception ex) {

        }
        return lst;
    }
    public String getContent(String url) {
        try {
            return sendGET(url);
        } catch (IOException ex) {
            return null;
        }
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
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(1000);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            if (this.token != null)
                con.setRequestProperty("Authorization", "bearer " + this.token);
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
                con.disconnect();
                // print result
                return response.toString();
            }
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return "Not Authorized";
            } else {

                return "GET request not worked";
            }
        } catch (SocketTimeoutException ex) {
            return "Time out";
        } catch (IOException ex) {
            return "IO error";
        }
    }

    private String sendPOST(String url,String params ) throws IOException {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(1000);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-Agent", USER_AGENT);
            if (this.token != null)
                con.setRequestProperty("Authorization", "bearer " + this.token);
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

                con.disconnect();
                // print result
                return response.toString();
            } else {
                return "POST request not worked";
            }
        } catch (SocketTimeoutException ex) {
            return "Timeout";
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
                    "" + urlParameters.getBytes().length);
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

    public ShiftModel getShift() {
        String url = API_BASE_URL + "api/shift";
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = sendGET(url);

            ShiftModel model = gson.fromJson(data, ShiftModel.class);
            model.setSelected(true);

            return model;
        } catch (Exception e) {
            Log.e("getShift", e.getMessage());
        }
        return null;
    }

    public List<RefuelItemData> getRefuelList()
    {
        return getRefuelList(false, 0);
    }

    public List<RefuelItemData> getRefuelList(boolean others) {
        return getRefuelList(others, 0);
    }
    public List<RefuelItemData> getRefuelList(boolean others, Integer t)
    {
        return getRefuelList(others, t, false);
    }
    public List<RefuelItemData> getRefuelList(boolean others, Integer t, boolean d) {

        String url = API_BASE_URL + "api/refuels?truckNo="
                + FMSApplication.getApplication().getTruckNo()
                + "&truckId="
                + FMSApplication.getApplication().getTruckId()
                + "&o=" + (others ? "1" : "0") + "&type=" + t.toString() + "&d=" + d;

        List<RefuelItemData> lst = new ArrayList<RefuelItemData>();
        try {
            String data = sendGET(url);
            JSONArray arr = new JSONArray(data);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            if (arr.length() > 0) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    RefuelItemData item;

                    item = gson.fromJson(o.toString(), RefuelItemData.class);
                    lst.add(item);
                }
            }
        } catch (Exception e) {
            Log.e("getRefuelList", e.getMessage());
            return null;
        }
        return lst;

    }

    public List<RefuelItemData> getModifiedRefuels(Integer t, Date lastModified) {

        String url = API_BASE_URL + "api/refuels/modified?type=" + t.toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS");
        if (lastModified != null)
            url += "&lastModified=" + dateFormat.format(lastModified);

        List<RefuelItemData> lst = new ArrayList<RefuelItemData>();
        try {
            String data = sendGET(url);
            JSONArray arr = new JSONArray(data);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            if (arr.length()>0)
            {
                for (int i=0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    RefuelItemData item;

                    item = gson.fromJson(o.toString(), RefuelItemData.class);

                    lst.add(item);
                }
            }
        }
        catch (Exception e)
        {
            Log.e("getRefuelList", e.getMessage());
            return null;
        }
        return lst;

    }

    public List<RefuelItemData> getExtractList() {

        return getRefuelList(true, 1);

    }

    public RefuelItemData postRefuel(RefuelItemData refuelData) {
        String url = API_BASE_URL + "api/refuels";
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            String parm = gson.toJson(refuelData);
            String data = sendPOST(url, parm);
            JSONObject o = new JSONObject(data);
            RefuelItemData newItem = gson.fromJson(data, RefuelItemData.class);
            if (newItem != null && refuelData.getId() == 0)
                refuelData.setId(newItem.getId());
            return newItem;
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
            String parm = gson.toJson(new TruckModel(truckNo, currentAmount));
            String data = sendPOST(url, parm);

        }
        catch (Exception e)
        {
            Log.e("updateTruckAmount", e.getMessage());
        }
    }

    public void postTruckFuel(int truckId, float addedAmount, String qcNo) {

        String url = API_BASE_URL+"api/trucks/fuel";
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            String parm = String.format("{\"truckId\":\"%d\",\"amount\":\"%f\",\"qcNo\":\"%s\"}",truckId, addedAmount, qcNo);
            String data = sendPOST(url, parm);

        }
        catch (Exception e)
        {
            Log.e("postTruckFuel", e.getMessage());
        }
    }
    public TruckModel postTruck(TruckModel model) {
        String url = API_BASE_URL + "api/trucks";
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            String parm = gson.toJson(model);
            String data = sendPOST(url, parm);
            JSONObject o = new JSONObject(data);
            return gson.fromJson(data, TruckModel.class);
        } catch (Exception e) {
            Log.e("postRefuel", e.getMessage());

        }
        return null;

    }

    public AirlineModel postAirline(AirlineModel model) {
        String url = API_BASE_URL + "api/airlines";
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String parm = gson.toJson(model);
        try {
            String data = sendPOST(url, parm);
            return gson.fromJson(data, AirlineModel.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public float getTruckAmount(String truckNo) {

        String url = API_BASE_URL+"api/trucks?truckNo="+truckNo;
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = sendGET(url);

            TruckModel model = gson.fromJson(data, TruckModel.class);
            return model.getCurrentAmount();

        }
        catch (Exception e)
        {
            Log.e("updateTruckAmount", e.getMessage());
        }
        return 0;
    }

    public PermissionModel getPermission() {
        String url = API_BASE_URL + "api/account/permission";
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = sendGET(url);

            PermissionModel model = gson.fromJson(data, PermissionModel.class);
            return model;
        } catch (Exception ex) {
            return null;
        }
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

    public List<ParkingLot> getParking() {
        String url = API_BASE_URL + "api/parking";
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = sendGET(url);

            JSONArray arr = new JSONArray(data);
            List<ParkingLot> lst = new ArrayList<>();
            if (arr.length() > 0) {

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);

                    ParkingLot item = gson.fromJson(o.toString(), ParkingLot.class);
                    lst.add(item);
                }
            }
            return lst;
        } catch (Exception e) {
            Log.e("updateTruckAmount", e.getMessage());
        }
        return null;
    }

    public boolean checkTruck(int truckId, String truckNo) {

        String url = API_BASE_URL+"api/trucks/check";
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            String parm = gson.toJson(new TruckModel(truckNo, truckId));
            String data = sendPOST(url, parm);
            boolean val = gson.fromJson(data, boolean.class);
            return  val;
        } catch (Exception e) {
            Log.e("Truck Check API ", e.getMessage());
            return true;
        }

    }


    public void sendLog() {
        String url = API_BASE_URL + "api/parking";
    }
}

