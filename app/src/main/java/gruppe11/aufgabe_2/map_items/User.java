package gruppe11.aufgabe_2.map_items;

import com.google.android.gms.maps.model.LatLng;

/**
 * Client
 * Holds client specific attributes like id and password
 */
public class User extends Localizable {

    private String id;
    private String username;
    private String password;
    private String description;

    public User(String username, String password, String description) {
        super();
        this.username = username;
        this.password = password;
        this.description = description;
    }

    public User(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public User(LatLng latLng) {
        super();
        setLatitude(latLng.latitude);
        setLongitude(latLng.longitude);
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
