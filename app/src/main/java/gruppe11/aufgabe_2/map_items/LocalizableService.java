package gruppe11.aufgabe_2.map_items;

import android.util.Log;

import java.util.ArrayList;

/**
 * Singelton. Holds a list of all elements displayed on the map (Client & other Users)
 */
public class LocalizableService {

    private ArrayList<Localizable> LIST_OF_LOCALIZABLES = new ArrayList<>();

    private static final LocalizableService ourInstance = new LocalizableService();

    public static LocalizableService getInstance() {
        return ourInstance;
    }

    private LocalizableService() {
    }

    /**
     * Adds Localizable to the list.
     * If Localizable is an instance of User (Client), user object that may already exists
     * in this list will be deleted and replaced with the current one.
     *
     * @param localizable user/client or communityItem
     */
    public void add(Localizable localizable) {
        if (localizable instanceof User) {
            removeClient();
        }
        LIST_OF_LOCALIZABLES.add(localizable);
    }

    /**
     * Removes Localizable from the list.
     *
     * @param index position in list
     */
    private void remove(int index) {
//        LIST_OF_LOCALIZABLES.remove(index);
        if (index - 1 < LIST_OF_LOCALIZABLES.size()) {
            LIST_OF_LOCALIZABLES.remove(index);
        }
    }

    /**
     * Removes the client(user) object from the list
     */
    private void removeClient() {
        for (Localizable localizable : LIST_OF_LOCALIZABLES) {
            if (localizable instanceof User) {
                LIST_OF_LOCALIZABLES.remove(localizable);
            }
        }
    }

    /**
     * Clears all elements in the list
     */
    public void clear() {
        LIST_OF_LOCALIZABLES.clear();
    }

    /**
     * Clears all Community Items in the List
     */
    public void clearCommunityItems() {
        for (int i = 0; i < LIST_OF_LOCALIZABLES.size(); i++) {
            if (getLocalizable(i) instanceof CommunityItem) {
                remove(i);
            }
        }
    }


    /**
     * Returns the client Localizables
     *
     * @return client/user object contained in list
     */
    public User getClientLocalizable() {
        for (Localizable localizable : LIST_OF_LOCALIZABLES) {
            if (localizable instanceof User) {
                return (User) localizable;
            }
        }
        return null;
    }


    /**
     * Returns any localizable from the list
     *
     * @param index Position in the list
     * @return Localizable Object (Client and Community Items)
     */
    public Localizable getLocalizable(int index) {
        if (index - 1 < LIST_OF_LOCALIZABLES.size()) {
            return LIST_OF_LOCALIZABLES.get(index);
        } else {
            return null;
        }
    }

    public int getSize() {
        return LIST_OF_LOCALIZABLES.size();
    }


}
