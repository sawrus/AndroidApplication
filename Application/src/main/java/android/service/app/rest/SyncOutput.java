package android.service.app.rest;

import android.service.app.utils.Log;
import android.support.annotation.NonNull;

public class SyncOutput
{
    private Object output;

    public SyncOutput(Object output)
    {
        this.output = output;
    }

    public boolean isSuccess()
    {
        return !(output instanceof Throwable);
    }

    @Override
    public String toString()
    {
        if (output instanceof Throwable)
        {
            return "unkown exception";
        }
        else
            return "SyncOutput{" +
                    "output='" + output + '\'' +
                    '}';
    }

    public Object getOutput()
    {
        return output;
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

                Object output = result.getOutput();
                if (output instanceof Throwable)
                    Log.error((Throwable) output);
                else if (Log.isInfoEnabled()) Log.info("result=" + result);
            }

            @Override
            public String toString()
            {
                return String.valueOf(result);
            }
        };
    }
}
