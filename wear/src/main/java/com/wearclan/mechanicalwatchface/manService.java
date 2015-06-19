package com.wearclan.mechanicalwatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class manService extends CanvasWatchFaceService {

    private static final long INTERACTIVE_UPDATE_RATE_MS = 50;
    private DisplayMetrics mDM;
    private WindowManager mWM;
    private static final String TAG = "WatchFace";
    private float scaleIndex;
    private int centerX;
    private int centerY;
    private float  heightRatio;
    private float widthRatio;
    private String gear_speed;
    private String sector_checked;
    private SettingStatus settingStatus;
    private SettingMonitor.SettingMonitorCallback settingCallback;


    public manService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDM = new DisplayMetrics();
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWM.getDefaultDisplay().getMetrics(mDM);

        gear_speed = "1";
        sector_checked = "1";

        centerX = mDM.widthPixels / 2;
        centerY = mDM.heightPixels / 2;

        if(mDM.heightPixels==290 && mDM.widthPixels==320){
            centerY = 160;
        }

        heightRatio = (float) (mDM.heightPixels/320.0);
        widthRatio = (float) (mDM.widthPixels/320.0);


        GlobalWearApiHelper.getInstance(this).sendScreenView("Create  View ");


        settingCallback = new SettingMonitor.SettingMonitorCallback() {
            @Override
            public void onChanged(SettingStatus status) {
                settingStatus = status;
            }
        };

        settingStatus = new SettingStatus();
        SettingMonitor.getInstance().register(this, settingCallback);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        gear_speed = sp.getString("gear_speed","1");
        sector_checked = sp.getString("sector_checked","1");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SettingMonitor.getInstance().unregister(this, settingCallback);
        GlobalWearApiHelper.getInstance(this).sendScreenView("Change to other watchfaces");
    }

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class BitMap {
        /*Bitmap bmp;*/
        public Bitmap transform(int id) {

            return BitmapFactory.decodeResource(manService.this.getResources(), id);

        }


        public Bitmap transform(int id,float scaleIndex){
            Bitmap bitmap = BitmapFactory.decodeResource(manService.this.getResources(), id);
            return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scaleIndex), (int) (bitmap.getHeight() * scaleIndex), true);
        }
    }


    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;
        boolean mIsRound;
        private Paint mBgPaint;
        private Time mTime;


        Bitmap ambientBackground;
        Bitmap ambientSquare;
        Bitmap ambientRound;
        Bitmap background;
        Bitmap squareBackground;
        Bitmap roundBackground;
        Bitmap topWrapper;
        Bitmap roundWrapper;
        Bitmap squareWrapper;

        Bitmap gear1;
        Bitmap gear2;
        Bitmap bottomGear1;
        Bitmap bottomGear2;
        Bitmap bottomGear3;
        Bitmap bottomGear4;
        Bitmap gearWrapper;
        Bitmap secondWrapper;
        Bitmap minute;
        Bitmap hour;
        Bitmap ambientHour;
        Bitmap ambientMinute;



        private Paint mPaint;


        private int mIndex;

        Matrix gear1Matrix;
        Matrix gear2Matrix;
        Matrix bottomGear1Matrix;
        Matrix bottomGear2Matrix;
        Matrix bottomGear3Matrix;
        Matrix bottomGear4Matrix;
        Matrix secondMatrix;
        Matrix minuteMatrix;
        Matrix hourMatrix;
        Matrix ambientHourMatrix;
        Matrix ambientMinuteMatrix;


        float secondRadius;
        float minuteRadius;
        float hourRadius;



        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(manService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setHotwordIndicatorGravity(Gravity.RIGHT | Gravity.BOTTOM)
                    .build());

            GATimer();

            BitMap bitMap = new BitMap();
            roundBackground = bitMap.transform(R.mipmap.man_background_round);
            squareBackground = bitMap.transform(R.mipmap.man_background_square);
            ambientRound = bitMap.transform(R.mipmap.man_ambient_background_round);
            ambientSquare = bitMap.transform(R.mipmap.man_ambient_background_square);

            roundWrapper = bitMap.transform(R.mipmap.man_top_wrapper_round);
            squareWrapper = bitMap.transform(R.mipmap.man_top_wrapper_square);



            Log.d("round1:",mIsRound+"");

            scaleIndex = (float) (roundBackground.getWidth()/320.0);

            if(scaleIndex != 1.0){
                scaleIndex = 1;
            }else{
                scaleIndex = (float) (mDM.widthPixels/320.0);
            }

            gear1 = bitMap.transform(R.mipmap.man_gear_1,scaleIndex);
            gear2 = bitMap.transform(R.mipmap.man_gear_2,scaleIndex);
            bottomGear1 = bitMap.transform(R.mipmap.man_gear_bottomlayer1,scaleIndex);
            bottomGear2 = bitMap.transform(R.mipmap.man_gear_bottomlayer2,scaleIndex);
            bottomGear3 = bitMap.transform(R.mipmap.man_gear_bottomlayer3,scaleIndex);
            bottomGear4 = bitMap.transform(R.mipmap.man_gear_bottomlayer4,scaleIndex);
            gearWrapper = bitMap.transform(R.mipmap.man_gear_wrapper,scaleIndex);
            minute = bitMap.transform(R.mipmap.minute,scaleIndex);
            hour = bitMap.transform(R.mipmap.hour,scaleIndex);
            ambientHour = bitMap.transform(R.mipmap.ambient_hour,scaleIndex);
            ambientMinute = bitMap.transform(R.mipmap.ambient_minute,scaleIndex);
            secondWrapper = bitMap.transform(R.mipmap.second_wrapper,scaleIndex);


            gear1Matrix = new Matrix();
            gear2Matrix = new Matrix();
            bottomGear1Matrix = new Matrix();
            bottomGear2Matrix = new Matrix();
            bottomGear3Matrix = new Matrix();
            bottomGear4Matrix = new Matrix();
            secondMatrix = new Matrix();

            minuteMatrix = new Matrix();
            hourMatrix = new Matrix();
            ambientHourMatrix = new Matrix();
            ambientMinuteMatrix = new Matrix();

            mBgPaint = new Paint();
            mPaint = new Paint();
            mBgPaint.setColor(Color.BLACK);

            mTime = new Time();
            mTime.setToNow();



        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {



            super.onDraw(canvas, bounds);
            mTime.setToNow();

            if(gear_speed.equals("")){
                gear_speed = "1";
            }

            if(sector_checked.equals("")){
                sector_checked = "1";
            }

            //setting changed
            if(settingStatus.gear_speed!=null && !settingStatus.gear_speed.equals("")){
                gear_speed = settingStatus.gear_speed;
            }
            if(settingStatus.sector_checked!=null && !settingStatus.sector_checked.equals("")){
                sector_checked = settingStatus.sector_checked;
            }


            Date date = new Date(System.currentTimeMillis());

            float second = (float) Double.parseDouble((new SimpleDateFormat("ss.SSS")).format(date));


            long timeMs = System.currentTimeMillis();




            secondRadius = (float) (second*6);




            minuteRadius = mTime.minute*6;
            hourRadius = (float) (mTime.hour*30+mTime.minute*0.5);

            ambientMinuteMatrix.setRotate(minuteRadius,ambientMinute.getWidth()/2,ambientMinute.getHeight()-20*heightRatio);
            ambientMinuteMatrix.postTranslate(centerX - ambientMinute.getWidth() / 2, centerY - ambientMinute.getHeight() + 20 * heightRatio);

            ambientHourMatrix.setRotate(hourRadius,ambientHour.getWidth()/2,ambientHour.getHeight()-20*heightRatio);
            ambientHourMatrix.postTranslate(centerX - ambientHour.getWidth() / 2, centerY - ambientHour.getHeight() + 20 * heightRatio);

            secondMatrix.setRotate(secondRadius, secondWrapper.getWidth() / 2, secondWrapper.getHeight() / 2);
            secondMatrix.postTranslate(centerX - secondWrapper.getWidth() / 2, centerY - secondWrapper.getHeight() / 2);

            minuteMatrix.setRotate(minuteRadius, minute.getWidth() / 2, minute.getHeight() - 52 * heightRatio);
            minuteMatrix.postTranslate(centerX - minute.getWidth() / 2, centerY - minute.getHeight() + 52 * heightRatio);

            hourMatrix.setRotate(hourRadius, hour.getWidth() / 2, hour.getHeight() - 52 * heightRatio);
            hourMatrix.postTranslate(centerX - hour.getWidth() / 2, centerY - hour.getHeight() + 52 * heightRatio);




            switch (gear_speed){
                case "1":
                    secondRadius = secondRadius*1;
                    Log.d("case:",gear_speed);
                    break;
                case "2":
                    secondRadius = (float) (secondRadius*1.5);
                    Log.d("case:",gear_speed);
                    break;
                case "3":
                    secondRadius = secondRadius*2;
                    Log.d("case:",gear_speed);
                    break;
                default:
                    secondRadius = secondRadius*3;
                    Log.d("case:",gear_speed);
            }

            bottomGear1Matrix.setRotate((float) (-secondRadius * 7), (float) (bottomGear1.getWidth() / 2 + 75.3 * widthRatio), (float) (bottomGear1.getHeight() / 2 - 66.5 * heightRatio));
            bottomGear1Matrix.postTranslate(centerX - bottomGear1.getWidth() / 2, centerY - bottomGear1.getHeight() / 2);

            bottomGear2Matrix.setRotate((float) (secondRadius * 6), (float) (bottomGear2.getWidth() / 2 - 77.5 * widthRatio), (float) (bottomGear2.getHeight() / 2 + 61 * heightRatio));
            bottomGear2Matrix.postTranslate(centerX - bottomGear2.getWidth() / 2, centerY - bottomGear2.getHeight() / 2);

            bottomGear3Matrix.setRotate((float) (secondRadius * 3), (float) (bottomGear3.getWidth() / 2 + 16.1 * widthRatio), (float) (bottomGear3.getHeight() / 2 + 10.5 * heightRatio));
            bottomGear3Matrix.postTranslate(centerX - bottomGear3.getWidth() / 2, centerY - bottomGear3.getHeight() / 2);

            bottomGear4Matrix.setRotate((float) (-secondRadius * 3), (float) (bottomGear4.getWidth() / 2 - 77 * widthRatio), (float) (bottomGear4.getHeight() / 2 - 15.6 * heightRatio));
            bottomGear4Matrix.postTranslate(centerX - bottomGear4.getWidth() / 2, centerY - bottomGear4.getHeight() / 2);

            gear1Matrix.setRotate((float) (-secondRadius  * (23.0 / 12.0)), gear1.getWidth() / 2 + 40 * widthRatio, gear1.getHeight() / 2 + 60 * heightRatio);
            gear1Matrix.postTranslate(centerX - gear1.getWidth() / 2, centerY - gear1.getHeight() / 2);

            gear2Matrix.setRotate(secondRadius , gear2.getWidth() / 2 - 23 * widthRatio, gear2.getHeight() / 2 - 30 * heightRatio);
            gear2Matrix.postTranslate(centerX - gear2.getWidth() / 2, centerY - gear2.getHeight() / 2);

            PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
            canvas.setDrawFilter(pfd);

            canvas.drawRect(bounds, mBgPaint);


            if(!mIsRound){
                ambientBackground = ambientSquare;
                background = squareBackground;
                topWrapper = squareWrapper;

            }else{
                ambientBackground = ambientRound;
                background = roundBackground;
                topWrapper = roundWrapper;
            }




            if(shouldTimerBeRunning()){

                canvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), bounds, null);
                canvas.drawBitmap(bottomGear1, bottomGear1Matrix, null);
                canvas.drawBitmap(bottomGear2, bottomGear2Matrix, null);
                canvas.drawBitmap(bottomGear3, bottomGear3Matrix, null);
                canvas.drawBitmap(bottomGear4, bottomGear4Matrix, null);


                canvas.drawBitmap(gear1, gear1Matrix, null);
                canvas.drawBitmap(gear2,gear2Matrix,null);

                canvas.drawBitmap(gearWrapper, new Rect(0, 0, gearWrapper.getWidth(), gearWrapper.getHeight()), bounds, null);
                canvas.drawBitmap(topWrapper, new Rect(0, 0, topWrapper.getWidth(), topWrapper.getHeight()), bounds, null);

                if(sector_checked.equals("1")){
                    canvas.drawBitmap(secondWrapper,secondMatrix,null);
                }

                canvas.drawBitmap(minute,minuteMatrix,null);
                canvas.drawBitmap(hour,hourMatrix,null);


            }else{

                canvas.drawBitmap(ambientBackground, new Rect(0, 0, background.getWidth(), background.getHeight()), bounds, null);
//                canvas.drawBitmap(ambientMinute,ambientMinuteMatrix,null);
//                canvas.drawBitmap(ambientHour,ambientHourMatrix,null);

                /*------------------------always-on-state-------------------------------*/


            }

            canvas.drawBitmap(minute,minuteMatrix,null);
            canvas.drawBitmap(hour,hourMatrix,null);

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            mTime.setToNow();
            invalidate();
        }

        /** Handler to update the time once a second in interactive mode. */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        mIndex = ++mIndex;
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };



        final Handler sendGAHandler = new Handler(){
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:


                        long timeMs = System.currentTimeMillis();
                        long delayMs = 3600000
                                - (timeMs % 3600000);
                        GlobalWearApiHelper.getInstance(manService.this).sendScreenView("man watchface using per hour");
                        sendGAHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);

                        break;
                }
            }
        };

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();



            } else {
                unregisterReceiver();


            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();

        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            manService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            manService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }


        private void GATimer(){
            sendGAHandler.removeMessages(MSG_UPDATE_TIME);
            sendGAHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }




}
