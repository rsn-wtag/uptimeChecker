package com.example.uptimeChecker.constants;

public class RestEndpoints {
   public static final String USER_WEBSITE_LIST="/website-management/users/{userId}/websites";
   public static final String REGISTER_WEBSITE="/website-registration/register-website";
   public static final String UPDATE_WEBSITE="/website-management/update-website";
   public static final String REMOVE_WEBSITE="/website-management/users/{userId}/remove-website/{webId}";
   public static final String USER_WEBSITE_INFO="/website-management/users/{userId}/websites/{webId}";

   public static final String WEBSITE_DOWNTIME_HISTORY="/website-management/websites/{webId}/down-time-history";
   public static final String WEBSITE_DOWNTIME_HISTORY_TODAY="/website-management/websites/{webId}/down-time-history-today";

}
