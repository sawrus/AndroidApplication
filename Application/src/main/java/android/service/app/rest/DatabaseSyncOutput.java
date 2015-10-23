package android.service.app.rest;

public class DatabaseSyncOutput
{
    private String output;

    public DatabaseSyncOutput(String output)
    {
        this.output = output;
    }

    @Override
    public String toString()
    {
        return "DatabaseSyncOutput{" +
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
}
