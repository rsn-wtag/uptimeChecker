package com.example.uptimeChecker.Constants;

public class RestEndpoints {
   public static final String WEBSITE_LIST="/websites";//get
   public static final String REGISTER_WEBSITE="/websites";// post
   public static final String UPDATE_WEBSITE="/websites/{webId}";//websites/{websiteId} patch
   public static final String REMOVE_WEBSITE="/websites/{webId}";//websites/{websiteId} delete
   public static final String WEBSITE_INFO="/websites/{webId}";//websites/{websiteId} get

   public static final String WEBSITE_DOWNTIME_HISTORY="/websites/{webId}/down-time-summary";///websites/{websiteId}/down-time-summary get
   public static final String WEBSITE_DOWNTIME_HISTORY_TODAY="/websites/{webId}/today-down-time-histories";///websites/{websiteId}/today-downtime-histories get

   public static final String UPDATE_USER="/users/{userId}";//users/{userId} patch

}
