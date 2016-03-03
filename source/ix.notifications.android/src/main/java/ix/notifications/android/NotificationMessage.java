package ix.notifications.android;

import java.util.ArrayList;

public class NotificationMessage {
    public String Timestamp;
    public String Version;
    public String Subject;
    public String Body;
    public ArrayList<NotificationMessageLink> Links;
    public ArrayList<NotificationMetadataValue> Metadata;
    public NotificationMessage() {
        Links = new ArrayList<NotificationMessageLink>();
        Metadata = new ArrayList<NotificationMetadataValue>();
    }
}
