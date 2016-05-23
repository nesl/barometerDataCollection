package ucla.nesl.pressuresensing2;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST_LOCATION = 0x1;
    private static final int MY_PERMISSION_REQUEST_READ_FILE = 0x2;
    private static final int MY_PERMISSION_REQUEST_WRITE_FILE = 0x4;
    private static final int MY_PERMISSION_REQUEST_WAKE_LOCK = 0x8;
    private static final int MY_PERMISSION_REQUEST_ALL = 0xf;

    private int currentPermission = 0x0;

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

    private PrintWriter loggerEvent;

    private ISensingService sensingService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // permission checking
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            currentPermission |= MY_PERMISSION_REQUEST_LOCATION;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            currentPermission |= MY_PERMISSION_REQUEST_READ_FILE;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_READ_FILE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            currentPermission |= MY_PERMISSION_REQUEST_WRITE_FILE;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_WRITE_FILE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK)
                == PackageManager.PERMISSION_GRANTED) {
            currentPermission |= MY_PERMISSION_REQUEST_WAKE_LOCK;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WAKE_LOCK},
                    MY_PERMISSION_REQUEST_WAKE_LOCK);
        }

        checkAllPermissionsAndBindService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentPermission |= requestCode;
                }
            }
            case MY_PERMISSION_REQUEST_READ_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentPermission |= requestCode;
                }
            }
            case MY_PERMISSION_REQUEST_WRITE_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentPermission |= requestCode;
                }
            }
            case MY_PERMISSION_REQUEST_WAKE_LOCK: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentPermission |= requestCode;
                }
            }
        }
        checkAllPermissionsAndBindService();
    }

    private void checkAllPermissionsAndBindService() {
        if (currentPermission == MY_PERMISSION_REQUEST_ALL) {
            Intent intent = new Intent(this, SensingService.class);
            intent.setAction(ISensingService.class.getName());
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void renderUI() {
        String pathRoot = null;
        try {
            pathRoot = sensingService.getPathRoot();
            loggerEvent = new PrintWriter(pathRoot + ".event");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // UI
        LinearLayout la = (LinearLayout) findViewById(R.id.main);

        textFileName = TextViewBuf.createText(la, this, "File name: " + pathRoot + ".*");
        //TextViewBuf.createText(la, this, "");
        textBaro = TextViewBuf.createText(la, this, "");
        textBaroHz = TextViewBuf.createText(la, this, "");
        textAccX = TextViewBuf.createText(la, this, "");
        textAccY = TextViewBuf.createText(la, this, "");
        textAccZ = TextViewBuf.createText(la, this, "");
        textAccHz = TextViewBuf.createText(la, this, "");
        textGyroX = TextViewBuf.createText(la, this, "");
        textGyroY = TextViewBuf.createText(la, this, "");
        textGyroZ = TextViewBuf.createText(la, this, "");
        textGyroHz = TextViewBuf.createText(la, this, "");
        textMagX = TextViewBuf.createText(la, this, "");
        textMagY = TextViewBuf.createText(la, this, "");
        textMagZ = TextViewBuf.createText(la, this, "");
        textMagHz = TextViewBuf.createText(la, this, "");
        textGps = TextViewBuf.createText(la, this, "");
        //TextViewBuf.createText(la, this, "");
        textStorage = TextViewBuf.createText(la, this, "");
        textEvent = TextViewBuf.createText(la, this, "");
        //TextViewBuf.createText(la, this, "");

        for (int i = 0; i < 3; i++) {
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            ViewGroup.LayoutParams buttonLayoutParam = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
    protected void onDestroy() {
        super.onDestroy();
        try {
            loggerEvent.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private Handler frameUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                DisplayPack dp = sensingService.getDisplayPack();
                textBaro.setStr(dp.strBaro);
                textBaroHz.setStr(dp.strBaroHz);
                textAccX.setStr(dp.strAccX);
                textAccY.setStr(dp.strAccY);
                textAccZ.setStr(dp.strAccZ);
                textAccHz.setStr(dp.strAccHz);
                textGyroX.setStr(dp.strGyroX);
                textGyroY.setStr(dp.strGyroY);
                textGyroZ.setStr(dp.strGyroZ);
                textGyroHz.setStr(dp.strGyroHz);
                textMagX.setStr(dp.strMagX);
                textMagY.setStr(dp.strMagY);
                textMagZ.setStr(dp.strMagZ);
                textMagHz.setStr(dp.strMagHz);
                textGps.setStr(dp.strGps);
                TextViewBuf.update();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            sendEmptyMessageDelayed(0, 20);
        }
    };

    private Handler storageCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            StatFs fs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            double mb = (double) fs.getAvailableBytes() / 1024.0 / 1024.0;
            textStorage.setStr(String.format("Free space: %.1f MB remains", mb));
            sendEmptyMessageDelayed(0, 10 * 60 * 1000L);
        }
    };


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sensingService = ISensingService.Stub.asInterface(service);
            renderUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sensingService = null;
        }
    };

}
