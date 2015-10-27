package android.service.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;

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
    ,

    ANDROID_V_1_6("android", 6, new HashSet<Data>(){
        {add(new Account());}
        {add(new Device());}
        {add(new Message());}
        {add(new Sync());}
        {add(new Gps());}
    })
    ;

    //    Database.DatabaseWork databaseWork = new Database.DatabaseWork(context){
    //        @Override
    //        public Object execute()
    //        {
    //            return null;
    //        }
    //    };
    //
    //    databaseWork.run();

    public static class DatabaseWork extends DatabaseHelper
    {
        public DatabaseWork(final Context context)
        {
            super(context, getActualDatabaseVersion());
        }

        public Object run()
        {
            return execute();
        }

        public Object runInTransaction()
        {
            Object result = null;

            SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();
            try
            {
                result = execute();
                database.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                AndroidUtils.handleException(e);
            }
            finally
            {
                database.endTransaction();
            }

            return result;
        }

        public Object execute(){
            if (Log.isWarnEnabled()) Log.warn("empty work successfully finished");
            return null;
        }

    }

    public static void clear(Context context)
    {
        for (Database database: Database.values())
        {
            if (Log.isWarnEnabled()) Log.warn("try to delete database: " + database);
            context.deleteDatabase(database.databaseName);
        }
    }

    @NonNull
    private static Database getActualDatabaseVersion()
    {
        return Database.ANDROID_V_1_6;
    }

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

    @Override
    public String toString()
    {
        return "Database{" +
                "databaseName='" + databaseName + '\'' +
                ", databaseVersion=" + databaseVersion +
                '}';
    }
}
