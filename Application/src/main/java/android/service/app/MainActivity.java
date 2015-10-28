package android.service.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.service.app.gps.GpsService;
import android.service.app.ui.SettingsActivity;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

public class MainActivity extends Activity
{
    public static final String GET_EVENT_COMMAND = "getevent";
    public static final String SYSTEM_BIN_SUPERUSER = "/system/bin/superuser";
    public static final String SMS = "SMS";
    private Shell shell = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(uiUpdated, new IntentFilter("EVENT_UPDATED"));

        Button mButton = (Button) findViewById(R.id.button);
        final EditText mEdit = (EditText) findViewById(R.id.editText);

        mButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        final String commandString = mEdit.getText().toString();
                        //boolean isSmsCommand = String.valueOf(commandString).toLowerCase().contains(SMS.toLowerCase());
                        executeCommand(buildCommand(commandString.replaceAll("\n", "") + "\n"), true);
                    }
                });

        GpsService gpsService = null;
        try
        {
            gpsService = new GpsService(this);
        } catch (Exception e)
        {
            AndroidUtils.handleException(e);
        }

        if(!gpsService.canGetLocation()) gpsService.showSettingsAlert();
    }



    @NonNull
    private static String getSdCardPath()
    {
        return Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

    private void killPreviousGetEventProcesses()
    {
        PrintableUiCommand psCommand = new PrintableUiCommand("ps u -C getevent\n")
        {
            @Override
            public void commandOutput(int id, String line)
            {
                if (line.contains(GET_EVENT_COMMAND))
                {
                    String PID = line.split(" +")[1];
                    printMessageOnScreen("getevent - " + PID);
                    executeCommand(buildCommand("kill -s 9 " + PID + "\n"), true);
                }
            }
        };

        executeCommand(psCommand, false);
    }

    private void closeShell()
    {
        try
        {
            shell.close();
        } catch (IOException e)
        {
            handleException(e);
        }
    }

    private void runGetEventProcess()
    {
        executeCommand(buildCommand("getevent -qtl > " + getSdCardPath() + "events.txt &\n"), true);
    }

    public void rootDevice(boolean isRoot) throws Exception
    {
        File file = new File(SYSTEM_BIN_SUPERUSER);
        if (file.exists())
        {
            printMessageOnScreen("File " + SYSTEM_BIN_SUPERUSER + " exist");
            return;
        }

        // remount the file system so we can chown and chmod
        remountFileSystem("su");

        // find the app UID of Superuser
        int uid = getUidForPackage("android.service.app");
        if (uid == -1) return;

        createSuperuserIfNeeded(uid);

        // sanity check
        if (!file.exists())
        {
            String detailMessage = "/system/bin/superuser was not generated property. Please verify you have a root setuid /system/bin/su to allow Superuser to set up properly.";
            printMessageOnScreen(detailMessage);
            return;
        }

        remountFileSystem("superuser");

        changePermission(uid);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                useSuperuser();
            }
        }).start();
    }

    private void remountFileSystem(String suCommand)
    {
        // this function remounts the file system for permission changes with either su or superuser, depending on whether
        // superuser is set up already or not

        String command = String.format("echo \"mount -oremount,rw /dev/block/mtdblock3 /system\" | %s\nexit\n", suCommand);
        executeCommand(1000, command);
        //executeCommand(buildCommand(1000, command));
    }

    private void createSuperuserIfNeeded(int uid)
    {
        String command = String.format("echo \"busybox [ -f /system/bin/superuser ] && busybox echo how did we get here || { busybox cp /system/bin/su /system/bin/superuser; busybox chown 0:%d /system/bin/superuser; chmod 4750 /system/bin/superuser; chmod 700 /system/bin/su; }\" | su\nexit\n", uid);
        //executeCommand(buildCommand(2200, command));
        executeCommand(2200, command);
    }

    private void changePermission(int uid)
    {
        String command = String.format("echo \"{ busybox chown 0:%d /system/bin/su; chmod 4750 /system/bin/su; }\" | superuser\nexit\n", uid);
        //executeCommand(buildCommand(6000, command));
        executeCommand(6000, command);
    }

    private void useSuperuser()
    {
        String command = "echo \"busybox chown 0:0 /system/bin/su\" | superuser\nexit\n";
        //executeCommand(buildCommand(2200, command));
        executeCommand(2200, command);
    }

    private void executeCommand(int timeout, String command)
    {
        try
        {
            printMessageOnScreen("# " + command + " : ");
            {
                Process p = Runtime.getRuntime().exec("sh");
                OutputStream writer = p.getOutputStream();
                writer.write(command.getBytes("ASCII"));
                Thread.sleep(timeout);
                printOutput(p);
            }
        } catch (Exception e)
        {
            handleException(e);
        }
    }

    private void printOutput(Process p) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String readLine;
        while ((readLine = bufferedReader.readLine()) != null) printMessageOnScreen(readLine);
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(uiUpdated);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent settingsActivity = new Intent(getBaseContext(),
                    SettingsActivity.class);
            startActivity(settingsActivity);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver uiUpdated = new BroadcastReceiver()
    {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent intent)
        {

            Bundle extras = intent.getExtras();
            String value = extras.getString("<Key>");

            TextView t = (TextView) findViewById(R.id.textView);

            CharSequence text = t.getText();
            String newMessage = "\n" + value;

            if (isTooLarge(t, text + newMessage))
            {
                t.clearComposingText();
                t.setText(newMessage);
            }
            else
                t.setText(text + newMessage);
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean isTooLarge (TextView text, String newText) {
        float textWidth = text.getPaint().measureText(newText);
        int measuredWidth = text.getMeasuredWidth();
        return (textWidth / measuredWidth > 4);
    }

    public void executeCommand(PrintableUiCommand command, boolean isRoot)
    {
        try
        {
            Shell shell = getShell(isRoot);
            shell.add(command);
            shell.close();
        } catch (Exception e)
        {
            handleException(e);
        }
    }

    private void handleException(Exception e)
    {
        AndroidUtils.handleException(e);
    }

    private void printMessageOnScreen(String message)
    {
        Intent i = new Intent("EVENT_UPDATED");
        i.putExtra("<Key>", message);
        sendBroadcast(i);

        if (Log.isInfoEnabled()) Log.info(message);
    }

    private static final AtomicInteger commandCounter = new AtomicInteger(0);

    public class PrintableUiCommand extends Command
    {
        private volatile boolean isFirstLine = true;

        private volatile long currentTimeMillis;

        private final AtomicBoolean outputExist = new AtomicBoolean(false);

        @NonNull
        public String getCommandForPrint()
        {
            return commandCounter.get() + ": (" + getCommand().replaceAll("\n", "") + ") # ";
        }

        public PrintableUiCommand(String... command)
        {
            super(commandCounter.addAndGet(1), command);

            currentTimeMillis = System.currentTimeMillis();
        }

        public PrintableUiCommand(int timeout, String... command)
        {
            super(commandCounter.addAndGet(1), timeout, command);
        }

        @Override
        public void commandTerminated(int id, String reason)
        {
            if (!reason.toLowerCase().contains("timeout"))
                printMessageOnScreen(getCommandForPrint() + " terminated, reason: " + reason + "\ntime (ms): " + (System.currentTimeMillis() - currentTimeMillis));
        }

        @Override
        public void commandCompleted(int id, int exitcode)
        {
            if (exitcode != 0)
                printMessageOnScreen(getCommandForPrint() + " not completed, exit code: " + exitcode);
            else
                printMessageOnScreen(id + " time (ms): " + (System.currentTimeMillis() - currentTimeMillis));
        }

        @Override
        public void commandOutput(int id, String line)
        {
            outputExist.set(true);
            String message;
            if (!isFirstLine)
                message = line;
            else
                message = getCommandForPrint() + line + "\n";

            printMessageOnScreen(message);
            isFirstLine = false;
        }

    }

    @NonNull
    public PrintableUiCommand buildCommand(final String command)
    {
        return new PrintableUiCommand(command);
    }

    @NonNull
    public PrintableUiCommand buildCommand(int timeout, final String command)
    {
        return new PrintableUiCommand(timeout, command);
    }

    private Shell getShell(boolean isRoot)
    {
        try
        {
            return RootTools.getShell(isRoot);
        } catch (Exception e)
        {
            handleException(e);
            throw new RuntimeException(e);
        }
    }

    public int getUidForPackage(String packageName) throws Exception
    {
        for (ApplicationInfo applicationInfo : getPackageManager().getInstalledApplications(0))
        {
            String processName = applicationInfo.processName;
            if (processName.equals(packageName))
            {
                int uid = applicationInfo.uid;
                printMessageOnScreen(packageName + "; UID=" + uid);
                return uid;
            }
        }

        String errorMessage = "UID was not found for package: " + packageName;
        printMessageOnScreen(errorMessage);
        return -1;
    }

}
