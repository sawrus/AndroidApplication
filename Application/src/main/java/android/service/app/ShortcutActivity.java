package android.service.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ShortcutActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent HomeScreenShortCut = new Intent(getApplicationContext(),
                MainActivity.class);

        HomeScreenShortCut.setAction(Intent.ACTION_MAIN);
        HomeScreenShortCut.putExtra("duplicate", true);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, HomeScreenShortCut);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "AndroidApplication");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                android.R.drawable.ic_menu_save));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        getApplicationContext().sendBroadcast(addIntent);

        super.onCreate(savedInstanceState);
    }
}
