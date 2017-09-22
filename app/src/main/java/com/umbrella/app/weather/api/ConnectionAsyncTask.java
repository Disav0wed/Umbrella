package com.umbrella.app.weather.api;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ibrahimserpici on 9/22/17.
 */


/**
 *  This Asynctask Connects and Fetchs the data from Weather API
 *  Connection is processed under three steps.
 *  At first connection, it receives the ZIP code and sends a response from where we take the city and state.
 *  At second connection, it receives the city and state and sends a response with the information for the current weather.
 *  At third connection, it recieves the future forecast.
 *  */
public class ConnectionAsyncTask extends AsyncTask<String, Void, String> {

    // API endpoint to be connected
    private String endpointNameString;

    // HTTP constants
    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;

    // Response status
    public static final String RESPONSE_OK = "RESPONSE_OK";
    public static final String RESPONSE_ERROR = "RESPONSE_ERROR";
    public static final String RESPONSE_TIMEOUT = "RESPONSE_TIMEOUT";

    // Listeners
    private OnResponseListener onResponseListener;

    // Listener to let know when a response has been received
    public interface OnResponseListener {
        void onResponse(String endpointName, String status, String result);
    }

    public void setOnResponseListener(OnResponseListener onResponseListener) {
        this.onResponseListener = onResponseListener;
    }

    @Override
    protected String doInBackground(String... params) {

        endpointNameString = params[0];
        String resultURLString = params[1];

        String result = null;

        try {

            // Create a URL object from the URL param
            URL url = new URL(resultURLString);

            // Create a connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            // Set methods and timeouts
            httpURLConnection.setRequestMethod(REQUEST_METHOD);
            httpURLConnection.setReadTimeout(READ_TIMEOUT);
            httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);

            // Connect to URL
            httpURLConnection.connect();

            // Create an InputStreamReader
            InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());

            // Create a BufferReader and StringBuilder
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            // Read lines
            String inputLineString;
            while ((inputLineString = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLineString);
            }

            // Close both InputStreamReader and BufferedReader
            bufferedReader.close();
            inputStreamReader.close();

            // Set the result
            result = stringBuilder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // If result is null, return ERROR status
        if (result == null) {
            result = "{\"response\": {\"error\": {\"description\": \"Connection timeout.\"}}}";

            this.onResponseListener.onResponse(endpointNameString, RESPONSE_TIMEOUT, result);

            return;
        }

        // If result contains an "error" key, return ERROR status
        try {
            JSONObject resultJSONObject = new JSONObject(result);
            JSONObject responseJSONObject = resultJSONObject.getJSONObject("response");

            if (responseJSONObject.has("error")) {

                this.onResponseListener.onResponse(endpointNameString, RESPONSE_ERROR, result);

                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Otherwise return OK status
        this.onResponseListener.onResponse(endpointNameString, RESPONSE_OK, result);
    }
}
