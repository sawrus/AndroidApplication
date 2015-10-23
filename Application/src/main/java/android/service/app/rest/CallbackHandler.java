package android.service.app.rest;

public abstract class CallbackHandler<T>
{
    public abstract void handle(T result);
}
