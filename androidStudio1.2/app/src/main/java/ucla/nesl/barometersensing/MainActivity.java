package ucla.nesl.barometersensing;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;


public class MainActivity extends Activity implements SensorEventListener, LocationListener {
    private String[] textOfButtons = {"Window open", "Window close", "AC on", "AC off", "Door on", "Door off"};

    private TextViewBuf textFileName;
    private TextViewBuf textBaro;
    private TextViewBuf textBaroHz;
    private TextViewBuf textAccX;
    private TextViewBuf textAccY;
    private TextViewBuf textAccZ;
    private TextViewBuf textAccHz;
    private TextViewBuf textGyroX;
    private TextViewBuf textGyroY;
    private TextViewBuf textGyroZ;
    private TextViewBuf textGyroHz;
    private TextViewBuf textMagX;
    private TextViewBuf textMagY;
    private TextViewBuf textMagZ;
    private TextViewBuf textMagHz;
    private TextViewBuf textGps;
    private TextViewBuf textStorage;
    private TextViewBuf textEvent;

    private String deviceNo;

    private PrintWriter loggerBaro;
    private PrintWriter loggerAcc;
    private PrintWriter loggerGyro;
    private PrintWriter loggerMag;
    private PrintWriter loggerGps;
    private PrintWriter loggerEvent;

    private String offsetFileName = null;

    private TimeString timeString = new TimeString();

    private long sensorStartTime = 0;     // start timestamp of motion sensor
    private long sensorStartTimeAbs = 0;  // start system timestamp when first sensor reading comes
    private long gpsStartTime = 0;      // start timestamp of gps sensor
    private long gpsStartTimeAbs = 0;   // start system timestamp when first sensor reading comes
    private int baroCnt = 0;
    private int accCnt = 0;
    private int gyroCnt = 0;
    private int magCnt = 0;
    private int gpsGCnt = 0;
    private int gpsNCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // log file
        deviceNo = getDeviceNo();
        String filePrefix = "/baro_" + deviceNo + "_" + timeString.currentTimeForFile();
        String pathRoot = Environment.getExternalStorageDirectory() + filePrefix;
        try {
            loggerBaro = new PrintWriter(pathRoot + ".baro");
            loggerAcc = new PrintWriter(pathRoot + ".acc");
            loggerGyro = new PrintWriter(pathRoot + ".gyro");
            loggerMag = new PrintWriter(pathRoot + ".mag");
            loggerGps = new PrintWriter(pathRoot + ".gps");
            loggerEvent = new PrintWriter(pathRoot + ".event");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        offsetFileName = pathRoot + ".offset";

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_FASTEST);

        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);

        Sensor magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_FASTEST);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag").acquire();


        // UI
        LinearLayout la = (LinearLayout) findViewById(R.id.main);


        textFileName = TextViewBuf.createText(la, this, "File name: " + filePrefix + ".*");
        //TextViewBuf.createText(la, this, "");
        textBaro = TextViewBuf.createText(la, this, "BARO value: --");
        textBaroHz = TextViewBuf.createText(la, this, "BARO freq: -- Hz");
        textAccX = TextViewBuf.createText(la, this, "ACC x: --");
        textAccY = TextViewBuf.createText(la, this, "ACC y: --");
        textAccZ = TextViewBuf.createText(la, this, "ACC z: --");
        textAccHz = TextViewBuf.createText(la, this, "ACC freq: -- Hz");
        textGyroX = TextViewBuf.createText(la, this, "GYRO x: --");
        textGyroY = TextViewBuf.createText(la, this, "GYRO y: --");
        textGyroZ = TextViewBuf.createText(la, this, "GYRO z: --");
        textGyroHz = TextViewBuf.createText(la, this, "GYRO freq: -- Hz");
        textMagX = TextViewBuf.createText(la, this, "MAG x: --");
        textMagY = TextViewBuf.createText(la, this, "MAG y: --");
        textMagZ = TextViewBuf.createText(la, this, "MAG z: --");
        textMagHz = TextViewBuf.createText(la, this, "MAG freq: --");
        textGps = TextViewBuf.createText(la, this, "GPS from gps: --  from network: --");
        //TextViewBuf.createText(la, this, "");
        textStorage = TextViewBuf.createText(la, this, "Storage: --");
        textEvent = TextViewBuf.createText(la, this, "");
        //TextViewBuf.createText(la, this, "");

        for (int i = 0; i < 3; i++) {
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams buttonLayoutParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            buttonLayout.setLayoutParams(buttonLayoutParam);

            for (int j = 0; j < 2; j++) {
                int idx = i * 2 + j;
                Button btn = new Button(this);
                btn.setText(textOfButtons[idx]);
                btn.setOnClickListener(new EventButtonListener(idx, textOfButtons[idx], loggerEvent, textEvent));
                buttonLayout.addView(btn);
            }
            la.addView(buttonLayout);
        }


        frameUpdateHandler.sendEmptyMessage(0);
        storageCheckHandler.sendEmptyMessage(0);
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //sensorManager.unregisterListener(this);
        try {
            loggerBaro.close();
            loggerAcc.close();
            loggerGyro.close();
            loggerMag.close();
            loggerGps.close();
            loggerEvent.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorStartTimeAbs == 0) {
            sensorStartTimeAbs = System.currentTimeMillis();
            sensorStartTime = event.timestamp;
            saveOffset();
        }
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PRESSURE: {
                long timestamp = event.timestamp;
                float value = event.values[0];
                baroCnt++;
                textBaro.setStr("BARO value: " + value);
                textBaroHz.setStr("BARO freq: " + freqStrForSensor(timestamp, baroCnt));
                loggerBaro.println(timestamp + "," + value);
                loggerBaro.flush();
            }
            break;
            case Sensor.TYPE_ACCELEROMETER: {
                long timestamp = event.timestamp;
                float valueX = event.values[0];
                float valueY = event.values[1];
                float valueZ = event.values[2];
                accCnt++;
                textAccX.setStr("ACC x: " + valueX);
                textAccY.setStr("ACC y: " + valueY);
                textAccZ.setStr("ACC z: " + valueZ);
                textAccHz.setStr("ACC freq: " + freqStrForSensor(timestamp, accCnt));
                loggerAcc.println(timestamp + "," + valueX + "," + valueY + "," + valueZ);
                loggerAcc.flush();
            }
            break;
            case Sensor.TYPE_GYROSCOPE: {
                long timestamp = event.timestamp;
                float valueX = event.values[0];
                float valueY = event.values[1];
                float valueZ = event.values[2];
                gyroCnt++;
                textGyroX.setStr("GYRO x: " + valueX);
                textGyroY.setStr("GYRO y: " + valueY);
                textGyroZ.setStr("GYRO z: " + valueZ);
                textGyroHz.setStr("GYRO freq: " + freqStrForSensor(timestamp, gyroCnt));
                loggerGyro.println(timestamp + "," + valueX + "," + valueY + "," + valueZ);
                loggerGyro.flush();
            }
            break;
            case Sensor.TYPE_MAGNETIC_FIELD: {
                long timestamp = event.timestamp;
                float valueX = event.values[0];
                float valueY = event.values[1];
                float valueZ = event.values[2];
                magCnt++;
                textMagX.setStr("MAG x: " + valueX);
                textMagY.setStr("MAG y: " + valueY);
                textMagZ.setStr("MAG z: " + valueZ);
                textMagHz.setStr("MAG freq: " + freqStrForSensor(timestamp, magCnt));
                loggerMag.println(timestamp + "," + valueX + "," + valueY + "," + valueZ);
                loggerMag.flush();
            }
            break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.i("GPS", location.getTime() + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + location.getProvider());
        if (gpsStartTimeAbs == 0) {
            gpsStartTimeAbs = System.currentTimeMillis();
            gpsStartTime = location.getTime();
            saveOffset();
        }
        int gpsType = (location.getProvider().equals(LocationManager.GPS_PROVIDER)) ? 0 : 1;
        if (gpsType == 0)
            gpsGCnt++;
        else
            gpsNCnt++;
        loggerGps.println(location.getTime() + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + location.getAccuracy() + "," + location.getSpeed() + "," + gpsType);
        loggerGps.flush();
        textGps.setStr("GPS from gps:" + gpsGCnt + "  from network:" + gpsNCnt);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    private String getDeviceNo() {
        String fileName = Environment.getExternalStorageDirectory() + "/nesldev";
        try {
            FileInputStream fis = new FileInputStream(new File(fileName));
            Scanner scanner = new Scanner(fis);
            String firstLine = scanner.nextLine();
            firstLine.trim();
            fis.close();
            return firstLine;
        } catch (Exception e) {
            //Log.e("ERROR", "what", e);
        }
        return "x";
    }

    private String freqStrForSensor(long curSensorTimestamp, int count) {
        long dt = (curSensorTimestamp - sensorStartTime) / 1000000;  // result in milli-second
        //Log.i("DEBUG", curSensorTimestamp + " " + sensorStartTime);
        return String.format("%.2f", ((double) count) / (dt / 1e3)) + "  (" + count + " / " + timeString.ms2watch(dt) + ")";
    }

    private Handler frameUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TextViewBuf.update();
            sendEmptyMessageDelayed(0, 20);
        }
    };

    private Handler storageCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // this is also wrong....
            sendEmptyMessageDelayed(0, 10 * 60 * 1000L);
        }
    };


    private void saveOffset() {
        try {
            PrintWriter writer = new PrintWriter(offsetFileName);
            writer.println((double)sensorStartTime / 1e9 - (double)sensorStartTimeAbs / 1e3);
            writer.println((double)gpsStartTime / 1e3 - (double)gpsStartTimeAbs / 1e3);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
