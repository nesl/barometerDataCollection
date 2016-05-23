package ucla.nesl.pressuresensing2;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class SensingService extends Service implements SensorEventListener, LocationListener {
    private String pathRoot;

    private PrintWriter loggerBaro;
    private PrintWriter loggerAcc;
    private PrintWriter loggerGyro;
    private PrintWriter loggerMag;
    private PrintWriter loggerGps;

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

    private DisplayPack displayPack = new DisplayPack();

    private LocationManager locationManager;

    @Override
    public IBinder onBind(Intent intent) {
        // log file
        String deviceNo = getDeviceNo();
        String filePrefix = "/baro_" + deviceNo + "_" + timeString.currentTimeForFile();
        pathRoot = Environment.getExternalStorageDirectory() + filePrefix;
        try {
            loggerBaro = new PrintWriter(pathRoot + ".baro");
            loggerAcc = new PrintWriter(pathRoot + ".acc");
            loggerGyro = new PrintWriter(pathRoot + ".gyro");
            loggerMag = new PrintWriter(pathRoot + ".mag");
            loggerGps = new PrintWriter(pathRoot + ".gps");
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag").acquire();

        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //sensorManager.unregisterListener(this);
        try {
            loggerBaro.close();
            loggerAcc.close();
            loggerGyro.close();
            loggerMag.close();
            loggerGps.close();
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
                displayPack.strBaro = "BARO value: " + value;
                displayPack.strBaroHz = "BARO freq: " + freqStrForSensor(timestamp, baroCnt);
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
                displayPack.strAccX = "ACC x: " + valueX;
                displayPack.strAccY = "ACC y: " + valueY;
                displayPack.strAccZ = "ACC z: " + valueZ;
                displayPack.strAccHz = "ACC freq: " + freqStrForSensor(timestamp, accCnt);
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
                displayPack.strGyroX = "GYRO x: " + valueX;
                displayPack.strGyroY = "GYRO y: " + valueY;
                displayPack.strGyroZ = "GYRO z: " + valueZ;
                displayPack.strGyroHz = "GYRO freq: " + freqStrForSensor(timestamp, gyroCnt);
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
                displayPack.strMagX = "MAG x: " + valueX;
                displayPack.strMagY = "MAG y: " + valueY;
                displayPack.strMagZ = "MAG z: " + valueZ;
                displayPack.strMagHz = "MAG freq: " + freqStrForSensor(timestamp, magCnt);
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
        displayPack.strGps = "GPS from gps:" + gpsGCnt + "  from network:" + gpsNCnt;
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
        return String.format("%.2f  (%d / %s)", ((double) count) / (dt / 1e3), count, timeString.ms2watch(dt));
    }

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

    private final ISensingService.Stub mBinder = new ISensingService.Stub() {
        @Override
        public DisplayPack getDisplayPack() throws RemoteException {
            return displayPack;
        }

        @Override
        public String getPathRoot() throws RemoteException {
            return pathRoot;
        }
    };

}
