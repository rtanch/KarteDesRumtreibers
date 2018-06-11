package gruppe11.aufgabe_2.map_items;

/**
 * All users except this client that are in range of a predefined radius.
 */
public class CommunityItem extends Localizable {

    private String name;
    private String description;

    public CommunityItem(String name, String description, double latitude, double longitude) {
        super(latitude, longitude);
        this.name = name;
        this.description = description;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
