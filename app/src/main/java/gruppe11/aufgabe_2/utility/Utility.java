package gruppe11.aufgabe_2.utility;

import android.content.Context;
import android.util.Log;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import de.greenrobot.event.EventBus;


/**
 * Standard Utility class holding methods for various classes
 */
public class Utility {

    /**
     * Converts Plaintext user password into SHA-256 hash
     *
     * @param password Chosen user password
     * @return SHA-256 Hash formatted in HEXADECIMAL parsed as string
     */
    public static String hash(String password) {

        byte[] passBytes = password.getBytes();

        try {

            String hashString = new String(Hex.encodeHex(DigestUtils.sha256(passBytes)));
            Log.d("DEBUGLOG-UTILITY:", "Password hashed: " + hashString);
            return hashString;

        } catch (Exception e) {
            Log.d("DEBUGLOG-UTILITY:", "Password could not be hashed\nException: " + e.getMessage());
            return null;
        }

    }

    /**
     * Unregisters and registers EventBus
     *
     * @param context
     */
    public static void refreshEventBus(Context context) {
        EventBus.getDefault().unregister(context);
        EventBus.getDefault().register(context);
    }

    /**
     * Calculates Latitude/Longitude Bounds of the Google API Circle so the camera may be
     * moved to the proper location.
     *
     * @param center         Latitude and Longitude values (LatLng) of Google API Circle Object
     * @param radiusInMeters Radius of Google API Circle Object
     * @return Latitude/Longitude Bounds consisting of two coordinates
     * - The southwest corner of the area which encloses the gmaps Circle
     * - The northeast corner of the area which encloses the gmaps Circle
     */
    public static LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }


}