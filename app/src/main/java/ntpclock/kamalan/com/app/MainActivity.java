package ntpclock.kamalan.com.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ntpclock.kamalan.com.utility.InternetConnection;
import ntpclock.kamalan.com.utility.NTPClient;
import ntpclock.kamalan.com.widget.MyAnalogClock;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String SERVER_ADDR = "0.asia.pool.ntp.org";
    private static final long UPDATE_INTERVALS = 10 * 60 * 1000;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            displayCurrentTime();
            displayRemainingTime();
            displayAnalogClock();
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    private Date currentTime;
    private Date nextUpdateTime;

    private TextView mTimeDisplay;
    private TextView mTimeRemaining;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimeDisplay = (TextView) findViewById(R.id.tvTimeDisplay);
        mTimeRemaining = (TextView) findViewById(R.id.tvRemainingTime);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        getTimeFromNTPServer();
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkInternetStatus();
    }

    protected void onStop() {
        super.onStop();

        mHandler.removeCallbacks(mRunnable);
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
        if (id == R.id.action_sync) {
            getTimeFromNTPServer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getTimeFromNTPServer() {
        if(InternetConnection.isAvailable(MainActivity.this))
            new getNTPTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    private void checkInternetStatus() {
        if(!InternetConnection.isAvailable(MainActivity.this))
            Toast.makeText(MainActivity.this, "Internet connection not found!", Toast.LENGTH_LONG).show();
    }

    private void displayCurrentTime() {
        if (currentTime.getTime() >= nextUpdateTime.getTime()) {
            getTimeFromNTPServer();
            return;
        }

        long newTime = currentTime.getTime();
        newTime += 1000; // reduce one seconds
        currentTime = new Date(newTime);
        String newDate = new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss").format(currentTime);
        mTimeDisplay.setText("Current network time:\n" + newDate);
    }

    private void displayRemainingTime() {
        long remainingTime = nextUpdateTime.getTime() - currentTime.getTime();
        Date time = new Date(remainingTime);
        String newDate = new SimpleDateFormat("mm:ss").format(time);
        mTimeRemaining.setText("To next update:\n" + newDate);
    }

    private void displayAnalogClock() {
        mRelativeLayout.removeAllViews();
        mRelativeLayout.addView(new MyAnalogClock(MainActivity.this,
                mRelativeLayout.getMeasuredWidth()/2,
                mRelativeLayout.getMeasuredHeight()/2,
                currentTime));
    }

    /************************
     *   Async Task Class   *
     ************************/
    private class getNTPTask extends AsyncTask<Void, Void, Date> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "getNTPTask is about to start...");
            mHandler.removeCallbacks(mRunnable);
        }

        @Override
        protected Date doInBackground(Void... params) {
            Date date = null;

            NTPUDPClient client = new NTPUDPClient();
            // We want to timeout if a response takes longer than 10 seconds
            client.setDefaultTimeout(15000);
            try {
                client.open();
                try {
                    InetAddress hostAddr = InetAddress.getByName(SERVER_ADDR);
                    System.out.println("> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
                    TimeInfo info = client.getTime(hostAddr);
                    date = NTPClient.processResponse(info);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } finally {
                client.close();
            }

            return date;
        }

        @Override
        protected void onPostExecute(Date date) {
            Log.d(TAG, "getNTPTask finished its task.");

            if(date != null) {
                currentTime = date;
                long time = date.getTime();
                time += UPDATE_INTERVALS;
                nextUpdateTime = new Date(time);
                mHandler.post(mRunnable);
            } else
                Toast.makeText(MainActivity.this, "Error! Couldn't get the time", Toast.LENGTH_SHORT).show();
        }
    }

}
