package android.service.app.db;

import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;

import java.util.HashSet;
import java.util.Set;

public enum Database
{
    ANDROID_V_1_1("android", 1, new HashSet<Data>(){
        {add(new Account());}
        {add(new Device());}
        {add(new Message());}
    }),

    ANDROID_V_1_2("android", 2, new HashSet<Data>(){
        {add(new Account());}
        {add(new Device());}
        {add(new Message());}
        {add(new Sync());}
    }),

    ANDROID_V_1_3("android", 3, new HashSet<Data>(){
        {add(new Account());}
        {add(new Device());}
        {add(new Message());}
        {add(new Sync());}
    }),

    ANDROID_V_1_4("android", 4, new HashSet<Data>(){
        {add(new Account());}
        {add(new Device());}
        {add(new Message());}
        {add(new Sync());}
    })
    ,

    ANDROID_V_1_5("android", 5, new HashSet<Data>(){
        {add(new Account());}
        {add(new Device());}
        {add(new Message());}
        {add(new Sync());}
        {add(new Gps());}
    })
    ;

    public final String databaseName;
    public final Integer databaseVersion;
    public final Set<Data> tableSet;

    Database(String databaseName, Integer databaseVersion, Set<Data> tableSet)
    {
        this.databaseName = databaseName;
        this.databaseVersion = databaseVersion;
        this.tableSet = tableSet;
    }

    public static Database getDatabaseByNameVersion(String name, Integer version)
    {
        for (Database database: values())
        {
            if (database.databaseName.equals(name) && database.databaseVersion.equals(version))
                return database;
        }

        throw new IllegalStateException("absent database " + name + " with version " + version);
    }
}
