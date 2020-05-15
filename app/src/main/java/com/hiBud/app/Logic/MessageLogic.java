package com.hiBud.app.Logic;

import com.hiBud.app.Firebase.Message;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.Locale;

public class MessageLogic {

    private String key;
    private Message message;
    private UserLogic userLogic;

    public MessageLogic(String key, Message message) {
        this.key = key;
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Message getMessage() {
        return message;
    }

    public long getCreatedTimestampLong() {
        return (long) message.getCreatedTimestamp();
    }

    public UserLogic getUserLogic() {
        return userLogic;
    }

    public void setUserLogic(UserLogic userLogic) {
        this.userLogic = userLogic;
    }

    public String messageCreationDate() {
        Date date = new Date(getCreatedTimestampLong());
        PrettyTime prettyTime = new PrettyTime(new Date(), Locale.getDefault());
        return prettyTime.format(date);
    }

}

