package model;

public class ActivityLog {

    private final int logId;
    private final int userId;
    private final String actionType;
    private final String description;
    private final String timestamp;

    public ActivityLog(
            int logId,
            int userId,
            String actionType,
            String description,
            String timestamp) {

        this.logId = logId;
        this.userId = userId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = timestamp;
    }

    public int getLogId() {
        return logId;
    }

    public int getUserId() {
        return userId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }
}