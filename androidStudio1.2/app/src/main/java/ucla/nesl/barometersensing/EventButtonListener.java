package ucla.nesl.barometersensing;

import android.view.View;

import java.io.PrintWriter;

/**
 * Created by timestring on 2/26/15.
 */
public class EventButtonListener implements View.OnClickListener {
    private int eventNo;
    private String msg;
    private PrintWriter writer;
    private TextViewBuf textViewBuf;

    public EventButtonListener(int _eventNo, String _appearMsg, PrintWriter _writer, TextViewBuf _targetTextViewBuf) {
        eventNo = _eventNo;
        msg = _appearMsg;
        writer = _writer;
        textViewBuf = _targetTextViewBuf;
    }

    @Override
    public void onClick(View view) {
        try {
            long time = System.currentTimeMillis();
            writer.println(time + "," + eventNo);
            writer.flush();
            textViewBuf.setStr("Event at " + time + ": " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
