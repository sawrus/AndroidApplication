package android.service.app.rest;

import android.service.app.utils.Log;
import android.support.annotation.NonNull;

public class SyncOutput
{
    private String output;

    public SyncOutput(String output)
    {
        this.output = output;
    }

    @Override
    public String toString()
    {
        return "SyncOutput{" +
                "output='" + output + '\'' +
                '}';
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    @NonNull
    public static CallbackHandler<SyncOutput> getStringCallbackHandler()
    {
        return new CallbackHandler<SyncOutput>()
        {
            private SyncOutput result;

            @Override
            public void handle(SyncOutput result)
            {
                this.result = result;
                Log.v("result=" + result);
            }

            @Override
            public String toString()
            {
                return String.valueOf(result);
            }
        };
    }
}
