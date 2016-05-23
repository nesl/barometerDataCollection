package ucla.nesl.pressuresensing2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by timestring on 5/20/16.
 */
public class DisplayPack implements Parcelable {
    public String strBaro;
    public String strBaroHz;
    public String strAccX;
    public String strAccY;
    public String strAccZ;
    public String strAccHz;
    public String strGyroX;
    public String strGyroY;
    public String strGyroZ;
    public String strGyroHz;
    public String strMagX;
    public String strMagY;
    public String strMagZ;
    public String strMagHz;
    public String strGps;
    
    public static final Parcelable.Creator<DisplayPack> CREATOR = new Parcelable.Creator<DisplayPack>() {

        @Override
        public DisplayPack createFromParcel(Parcel in) {
            return new DisplayPack(in);
        }

        @Override
        public DisplayPack[] newArray(int size) {
            return new DisplayPack[size];
        }
    };

    public DisplayPack() {
        strBaro   = "BARO value: --";
        strBaroHz = "BARO freq: -- Hz";
        strAccX   = "ACC x: --";
        strAccY   = "ACC y: --";
        strAccZ   = "ACC z: --";
        strAccHz  = "ACC freq: -- Hz";
        strGyroX  = "GYRO x: --";
        strGyroY  = "GYRO y: --";
        strGyroZ  = "GYRO z: --";
        strGyroHz = "GYRO freq: -- Hz";
        strMagX   = "MAG x: --";
        strMagY   = "MAG y: --";
        strMagZ   = "MAG z: --";
        strMagHz  = "MAG freq: --";
        strGps    = "GPS from gps: --  from network: --";
    }
    
    public DisplayPack(Parcel in) {
        readFromParcel(in);
    }
    
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(strBaro);
        out.writeString(strBaroHz);
        out.writeString(strAccX);
        out.writeString(strAccY);
        out.writeString(strAccZ);
        out.writeString(strAccHz);
        out.writeString(strGyroX);
        out.writeString(strGyroY);
        out.writeString(strGyroZ);
        out.writeString(strGyroHz);
        out.writeString(strMagX);
        out.writeString(strMagY);
        out.writeString(strMagZ);
        out.writeString(strMagHz);
        out.writeString(strGps);
    }
    
    public void readFromParcel(Parcel in) {
        strBaro   = in.readString();
        strBaroHz = in.readString();
        strAccX   = in.readString();
        strAccY   = in.readString();
        strAccZ   = in.readString();
        strAccHz  = in.readString();
        strGyroX  = in.readString();
        strGyroY  = in.readString();
        strGyroZ  = in.readString();
        strGyroHz = in.readString();
        strMagX   = in.readString();
        strMagY   = in.readString();
        strMagZ   = in.readString();
        strMagHz  = in.readString();
        strGps    = in.readString();
    }
}
