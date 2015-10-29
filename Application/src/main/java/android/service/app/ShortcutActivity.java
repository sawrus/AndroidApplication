package android.service.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

public class ShortcutActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Context context = getApplicationContext();

        Intent HomeScreenShortCut = new Intent(context,
                MainActivity.class);

        HomeScreenShortCut.setAction(Intent.ACTION_MAIN);
        HomeScreenShortCut.putExtra("duplicate", false);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, HomeScreenShortCut);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "AndroidApplication");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context,
                android.R.drawable.ic_menu_save));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        context.sendBroadcast(addIntent);

        super.onCreate(savedInstanceState);
    }
}
