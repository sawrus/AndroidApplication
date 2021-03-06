package android.service.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ShortcutActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Context context = getApplicationContext();

        addShortcut(context);

        super.onCreate(savedInstanceState);
    }

    public static void addShortcut(Context context)
    {
        Intent removeIntent = new Intent();

        removeIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        context.sendBroadcast(removeIntent);

        Intent HomeScreenShortCut = new Intent(context,
                DataActivity.class);

        HomeScreenShortCut.setAction(Intent.ACTION_MAIN);
        HomeScreenShortCut.putExtra("duplicate", false);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, HomeScreenShortCut);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "AndroidApplication");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context,
                android.R.drawable.ic_menu_save));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        context.sendBroadcast(addIntent);
    }
}
