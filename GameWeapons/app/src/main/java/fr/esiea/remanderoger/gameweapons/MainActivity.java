package fr.esiea.remanderoger.gameweapons;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView rv;
    final RecyclerView.LayoutManager layoutManager =
            new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GetWeaponsService.startActionWeapons(this);
        IntentFilter intentFilter = new IntentFilter(WEAPONS_UPDATE);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(new WeaponsUpdate(), intentFilter);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rv = (RecyclerView) findViewById(R.id.rv_weapons);
        rv.setHasFixedSize(true); // Improves performance of RV
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(new WeaponsAdapter(getWeaponsFromFile()));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add feature not yet implemented, coming soon !", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_refresh) {
            GetWeaponsService.startActionWeapons(this);
        } else if (id == R.id.nav_signup) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.sign_up_url)));
            startActivity(browserIntent);
        } else if (id == R.id.nav_signin) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static String WEAPONS_UPDATE = ".WEAPONS_UPDATE";

    public class WeaponsUpdate extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Log.d(WEAPONS_UPDATE, getIntent().getAction());
            android.support.v4.app.NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(MainActivity.this)
                            .setSmallIcon(R.drawable.ic_ak_47)
                            .setContentTitle("Weapons downloader")
                            .setContentText("Your weapons have been downloaded");
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notificationBuilder.build());
            WeaponsAdapter wa = (WeaponsAdapter) MainActivity.this.rv.getAdapter();
            wa.setNewWeapon();
        }
    }

    public JSONArray getWeaponsFromFile() {
        try {
            InputStream in = new FileInputStream(getCacheDir()
                + "/" + "weapons.json");
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            in.close();
            return new JSONArray(new String(buffer, "UTF-8"));
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            return new JSONArray();
        } catch(IOException e) {
            e.printStackTrace();
            return new JSONArray();
        } catch(JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    private class WeaponsAdapter extends RecyclerView.Adapter<WeaponsAdapter.WeaponHolder> {

        private JSONArray weapons = null;

        public WeaponsAdapter(JSONArray weapons) {
            this.weapons = weapons;
        }

        @Override
        public WeaponsAdapter.WeaponHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.rv_weapon_element, parent, false);
            WeaponHolder weaponHolder = new WeaponHolder(v);
            return weaponHolder;
        }

        @Override
        public void onBindViewHolder(WeaponsAdapter.WeaponHolder holder, int position) {
            try {
                holder.weaponName.setText(weapons.getJSONObject(position).getString("name"));
                holder.weaponPrice.setText("$" + String.valueOf(weapons.getJSONObject(position).getInt("price")));
                holder.weaponVG.setText(weapons.getJSONObject(position).getString("video_game"));
                new ImagesDownloader(holder.weaponPhoto).execute(weapons.getJSONObject(position).getString("image"));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return this.weapons.length();
        }

        public void setNewWeapon() {
            weapons = getWeaponsFromFile();
            notifyDataSetChanged();
        }

        public class WeaponHolder extends RecyclerView.ViewHolder {
            public CardView cvWeapons;
            public TextView weaponName;
            public TextView weaponPrice;
            public ImageView weaponPhoto;
            public TextView weaponVG;

            public WeaponHolder(View itemView) {
                super(itemView);
                cvWeapons = (CardView)itemView.findViewById(R.id.cv_weapons);
                weaponName = (TextView)itemView.findViewById(R.id.weapon_name);
                weaponPrice = (TextView)itemView.findViewById(R.id.weapon_price);
                weaponPhoto = (ImageView)itemView.findViewById(R.id.weapon_photo);
                weaponVG = (TextView)itemView.findViewById(R.id.weapon_vg);
            }
        }
    }
}
