package org.hotelbyte.app.service;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static org.hotelbyte.app.settings.Constants.EXPLORER_URL;


public class NetworkApiExplorerService {
    private static NetworkApiExplorerService instance;

    private Context context;
    private RequestQueue queue;

    public static NetworkApiExplorerService getInstance(Context context) {
        if (instance == null)
            instance = new NetworkApiExplorerService(context);
        return instance;
    }

    private NetworkApiExplorerService(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(context.getApplicationContext());
    }


    public void callTransactions(String address, Response.Listener<Map<String, Boolean>> listener) {
        callTransactions(address, listener, (e) -> e.printStackTrace());
    }

    public void callTransactions(String address, Response.Listener<Map<String, Boolean>> listener,
                                 Response.ErrorListener errorListener) {
        try {
            String url = EXPLORER_URL + "/accounts/" + address;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, (response -> {
                try {
                    Map<String, Boolean> transactionMap = getTransactionMap(response);
                    listener.onResponse(transactionMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }), errorListener) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };
            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Boolean> getTransactionMap(JSONObject response) throws JSONException {
        Map<String, Boolean> transactionMap = new HashMap<>();
        if (response.has("transactions")) {
            JSONArray transactions = response.getJSONArray("transactions");
            for (int i = 0; i < transactions.length(); i++) {
                JSONObject transaction = transactions.getJSONObject(i);
                if (transaction.has("hash") && transaction.has("sender")) {
                    transactionMap.put(transaction.getString("hash"), transaction.getBoolean("sender"));
                }
            }
        }
        return transactionMap;
    }
}
