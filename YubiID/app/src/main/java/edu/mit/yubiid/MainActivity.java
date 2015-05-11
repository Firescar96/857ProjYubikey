package edu.mit.yubiid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity {

    private static final String NEO_STORE = "NEO_STORE";
    private static final String YUBICO = "https://my.yubico.com/neo/";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String TAG = "MainActivity";

    private static final int PUB_KEY = 1;
    private static final int OTP = 2;
    private int state = 0;

    private String pubAddress;
    private String otp;

    //private ExchangesDbHelper dbHelper = new ExchangesDbHelper(this);
    //private SQLiteDatabase db = dbHelper.getReadableDatabase();
    
    private static MainActivity context;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        context = this;

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(getIntent() != null)
            handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (Exception e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    private void handleIntent(Intent intent) {
        /*if(getIntent().getDataString() != null)
            Log.i(TAG, getIntent().getDataString());
        else if(getIntent().getExtras() != null) {
            Parcelable[] messages = (Parcelable[]) getIntent().getExtras().get("android.nfc.extra.NDEF_MESSAGES");
            NdefRecord record= ((NdefMessage) messages[0]).getRecords()[0];
            try {
                state = PUB_KEY;
                byte[] payload = record.getPayload();

                // Get the Text Encoding
                String utf8 = "UTF-8";
                String utf16 = "UTF-16";
                String textEncoding = ((payload[0] & 128) == 0) ? utf8 : utf16;

                // Get the Language Code
                int languageCodeLength = payload[0] & 0063;

                // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                // e.g. "en"

                // Get the Text
                Log.i(TAG, "payload"+new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding));
                postKeyExecute(new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding));
                return;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else
            Log.i(TAG,getIntent().toString());*/

        /*if(getIntent() != null) {
            String pubAddress = getIntent().getDataString().split(":")[1];
            String otp = getIntent().getDataString().split(":")[2];
            ((EditText) findViewById(R.id.amount)).setText(otp);
        }*/
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Log.i(TAG,"Executing Task");
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return "";
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String utf8 = "UTF-8";
            String utf16 = "UTF-16";
            String textEncoding = ((payload[0] & 128) == 0) ? utf8 : utf16;

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            postKeyExecute(result);
        }
    }

    public void postKeyExecute(String result) {
        Log.i(TAG,result+": "+state);
        if (result != null) {
            if(state == PUB_KEY) {
                pubAddress = result.split(":")[PUB_KEY];
                ((EditText) findViewById(R.id.addresss)).setText(pubAddress);
            }
            if(state == OTP) {
                otp = result.split(":")[OTP];
                ((EditText) findViewById(R.id.verification)).setText(otp);
            }
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
        sendDataToBackend();
    }

    public void sendDataToBackend()
    {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... arg0) {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                String msg = "";
                InputStream inputStream = null;

                try {

                    // 1. create HttpClient
                    HttpClient httpclient = new DefaultHttpClient();

                    String amount = ((EditText) findViewById(R.id.amount)).getText().toString();
                    pubAddress = ((EditText) findViewById(R.id.addresss)).getText().toString();
                    otp = ((EditText) findViewById(R.id.verification)).getText().toString();

                    // 2. make POST request to the given URL
                    HttpGet httpGet = new HttpGet("http://macgregor.mit.edu:12345?amnt="+amount+"&pa="+pubAddress+"&otp="+otp);

                    // 7. Set some headers to inform server about the type of the content
                    httpGet.setHeader("Accept", "application/json");
                    httpGet.setHeader("Content-type", "application/json");

                    HttpParams httpParams = httpclient.getParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);
                    httpGet.setParams(httpParams);

                    // 8. Execute POST request to the given URL
                    Log.i(TAG,"executing");
                    HttpResponse httpResponse = httpclient.execute(httpGet);
                    // 9. receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();

                    // 10. convert inputstream to string
                    if(inputStream != null) {
                        Message mess = new Message();
                        mess.obj = convertInputStreamToString(inputStream);
                        contextHandler.sendMessage(mess);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

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

    //Previous Code for sending verification OTP to backend
    /*
    public void sendMessage(View v) {
        new AsyncTask <Object, Object, Object>() {

            private static final String TAG = "MainActivity";

            @Override
            protected Object doInBackground(Object...arg) {
                try {
                    Connection con = Jsoup.connect(YUBICO+((EditText) findViewById(R.id.amount)).getText());

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
    };*/

    public void setAddressMode(View v) {
            state = PUB_KEY;
    }

    public void setVerificationMode(View v) {
        state = OTP;
    }
    
   /* private void sendMoney(String senderyubikey, String publicyubikey, int amount) {
	   ContentValues values = new ContentValues();

	   values.put(Exchanges.COLUMN_NAME_SENDER_YUBIKEY, senderyubikey);
	   values.put(Exchanges.COLUMN_NAME_PUBLIC_YUBIKEY, publicyubikey);
	   values.put(Exchanges.COLUMN_NAME_AMOUNT, amount);

       int count = db.update(Exchanges.TABLE_NAME,values,null,null);

   }
   
   private  void receiveMoney(String senderyubikey, String publicyubikey, int amount) {
	   ContentValues values = new ContentValues();

	   values.put(Exchanges.COLUMN_NAME_SENDER_YUBIKEY, senderyubikey);
	   values.put(Exchanges.COLUMN_NAME_PUBLIC_YUBIKEY, publicyubikey);
	   values.put(Exchanges.COLUMN_NAME_AMOUNT, amount);
	   
	   int count = db.update(Exchanges.TABLE_NAME,values,null,null);
			   
   }*/

}