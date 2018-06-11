package gruppe11.aufgabe_2.rest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;

import de.greenrobot.event.EventBus;
import gruppe11.aufgabe_2.map_items.CommunityItem;
import gruppe11.aufgabe_2.map_items.LocalizableService;
import gruppe11.aufgabe_2.map_items.User;
import gruppe11.aufgabe_2.utility.Utility;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Handles all connections between Client and Rest-Webservice
 */
public class RestService extends Service implements Serializable {

    private final IBinder ibinder = new LocalService();

    // Rest Service
    private static RetrofitAPI SERVICE = null;
    private final static String BASE_URL = "https://swlab.iap.hs-heilbronn.de/ex2/api/v0.1/";
    private static boolean LOGGED_IN = false;
    private static String JWT_TOKEN = null;


    // JSON Node names
    private static final String TAG_NAME = "name";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_LOCATIONS = "locations";
    private static final String TAG_LONGITUDE = "latitude";
    private static final String TAG_LATITUDE = "longitude";

    @Override
    public void onCreate() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return ibinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    //inner class
    public class LocalService extends Binder implements Serializable {

        public RestService getService() {
            return RestService.this;
        }
    }


    /**
     * Initializes Retrofit using BASE_URL. Automatically converts transmitted Object to JSON
     * using GsonConverterFactory
     */
    public void initRestService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SERVICE = retrofit.create(RetrofitAPI.class);
    }


    /**
     * Registers new Client/User with Rest-Webservice
     *
     * @param username    name which will later be identifying the gmaps marker
     * @param password    sha256 hashed hexadecimal password
     * @param description client (account) description meant to provide more information about the client
     */
    public void registerClient(final String username, final String password, String description) {
        Log.d("DEBUGLOG-RS", "registerClient() called");
        if (SERVICE == null) {
            Log.d("DEBUGLOG-RS", "No custom URL set. Using BASE_URL");
            initRestService();
        }

        final String passwordHash = Utility.hash(password).toString();
        User user = new User(username, passwordHash, description);

        Call<ResponseBody> call = SERVICE.createUser(user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 200 HTTP OK (Client registered)");
                    if (LocalizableService.getInstance().getClientLocalizable() != null) {
                        LocalizableService.getInstance().getClientLocalizable().setUsername(username);
                        LocalizableService.getInstance().getClientLocalizable().setPassword(password);
                    } else {
                        LocalizableService.getInstance().add(new User(username, password));
                    }
                } else if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 409 HTTP CONFLICT (Username taken)");
                } else {
                    Log.d("DEBUGLOG-RS", "HTTP-FAILURE: \n--Call: " + call + "\n--Response: " + response);
                }
                EventBus.getDefault().post(new RestEvent(Event.REGISTER, response.code()));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Log.d("DEBUGLOG-RS", "RETROFIT-FAILURE in RegisterClient: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
                EventBus.getDefault().post(new RestEvent(Event.FAILURE));
            }
        });

    }


    /**
     * Login user/client
     * Receives ID and JWT-Token form Rest-Webservice which will be used to authenticate
     * the client in future transactions with the server
     *
     * @param username name which will later be identifying the gmaps marker
     * @param password sha256 hashed hexadecimal password
     */
    public void loginClient(String username, String password) {
        Log.d("DEBUGLOG-RS", "loginClient() called");
        if (SERVICE == null) {
            Log.d("DEBUGLOG-RS", "No custom URL set. Using BASE_URL");
            initRestService();
        }

        String passwordHash = Utility.hash(password).toString();
        final User user = new User(username, passwordHash);

        Call<User> call = SERVICE.loginUser(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 200 HTTP OK (Client logged in)");
                    LOGGED_IN = true;
                    Headers headers = response.headers();
                    JWT_TOKEN = headers.get("Authorization");
                    if (LocalizableService.getInstance().getClientLocalizable() != null) {
                        LocalizableService.getInstance().getClientLocalizable().setUsername(user.getUsername());
                        LocalizableService.getInstance().getClientLocalizable().setPassword(user.getPassword());
                    } else {
                        LocalizableService.getInstance().add(user);
                    }
                    LocalizableService.getInstance().getClientLocalizable().setId(response.body().getId());
                    LocalizableService.getInstance().getClientLocalizable().setDescription(response.body().getDescription());
                } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                    LOGGED_IN = false;
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 403 HTTP FORBIDDEN (Login failed)" +
                            "\n--Call: " + call + "\n--Response: " + response);
                } else {
                    LOGGED_IN = false;
                    Log.d("DEBUGLOG-RS", "HTTP-FAILURE LOGIN: \n--Call: " + call + "\n--Response: " + response);
                }
                EventBus.getDefault().post(new RestEvent(Event.LOGIN, response.code()));
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {
                LOGGED_IN = false;
                EventBus.getDefault().post(new RestEvent(Event.FAILURE));
                Log.d("DEBUGLOG-RS", "RETROFIT-FAILURE in LoginClient: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
            }
        });
    }


    /**
     * Log out client
     * - deletes local CommunityItem / Client (=User) data
     * - sets LOGGED_IN flag\
     * - triggers logout event
     */
    public void logoutClient() {
        LocalizableService.getInstance().clear();
        LOGGED_IN = false;
        EventBus.getDefault().post(new RestEvent(Event.LOGOUT));
    }


    /**
     * Sends current username, password and description to Rest-Webservice
     */
    public void updateClientData() {
        Log.d("DEBUGLOG-RS", "updateClientData() called");

        Call<ResponseBody> call = SERVICE.updateUser(LocalizableService.getInstance().getClientLocalizable(), LocalizableService.getInstance().getClientLocalizable().getId(), JWT_TOKEN);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 200 HTTP OK(Client data changed)");
                } else if (response.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 304 NOT MODIFIED (Client data wasn't changed / config wasn't changed)");
                } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 403 HTTP FORBIDDEN (Invalid JWT Token)");
                } else {
                    Log.d("DEBUGLOG-RS", "HTTP-FAILURE UPDATE_CLIENT_DATA: \n--Call: " + call + "\n--Response: " + response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                EventBus.getDefault().post(new RestEvent(Event.FAILURE));
                Log.d("DEBUGLOG-RS", "RETROFIT-FAILURE in UpdateClientData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
            }
        });
    }


    /**
     * Sends current client position to Rest-Webservice
     */
    public void updateClientGPSData() {
        Log.d("DEBUGLOG-RS", "updateClientGPSData() called");
        Call<ResponseBody> call = SERVICE.updateGPS(LocalizableService.getInstance().getClientLocalizable(), LocalizableService.getInstance().getClientLocalizable().getId(), JWT_TOKEN);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 200 HTTP OK(GPS data updated)");
                } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 403 HTTP FORBIDDEN (Invalid JWT Token)");
                } else {
                    Log.d("DEBUGLOG-RS", "HTTP-FAILURE UPDATE_CLIENT_GPS_DATA: \n--Call: " + call + "\n--Response: " + response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                EventBus.getDefault().post(new RestEvent(Event.FAILURE));
                Log.d("DEBUGLOG-RS", "RETROFIT-FAILURE in UpdateClientGPSData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
            }
        });
    }


    /**
     * Fetches Community Items (all other users or places) as JSON Objects from Rest-Webservice
     *
     * @param radius KM Radius around the client's position in which Community Items should be displayed
     */
    public void getCommunityData(double radius) {
        Log.d("DEBUGLOG-RS", "getCommunityData() called");


        Log.d("DEBUGLOG-RS", "Radius value RS: " + radius);

        Call<ResponseBody> call = SERVICE.getPeopleInRange(LocalizableService.getInstance().getClientLocalizable().getId(), radius, LocalizableService.getInstance().getClientLocalizable().getLatitude(), LocalizableService.getInstance().getClientLocalizable().getLongitude(), JWT_TOKEN);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 200 HTTP OK(Receiving Community Item Data)");
                    try {
                        String res = response.body().string();
                        JSONArray peopleInRange = new JSONArray(res);
                        for (int i = 0; i < peopleInRange.length(); i++) {
                            JSONArray locations = (JSONArray) peopleInRange.getJSONObject(i).get(TAG_LOCATIONS);
                            Log.d("DEBUGLOG-RS", "People in range - TAG_NAME: " + peopleInRange.getJSONObject(i).get(TAG_NAME));
                            Log.d("DEBUGLOG-RS", "People in range - TAG_LATITUDE: " + locations.getJSONObject(0).get(TAG_LATITUDE));
                            Log.d("DEBUGLOG-RS", "People in range - TAG_LONGITUDE: " + locations.getJSONObject(0).get(TAG_LONGITUDE));
                            if (LocalizableService.getInstance().getClientLocalizable().getUsername() != null) {
                                if (!LocalizableService.getInstance().getClientLocalizable().getUsername().equals(peopleInRange.getJSONObject(i).get(TAG_NAME).toString())) {
                                    CommunityItem personInRange = new CommunityItem(peopleInRange.getJSONObject(i).get(TAG_NAME).toString(), peopleInRange.getJSONObject(i).get(TAG_DESCRIPTION).toString(), Double.valueOf(locations.getJSONObject(0).get(TAG_LATITUDE).toString()), Double.valueOf(locations.getJSONObject(0).get(TAG_LONGITUDE).toString()));
                                    LocalizableService.getInstance().add(personInRange);
                                }
                            }
                        }

                    } catch (IOException e) {
                        Log.d("DEBUGLOG-RS", "JSON RESPONSE BODY IO-Exception: " + e.getMessage());
                    } catch (JSONException e) {
                        Log.d("DEBUGLOG-RS", "JSON RESPONSE BODY JSON-Exception: " + e + " --- " + e.getMessage());
                    }

                } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                    Log.d("DEBUGLOG-RS", "RESPONSE-CODE: 403 HTTP FORBIDDEN (Invalid JWT Token)");
                } else {
                    Log.d("DEBUGLOG-RS", "HTTP-FAILURE: \n--Call: " + call + "\n--Response: " + response);
                }
                EventBus.getDefault().post(new RestEvent(Event.COMMUNITY, response.code()));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                EventBus.getDefault().post(new RestEvent(Event.FAILURE));
                Log.d("DEBUGLOG-RS", "RETROFIT-FAILURE in getCommunityData: \n--Call: " + call + "\n--Response: " + throwable.getMessage());
            }
        });
    }

    public boolean isLoggedIn() {
        return LOGGED_IN;
    }

}
