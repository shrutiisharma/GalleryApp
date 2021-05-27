package com.streamliners.galleryapp;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RedirectURLHelper extends AsyncTask<String, Void, String> {

    private OnCompleteListener listener;
    private String redirectedURL;

    /**
     * Constructor
     * To fetch the redirected URL.
     * @param listener : For creating asynchronous callback.
     * @return same object
     */
    public RedirectURLHelper fetchRedirectedURL(OnCompleteListener listener){
        this.listener = listener;
        return this;
    }



    /**
     * To get the redirected URL for the specified URL.
     * Override this method to perform a computation on a background thread.
     * @param strings : url as string.
     * @return redirected url.
     */
    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try{
            connection= (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e){
            e.printStackTrace();
        }
        try{
            connection.connect();
        } catch (IOException e){
            e.printStackTrace();
        }
        try {
            inputStream=connection.getInputStream();
        } catch (IOException e){
            e.printStackTrace();
        }

        redirectedURL = connection.getURL().toString();

        try{
            inputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        return redirectedURL;
    }

    /**
     * Runs on the UI thread after doInBackground Method.
     * @param s The result of the operation computed by doInBackground Method.
     */
    @Override
    protected void onPostExecute(String s) {
        listener.onFetched(redirectedURL);
    }


    /**
     * Listener for this asynchronous task.
     */
    interface OnCompleteListener {
        /**
         * when the redirected URL is fetched successfully
         * @param url the redirected url
         */
        void onFetched(String url);

        /**
         * When error occurs
         */
        void onError(String error);
    }
}