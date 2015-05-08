package edu.mit.yubiid;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import server.Contract.Exchanges;
import server.ExchangesDbHelper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity {

    private static final String NEO_STORE = "NEO_STORE";
    private static final String YUBICO = "https://my.yubico.com/neo/";
    private static final String TAG = "MainActivity";

    private ExchangesDbHelper dbHelper = new ExchangesDbHelper(getContext()); 
    private SQLiteDatabase db = dbHelper.getReadableDatabase();
    
    private static MainActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        context = this;
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/

        if(getIntent() != null) {
            Log.i(TAG, getIntent().getDataString());
            ((EditText) findViewById(R.id.message)).setText(getIntent().getDataString().replace(YUBICO,""));
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
        new AsyncTask <Object, Object, Object>() {

            private static final String TAG = "MainActivity";

            @Override
            protected Object doInBackground(Object...arg) {
                try {
                    Connection con = Jsoup.connect(YUBICO+((EditText) findViewById(R.id.message)).getText());

                    Document doc = con.get();
                    Elements info = doc.select("#info");
                    Log.i(TAG,info.text());
                    Message msg = new Message();
                    msg.obj = info.text();
                    contextHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private static Handler contextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String pattern = "status=OK";

            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher((String) msg.obj);
            if (m.find( ))
                ((TextView) context.findViewById(R.id.response)).setText("VALID CODE");
            else
                ((TextView) context.findViewById(R.id.response)).setText("INVALID CODE");
        }
    };
    
   private static void sendMoney(String senderyubikey, String publicyubikey, int amount) {
	   Content values = new ContentValues();

	   values.put(Exchanges.COLUMN_NAME_SENDER_YUBIKEY, senderyubikey);
	   values.put(Exchanges.COLUMN_NAME_PUBLIC_YUBIKEY, publicyubikey);
	   values.put(Exchanges.COLUMN_NAME_AMOUNT, amount);
	   
	   int count = db.update(
			   ExchangesDbHelper.Exchanges.TABLE_NAME,
			   values);
			   
   }
   
   private static void receiveMoney(String senderyubikey, String publicyubikey, int amount) {
	   Content values = new ContentValues();

	   values.put(Exchanges.COLUMN_NAME_SENDER_YUBIKEY, senderyubikey);
	   values.put(Exchanges.COLUMN_NAME_PUBLIC_YUBIKEY, publicyubikey);
	   values.put(Exchanges.COLUMN_NAME_AMOUNT, amount);
	   
	   int count = db.update(
			   ExchangesDbHelper.Exchanges.TABLE_NAME,
			   values);
			   
   }

}