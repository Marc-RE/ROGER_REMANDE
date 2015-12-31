package fr.esiea.remanderoger.gameweapons;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetWeaponsService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_WEAPONS = ".action.WEAPONS";

    public GetWeaponsService() {
        super("GetWeaponsService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionWeapons(Context context) {
        Intent intent = new Intent(context, GetWeaponsService.class);
        intent.setAction(ACTION_WEAPONS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WEAPONS.equals(action)) {
                handleActionWeapons();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWeapons() {
        // TODO: Handle action Foo
        Log.d(ACTION_WEAPONS, "Thread service name: " + Thread.currentThread().getName());
        URL url = null;
        try {
            url = new URL("http://cdn.vpshark.io/weapons.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if(HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                copyInputStreamToFile(connection.getInputStream(),
                        new File(getCacheDir(), "weapons.json"));
                Log.d(ACTION_WEAPONS, getCacheDir().getAbsolutePath());
                Log.d(ACTION_WEAPONS, "weapons.json downloaded :-)");
                LocalBroadcastManager.getInstance(this).
                        sendBroadcast(new Intent(MainActivity.WEAPONS_UPDATE));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while((len = in.read(buffer))>0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
