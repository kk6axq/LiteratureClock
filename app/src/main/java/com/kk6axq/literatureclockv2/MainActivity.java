package com.kk6axq.literatureclockv2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*

TODO: Deal with with multiple images for one time
TODO: Format credits with html
TODO: Add github link to credits
 */
public class MainActivity extends AppCompatActivity {

    private final boolean IMAGE = true;
    private final boolean METADATA = false;
    private boolean currentType = IMAGE;
    private String hour = "00";
    private String minute = "00";
    private String lastGoodHour = "00";
    private String lastGoodMinute = "00";
    private ImageView imageView;
    private static String TAG = MainActivity.class.getName();

    private final String IMAGE_HEAD = "quote_";
    private final String IMAGE_TAIL = "_0";
    private final String METADATA_TAIL = "_credits";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListeners();
        imageView = findViewById(R.id.imageView1);

        updateTime();
        getRecentGoodTime();
        updateScreen();
        showCredits();
    }

    /**
     * Tries to find the most recent valid time image by iterating through the last 8 minutes of
     * images
     */
    private void getRecentGoodTime(){
        int temp_min = Integer.parseInt(minute);
        int temp_hour = Integer.parseInt(hour);
        String time;
        //Iterate through last 8 minutes
        for(int i = 0; i < 8; i++){
            //Decrement time by one minute
            //If minute digit is not 0
            if(temp_min != 0){
                //Decrement minute
                temp_min--;
            }else{
                //If hour digit is not 0
                if(temp_hour != 0){
                    //Decrement hour
                    temp_hour--;
                    }else{//Hour is 0
                    //Set hour to 23
                    temp_hour = 23;
                }
                //Set minute to 59
                temp_min = 59;
            }

            time = formatTime(temp_hour, temp_min);
            if(isValidImage(time)){
                lastGoodHour = intToFormattedString(temp_hour);
                lastGoodMinute = intToFormattedString(temp_min);
                //Leave for loop, a valid image has been found.
                break;
            }
        }

    }
    @NonNull
    private String formatTime(int h, int m){
        return intToFormattedString(h) + intToFormattedString(m);
    }

    private String intToFormattedString(int i){
        String out = Integer.toString(i);
        if(out.length() == 0){
            out = "0" + out;
        }
        return out;
    }
    private boolean isValidImage(String time){
        String filename = IMAGE_HEAD + time + IMAGE_TAIL;
        return isValidFilename(filename);
    }
    private boolean isValidFilename(String filename){
        return getResources().getIdentifier(filename,
                "drawable", getPackageName()) != 0;
    }
    private void showCredits(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getString(R.string.credit_title));
        alertDialog.setMessage(getString(R.string.credits));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.credit_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void imageClick(View view){
        currentType = !currentType;
        updateScreen();
    }

    private void addListeners(){

        Timer timer = new Timer();
        TimerTask minuteTask = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateTime();
                        updateScreen();
                        Log.e(TAG, "Screen Updated");
                    }
                });

            }
        };
        timer.schedule(minuteTask, 0l, 1000 * 10);
    }


    private void updateTime(){
        Date dt = new Date();
        //Format time as hhmm for fetching image
        SimpleDateFormat m = new SimpleDateFormat("mm");
        SimpleDateFormat h = new SimpleDateFormat("kk");
        //Convert time to string
        hour = h.format(dt);
        minute = m.format(dt);
        Log.e(TAG, "Hour: " + hour);
        Log.e(TAG, "Minute: " + minute);
    }
    private void updateScreen(){
        String filename = "quote_" + hour + minute + "_0";
        Log.e(MainActivity.class.getName(), filename);
        if(!isValidFilename(filename)) {
            filename = "quote_" + lastGoodHour + lastGoodMinute + "_0";
        }else{
            lastGoodHour = hour;
            lastGoodMinute = minute;
        }
        if(currentType == METADATA){
            filename += "_credits";
        }
        imageView.setImageResource(getResources().getIdentifier(filename,
                "drawable", getPackageName()));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_credits:
                showCredits();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

}
