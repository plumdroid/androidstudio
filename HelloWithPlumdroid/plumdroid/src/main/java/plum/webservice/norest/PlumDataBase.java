package plum.webservice.norest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PlumDataBase {

    protected final String url;

    private static final int HTTP_REQUEST_RESPONSE = 1;
    private static final String KEY_RESPONSE_WEBSERVICE = "KEY_RESPONSE_WEBSERVICE";
    private static final String KEY_EXCEPTION = "KEY_EXCEPTION";
    private static final String KEY_WEBSERVICE = "KEY_WEBSERVICE";
    private static final String KEY_COOKIE = "Set-Cookie";

    public static final int WEBSERVICE_CONTACT = 1;
    public static final int WEBSERVICE_EXECUTE = 2;
    public static final int WEBSERVICE_QUERY = 3;
    public static final int WEBSERVICE_AUTHENTICATION = 4;

    public PlumDataBase(String url) {

        this.url = new String(url);
    }

    /*
     * Contacter le webservice
     *
     * @return
     */
    public void contact(PlumDataBase.OnReponseListener onReponse,
                        PlumDataBase.OnExceptionListener OnExceptionListener) {

        HashMap<String, String> params = new HashMap<>();

        String http = url + "contact/hello/";//url+"contact/hello/";//"http://www.fnac.com/";//

        new HttpWebService(url, http, params, WEBSERVICE_CONTACT, onReponse, OnExceptionListener);

        return;
    }

    /*
     * Authentification utilisateur
     *
     * @return
     */
    public void authentification(String user, String password, PlumDataBase.OnReponseListener onReponse, PlumDataBase.OnExceptionListener OnExceptionListener) {

        HashMap<String, String> params = new HashMap();
        params.put("user", user);
        params.put("password", password);

        String http = url + "authentification/connecter/";

        new HttpWebService(url, http, params, WEBSERVICE_AUTHENTICATION, onReponse, OnExceptionListener);


        return;
    }

    /**
     * Execute SQL
     *
     * @param sql Une requête sql
     * @return objet PlumDataBaseReponse
     */
    public void execute(String sql, PlumDataBase.OnReponseListener onReponse, PlumDataBase.OnExceptionListener OnExceptionListener) {
        HashMap<String, String> params = new HashMap();
        params.put("requete", sql);


        String http = url + "webservice/execute/";
        new HttpWebService(this.url, http, params, WEBSERVICE_EXECUTE, onReponse, OnExceptionListener);

        return;

    }

    /**
     * Execute SQL
     *
     * @param sql  Une requête sql avec jeton '?' ; par exemple "insert into table VALUES(?,?)"
     * @param data un tableau avec les données remplaçant chaque jeton
     * @return objet PlumDataBaseReponse
     */

    public void execute(String sql, String[] data, PlumDataBase.OnReponseListener onReponse,
                        PlumDataBase.OnExceptionListener OnExceptionListener) {
        HashMap<String, String> params = new HashMap();
        params.put("requete", sql);

        int i = 0;
        for (String unedata : data) {
            String key = "data[" + i + "]"; //data[0]....
            params.put(key, unedata);
            i++;
        }

        String http = url + "webservice/execute/";
        new HttpWebService(url, http, params, WEBSERVICE_EXECUTE, onReponse, OnExceptionListener);

        return;

    }


    /**
     * Query SQL on retourne également la liste des données lues
     *
     * @param sql Une requête sql
     * @return objet PlumDataBaseReponse
     */
    public void query(String sql, PlumDataBase.OnReponseListener onReponseListener,
                      PlumDataBase.OnExceptionListener onExceptionListener) {
        HashMap<String, String> params = new HashMap();

        params.put("requete", sql);

        String http = url + "webservice/query/";

        new HttpWebService(url, http, params, WEBSERVICE_QUERY, onReponseListener, onExceptionListener);

        return;
    }

    /**
     * Query SQL on retourne également la liste des données lues
     *
     * @param sql  Une requête sql avec jeton '?' ; par exemple "select * from table where id=?"
     * @param data un tableau avec les données remplaçant chaque jeton
     * @return objet PlumDataBaseReponse
     */
    public void query(String sql, String[] data, PlumDataBase.OnReponseListener onReponseListener,
                      PlumDataBase.OnExceptionListener onExceptionListener) {
        HashMap<String, String> params = new HashMap();
        params.put("requete", sql);

        int i = 0;
        for (String unedata : data) {
            String key = "data[" + i + "]"; //data[0]....
            params.put(key, unedata);
            i++;
        }

        String http = url + "webservice/query/";
        new HttpWebService(url, http, params, WEBSERVICE_QUERY, onReponseListener, onExceptionListener);

        return;
    }

    // ----------------------------------------------------------------------------------------
    /// ----- interface permettant de traiter la réponse d'une action de PlumDataBase  ////
    public static interface OnReponseListener {
        public abstract void onReponseSucceed(PlumDataBaseReponse reponse);

        public abstract void onReponseError(PlumDataBaseReponse reponse);
    }

    public static interface OnExceptionListener {
        public abstract void onException(PlumDataBaseException e);
    }

    // ----------------------------------------------------------------------------------------
    /*
     * Acces HTTP
     *
     * return JSONObject
     */

    public class HttpWebService {
        // This handler used to listen to child thread show return page html text message and display those text in responseTextView.
        public Handler uiUpdater = null;

        @SuppressLint("HandlerLeak")
        public HttpWebService(final String uri,
                              final String http_webservice,
                              final HashMap params,
                              final int webService,
                              final PlumDataBase.OnReponseListener onReponseListener,
                              final PlumDataBase.OnExceptionListener onExceptionListener) {

            // This handler is used to wait for child thread message to update server response

            //Manager les cookies
            if (CookieHandler.getDefault() == null) {
                CookieManager cookieManager = new CookieManager();
                CookieHandler.setDefault(cookieManager);
            }
            CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
            //List lcook = cookieManager.getCookieStore().getCookies();
            //List luri = cookieManager.getCookieStore().getURIs();

            //Handler uiUpdater = null;
            uiUpdater = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what != HTTP_REQUEST_RESPONSE) return;

                    Bundle bundle = msg.getData();
                    if (bundle == null) return;

                    String exception = bundle.getString(KEY_EXCEPTION);
                    if (exception != null) {
                        onExceptionListener.onException(new PlumDataBaseException(exception, "", ""));
                        return;
                    }

                    String response = bundle.getString(KEY_RESPONSE_WEBSERVICE);

                    PlumDataBaseReponse d = null;
                    try {
                        d = new PlumDataBaseReponse(response);
                    } catch (PlumDataBaseException e) {
                        onExceptionListener.onException(new PlumDataBaseException(e.toString(), http_webservice, response));
                        return;
                    }


                    // mémorisation de secure_token sur Authentification
                    //le cookie secure_token est à usage local (le serveur ne gère pas ce cookie)
                    if (webService == WEBSERVICE_AUTHENTICATION & d.etat == 0) {
                        HttpCookie cookie = new HttpCookie("secure_token", d.secure_token);
                        try {
                            CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
                            cookieManager.getCookieStore().add(new URI(uri), cookie);
                        } catch (URISyntaxException e) {
                            onExceptionListener.onException(new PlumDataBaseException(e.toString(), http_webservice, response));
                            return;
                        }
                    }

                    //etat==0 => réponse authentifiée
                    //etat ==100  client non authentifié
                    if (d.etat != 0) {
                        onReponseListener.onReponseError(d);
                    } else {
                        onReponseListener.onReponseSucceed(d);
                    }
                }
            };

            //Thread accédant au webservice : rend la main à uiUpdater
            final Thread sendHttpRequestThread = new Thread() {
                @Override
                public void run() {

                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_WEBSERVICE, webService);

                    String exception = null;

                    String paramPost = "";
                    if (params != null && params.size() > 0) {
                        String et = "";

                        Set<String> keys = params.keySet();
                        for (String key : keys) {
                            paramPost += et + key + "=" + params.get(key);
                            et = "&";
                        }
                    }

                    //clé secure_token
                    CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
                    List<HttpCookie> lcook = cookieManager.getCookieStore().getCookies();
                    String secure_token = "";
                    for (HttpCookie cookie : lcook) {
                        if (cookie.getName().equals("secure_token")) {
                            secure_token = cookie.getValue();
                            paramPost += "&secure_token=" + secure_token;
                            break;
                        }
                    }

                    String line = "";

                    HttpURLConnection http = null;
                    InputStreamReader isReader = null;

                    String urlString = http_webservice;
                    try {
                        URL url = new URL(http_webservice);

                        http = (HttpURLConnection) url.openConnection();

                        http.setConnectTimeout(10000);
                        http.setRequestMethod("POST");
                        http.setDoInput(true);
                        http.setDoOutput(true);

                        // POST à envoyer ?
                        if (!paramPost.equals("")) {
                            http.setFixedLengthStreamingMode(paramPost.getBytes().length);

                            PrintWriter out = new PrintWriter(http.getOutputStream());
                            out.print(paramPost);
                            out.close();
                        }
                        //http.setRequestProperty("Content-Type",
                        //		"application/x-www-form-urlencoded");

                        InputStream in;
                        in = new BufferedInputStream(http.getInputStream());

                        InputStreamReader inr = new InputStreamReader(in, "UTF-8");
                        BufferedReader reader = new BufferedReader(inr);
                        String inputLine;
                        while ((inputLine = reader.readLine()) != null) {
                            line += inputLine;
                        }

                    } catch (
                            MalformedURLException e) {
                        exception = PlumDataBaseException.toStringException("Error in http connection URL malformed :" + e.toString(),
                                http_webservice, "");
                    } catch (
                            SocketTimeoutException e) {
                        exception = PlumDataBaseException.toStringException("Error in http connection timeout :" + e.toString(), http_webservice, "");
                    } catch (
                            IOException e) {
                        exception = PlumDataBaseException.toStringException("Error in http connection IO :" + e.toString(), http_webservice, "");
                    } finally {
                        // http.disconnect();
                    }

                    String headerName = "";
                    String cookie = "";
                    String header = "";
					/*for (int i = 1; (headerName = http.getHeaderFieldKey(i)) != null; i++)
					{
						header+=http.getHeaderFieldKey(i)+"="+http.getHeaderField(i)+",";
						if(headerName.equals(KEY_COOKIE))
						{
							cookie =http.getHeaderField(i);
						}

					}
					Log.i("cookies",cookie);
					bundle.putString(KEY_COOKIE, cookie);*/


                    // Send message to main thread to update response text in TextView after read all.
                    Message message = new Message();

                    // Set message type.
                    message.what = HTTP_REQUEST_RESPONSE;

                    // Put response text in the bundle with the special key.
                    bundle.putString(KEY_RESPONSE_WEBSERVICE, line);
                    // Set bundle data in message.
                    bundle.putString(KEY_EXCEPTION, exception);

                    message.setData(bundle);


                    // Send message to main thread Handler to process.
                    uiUpdater.sendMessage(message);
                }
            };

            //Lancer le Thread défini au dessus
			/*Log.i("handle_uiUpater",uiUpdater.toString());
			Log.i("handle_HttpWebservice",this.toString());
			Log.i("handle_onReponse",onReponse.toString());*/
            sendHttpRequestThread.start();

        }

    }

}