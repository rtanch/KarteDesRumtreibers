package gruppe11.aufgabe_2.rest;


import gruppe11.aufgabe_2.map_items.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * RetrofitAPI for Hochschule Heilbronn REST-Webservice (SLAB Augabe 2)
 */
public interface RetrofitAPI {

    @POST("user/new")
    Call<ResponseBody> createUser(@Body User user);

    @POST("user/login")
    Call<User> loginUser(@Body User user);

    @POST("user/update/{bid}")
    Call<ResponseBody> updateUser(@Body User user, @Path("bid") String bid, @Header("Authorization") String jwt);

    @POST("location/{bid}")
    Call<ResponseBody> updateGPS(@Body User user, @Path("bid") String bid, @Header("Authorization") String jwt);

    @GET("location/{bid}/{radius}/{lat}/{long}")
    Call<ResponseBody> getPeopleInRange(@Path("bid") String bid, @Path("radius") double radius, @Path("lat") double latitude, @Path("long") double longitude, @Header("Authorization") String jwt);

}