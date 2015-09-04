/*
 * Copyright (c) 2010 - 2015 Ushahidi Inc
 * All rights reserved
 * Contact: team@ushahidi.com
 * Website: http://www.ushahidi.com
 * GNU Lesser General Public License Usage
 * This file may be used under the terms of the GNU Lesser
 * General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.LGPL included in the
 * packaging of this file. Please review the following information to
 * ensure the GNU Lesser General Public License version 3 requirements
 * will be met: http://www.gnu.org/licenses/lgpl.html.
 *
 * If you have questions regarding the use of this file, please contact
 * Ushahidi developers at team@ushahidi.com.
 */

package org.addhen.smssync.presentation.util;

import org.addhen.smssync.R;
import org.addhen.smssync.data.PrefsFactory;
import org.addhen.smssync.presentation.receivers.ConnectivityChangedReceiver;
import org.addhen.smssync.presentation.view.ui.activity.MainActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ushahidi Team <team@ushahidi.com>
 */
public class Utility {

    private static final String URL_PATTERN
            = "\\b(https?|ftp|file)://[-a-zA-Z0-9+\\$&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    private static Pattern pattern;

    private static Matcher matcher;

    private static final int NOTIFY_RUNNING = 100;

    public static String formatDate(Date messageDate) {
        DateFormat formatter = new SimpleDateFormat("hh:mm a");
        return formatter.format(messageDate);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * This method removes all whitespaces from passed string
     *
     * @param s String to be trimmed
     * @return String without whitespaces
     */
    public static String removeWhitespaces(String s) {
        if (TextUtils.isEmpty(s)) {
            return "";
        }
        String withoutWhiteChars = s.replaceAll("\\s+", "");
        return withoutWhiteChars;
    }

    /**
     * Validate the callback URL
     *
     * @param url - The callback URL to be validated.
     * @return boolean True when URL is valid False otherwise
     */
    public static boolean validateUrl(String url) {

        if (TextUtils.isEmpty(url)) {
            return false;
        }

        pattern = Pattern.compile(URL_PATTERN);
        matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if there is Internet connection or data connection on the device.
     *
     * @param context - The activity calling this method.
     * @return boolean
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;

    }

    /**
     * Show notification
     */
    public static void showNotification(Context context) {

        Intent baseIntent = new Intent(context, MainActivity.class);
        baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                baseIntent, 0);

        buildNotification(context, R.drawable.ic_stat_notfiy,
                context.getString(R.string.notification_summary),
                context.getString(R.string.app_name), pendingIntent, true);

    }

    /**
     * Show a notification
     *
     * @param message           to display
     * @param notificationTitle notification title
     */
    public static void showFailNotification(Context context, String message,
            String notificationTitle) {

        Intent baseIntent = new Intent(context, MainActivity.class);
        baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                baseIntent, 0);

        buildNotification(context, R.drawable.ic_stat_notfiy, message, notificationTitle,
                pendingIntent, false);

    }

    /**
     * Build notification info
     *
     * @param context  The calling activity
     * @param drawable The notification icon
     * @param message  The message
     * @param title    The title for the notification
     * @param intent   The pending intent
     * @param ongoing  True if you don't want the user to clear the notification
     */
    private static void buildNotification(Context context, int drawable,
            String message, String title, PendingIntent intent, boolean ongoing) {

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSmallIcon(drawable);
        builder.setContentIntent(intent);

        if (ongoing) {
            builder.setOngoing(ongoing);
        }

        notificationManager.notify(NOTIFY_RUNNING, builder.build());
    }

    /**
     * Clear all notifications shown to the user.
     *
     * @param context - The context of the calling activity.
     * @return void.
     */
    public static void clearAll(Context context) {
        NotificationManager myNM = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        myNM.cancelAll();
    }

    /**
     * Clear a running notification.
     *
     * @param context - The context of the calling activity.
     * @return void
     */
    public static void clearNotify(Context context) {
        NotificationManager myNM = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        myNM.cancel(NOTIFY_RUNNING);
    }

    /**
     * Makes an attempt to connect to a data network.
     */
    public static void connectToDataNetwork(@NonNull Context context) {
        // Enable the Connectivity Changed Receiver to listen for
        // connection to a network so we can send pending messages.
        PackageManager pm = context.getPackageManager();
        ComponentName connectivityReceiver = new ComponentName(context,
                ConnectivityChangedReceiver.class);
        pm.setComponentEnabledSetting(connectivityReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static String getPhoneNumber(@NonNull Context context, @NonNull PrefsFactory prefs) {

        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        String number = mTelephonyMgr.getLine1Number();
        if (number != null) {
            return number;
        }
        return prefs.uniqueId().get();

    }

    /**
     * Capitalize any String given to it.
     *
     * @param text - The string to be capitalized.
     * @return String
     */
    public static String capitalizeFirstLetter(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

}
