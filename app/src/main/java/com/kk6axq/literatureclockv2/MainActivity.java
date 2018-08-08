package com.kk6axq.literatureclockv2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*
TODO: Deal with with multiple images for one time
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Image type definition for {@link #currentType}.
     */
    private final boolean IMAGE = true;
    /**
     * Metadata image type definition for {@link #currentType}.
     */
    private final boolean METADATA = false;
    /**
     * Holds the type of the onscreen image.
     */
    private boolean currentType = IMAGE;
    /**
     * Current hour.
     */
    private String hour = "00";
    /**
     * Current minute.
     */
    private String minute = "00";
    /**
     * Last hour with an image available.
     */
    private String lastGoodHour = "00";
    /**
     * Last minute with an image available.
     */
    private String lastGoodMinute = "00";

    /**
     * Class tag used in Log statements.
     */
    private static String TAG = MainActivity.class.getName();

    /**
     * Object for image display.
     */
    private ImageView imageView;

    /**
     * Text used to prefix time for image. Applies to both regular images and metadata images.
     */
    private final String IMAGE_HEAD = "quote_";

    /**
     * Text used to complete the filename of a regular image.
     */
    private final String IMAGE_TAIL = "_0";

    /**
     * Text used to complete the filename of a metadata image.
     */
    private final String METADATA_TAIL = "_credits";

    /**
     * Called on app start.
     *
     * @param savedInstanceState Saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize timer.
        initTimer();

        //Get image view from activity layout.
        imageView = findViewById(R.id.imageView1);

        //Get the latest time.
        updateTime();

        //Get the latest available image.
        getRecentGoodTime();

        //Display the latest available image.
        updateScreen();

        //Display instructions alert dialog.
        showInstructions();

        //Display credits dialog.
        showCredits();
    }

    /**
     * Tries to find the most recent valid time image by iterating through the last 8 minutes of
     * images.
     */
    private void getRecentGoodTime(){
        int temp_min = Integer.parseInt(minute);
        int temp_hour = Integer.parseInt(hour);
        String time;
        //Iterate through last 8 minutes.
        for (int i = 0; i < 8; i++) {
            //Decrement time by one minute.
            //If minute digit is not 0.
            if (temp_min != 0) {
                //Decrement minute.
                temp_min--;
            } else {
                //If hour digit is not 0.
                if (temp_hour != 0) {
                    //Decrement hour.
                    temp_hour--;
                } else {//Hour is 0.
                    //Set hour to 23.
                    temp_hour = 23;
                }
                //Set minute to 59.
                temp_min = 59;
            }
            //Get formatted time string.
            time = formatTime(temp_hour, temp_min);
            //If time string has an associated image.
            if (isValidImage(time)) {
                //Save time.
                lastGoodHour = intToFormattedString(temp_hour);
                lastGoodMinute = intToFormattedString(temp_min);
                //Leave for loop, a valid image has been found.
                break;
            }
        }

    }

    /**
     * Converts two ints representing hour and minute to a formatted time string in the format "hhmm".
     *
     * @param h Hour.
     * @param m Minute.
     * @return Formatted time string.
     */
    @NonNull
    private static String formatTime(int h, int m){
        return intToFormattedString(h) + intToFormattedString(m);
    }

    /**
     * Converts int to String in format "xx".
     *
     * @param i Input int.
     * @return Formatted String.
     */
    private static String intToFormattedString(int i){
        String out = Integer.toString(i);
        //If out is only 1 character long, ie <10, there needs to be a leading 0.
        if(out.length() == 0){
            out = "0" + out;
        }
        return out;
    }

    /**
     * Checks if time string has a corresponding valid image resource.
     * @param time Formatted time String in format "hhmm".
     * @return True if there is a valid image for that time, false otherwise.
     */
    private boolean isValidImage(String time){
        String filename = IMAGE_HEAD + time + IMAGE_TAIL;
        return isValidFilename(filename);
    }

    /**
     * Checks if filename exists in resources.
     * @param filename Filename String.
     * @return True if the file exists, false otherwise.
     */
    private boolean isValidFilename(String filename){
        return getResources().getIdentifier(filename,
                "drawable", getPackageName()) != 0;
    }

    /**
     * Shows instructions as an alert dialog.
     */
    private void showInstructions() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getString(R.string.instructions_title));


        alertDialog.setMessage(getString(R.string.instructions));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.instructions_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * Shows credits as an alert dialog.
     */
    private void showCredits(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getString(R.string.credits_title));
        alertDialog.setMessage(getString(R.string.credits));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.credits_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * Called when image is clicked. Switches {@link #currentType} from {@link #IMAGE} to 
     * {@link #METADATA} and vice versa. Also calls a screen update.
     * @param view View click was called in.
     */
    public void imageClick(View view){
        currentType = !currentType;
        updateScreen();
    }

    /**
     * Starts timer to run every 10 seconds.
     */
    private void initTimer(){

        Timer timer = new Timer();
        TimerTask minuteTask = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateTime();
                        updateScreen();
                        Log.d(TAG, "Screen Updated");
                    }
                });

            }
        };
        timer.schedule(minuteTask, 0L, 1000 * 10);
    }

    /**
     * Updates {@link #hour} and {@link #minute} to latest time from clock
     */
    private void updateTime(){
        Date dt = new Date();
        //Format time as mm and kk(24 hour format of hour)for fetching image.
        SimpleDateFormat m = new SimpleDateFormat("mm");
        SimpleDateFormat h = new SimpleDateFormat("kk");
        //Convert time to string.
        hour = h.format(dt);
        minute = m.format(dt);
        //Log updated time.
        Log.d(TAG, "Hour: " + hour);
        Log.d(TAG, "Minute: " + minute);
    }

    /**
     * Writes most recent available image to file.
     */
    private void updateScreen(){
        String filename = "quote_" + hour + minute + "_0";
        Log.d(MainActivity.class.getName(), filename);
        if(!isValidFilename(filename)) {
            filename = "quote_" + lastGoodHour + lastGoodMinute + "_0";
        }else{
            lastGoodHour = hour;
            lastGoodMinute = minute;
        }
        //Get correct type of image, defaults to IMAGE.
        if (currentType == METADATA) {
            filename += METADATA_TAIL;
        }
        //Set image.
        imageView.setImageResource(getResources().getIdentifier(filename,
                "drawable", getPackageName()));

    }

    /**
     * Called when menu item clicked.
     * @param item The item that was clicked.
     * @return True if handled correctly.
     */
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

    /**
     * Adds menu items to menu.
     * @param menu Menu to add items to.
     * @return True.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

}
