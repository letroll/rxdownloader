package fr.letroll.rxdownloader.events;

public class ClickEvent {
    public enum DownloadEventType { start,pause,resume,reset }
    private DownloadEventType eventType;

    public ClickEvent(DownloadEventType event) {
        eventType = event;
    }

    public DownloadEventType getEventType() { return eventType; }
}
