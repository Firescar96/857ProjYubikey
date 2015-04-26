package edu.mit.yubiid;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
        if(getIntent() != null) {
            Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.i(TAG, Arrays.toString(tag.getTechList()));
            transTag(tag, "hello world");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void sendMessage(View v) {

    }

    private static final String TAG = "MainActivity";

    public void transTag(Tag tag, String tagText) {
        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();
            byte[] response = isoDep.transceive(tagText.getBytes());
            Log.i(TAG,new String(response));
        } catch (IOException e) {
            Log.e(TAG, "IOException while closing MifareClassic...", e);
        } finally {
            try {
                isoDep.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareClassic...", e);
            }
        }
    }

}