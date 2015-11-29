package android.service.app.task;

public abstract class CallbackHandler<T>
{
    public abstract void handle(T result);
}
