package gruppe11.aufgabe_2.rest;


/**
 * POJO class for EventBus
 * - returns REST response codes and event enum as an identifier
 */
public class RestEvent {

    private Event event;
    private int responseCode;

    public RestEvent(Event event, int responseCode) {
        this.event = event;
        this.responseCode = responseCode;
    }

    public RestEvent(Event event) {
        this.event = event;
    }


    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
