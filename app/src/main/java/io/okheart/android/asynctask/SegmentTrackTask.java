package io.okheart.android.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import io.okheart.android.callback.SegmentTrackCallBack;
import io.okheart.android.utilities.BasicAuthInterceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static io.okheart.android.utilities.Constants.ANALYTICS_WRITE_KEY_DEV_OMTM;
import static io.okheart.android.utilities.Constants.ANALYTICS_WRITE_KEY_PROD_OMTM;


/**
 * Created by ramogiochola on 6/21/16.
 */
public class SegmentTrackTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "SegmentTrackTask";
    private SegmentTrackCallBack segmentTrackCallBack;
    private JSONObject jsonObject;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private int responseCode;
    private Boolean production;


    public SegmentTrackTask(SegmentTrackCallBack segmentTrackCallBack, JSONObject jsonObject, Boolean production) {
        displayLog("SegmentIdentifyTask called");

        this.segmentTrackCallBack = segmentTrackCallBack;
        this.jsonObject = jsonObject;
        this.production = production;
    }

    @Override
    protected String doInBackground(Void... params) {
        String results = "";

        try {
            String urlString = "https://api.segment.io/v1/track";
            String writekey;
            if (production) {
                writekey = ANALYTICS_WRITE_KEY_PROD_OMTM;
            } else {
                writekey = ANALYTICS_WRITE_KEY_DEV_OMTM;
            }

            OkHttpClient.Builder b = new OkHttpClient.Builder();
            b.addInterceptor(new BasicAuthInterceptor(writekey, null));
            b.connectTimeout(15, TimeUnit.SECONDS);
            b.writeTimeout(15, TimeUnit.SECONDS);
            b.readTimeout(15, TimeUnit.SECONDS);
            OkHttpClient client = b.build();

            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

            Request request = new Request.Builder()
                    .url(urlString)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            results = responseBody.string();
            responseCode = response.code();
        } catch (UnsupportedEncodingException e) {
            displayLog("unsupported encoding exception " + e.toString());
        } catch (IOException io) {
            displayLog("io exception " + io.toString());
        } catch (IllegalArgumentException iae) {
            displayLog("illegal argument exception " + iae.toString());
        }
        return results;
    }

    @Override
    protected void onPostExecute(String result) {
        if ((200 <= responseCode) && (responseCode < 300)) {

            segmentTrackCallBack.querycomplete(result, true);
        } else {

            segmentTrackCallBack.querycomplete(result, false);

        }
    }

    private void displayLog(String me) {
        Log.i(TAG, "% "+me);
    }
}
