package com.example.uptimeChecker.Constants;

public class RabbitmqConstants {
    public static final String EMAIL_QUEUE_NAME ="email-queue";
    public static final String SLACK_QUEUE_NAME ="slack-queue";
    public static final String EXCHANGE_NAME="exchange";
    public static final String EMAIL_ROUTING_KEY ="*.downtime.email.Notification";
    public static final String SLACK_ROUTING_KEY ="*.downtime.slack.Notification";

}
