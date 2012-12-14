package uk.me.jandj.runPerl;

import android.util.Log;
import java.lang.Thread;
import java.io.InputStream;

public class StreamLoggerThread extends Thread {
    InputStream in;
    String tag;
    int prio;

    public StreamLoggerThread(InputStream to_be_in, String to_be_tag, int to_be_prio) {
        in = to_be_in;
        tag = to_be_tag;
        prio = to_be_prio;
    }

    public void run() {
        // http://developer.android.com/reference/java/lang/Thread.html
        // http://developer.android.com/reference/java/io/InputStream.html
        // http://developer.android.com/reference/android/util/Log.html
        StringBuilder buffer = new StringBuilder();
        while (true) {
            int byte_read;
            try {
                byte_read = in.read();
            } catch (java.io.IOException e) {
                Log.e(tag, "Caught exception reading from perl: ", e);
                return;
            }
                
            //Log.println(prio, tag, "read byte " + byte_read);

            if (byte_read == 10) {
                // Newline, flush to log.
                Log.println(prio, tag, buffer.toString());
                buffer = new StringBuilder();
            } else if (byte_read == -1) {
                // The inputstream has reached eof, presumably because the interpreter process exited.  Suicide.
                if (buffer.length() > 0) {
                    Log.println(prio, tag, buffer.toString());
                }
                Log.println(prio, tag, "<stream closed>");
                break;
            } else {
                // A normal character, append it to our buffer.
                //Log.println(prio, tag, "captured byte");
                buffer.appendCodePoint(byte_read);
            }

            if (buffer.length() > 140) {
                // Don't let runaway lines happen, so if the buffer gets big, flush it even though there's no newline.  Twitter-length chosen completely arbitrarilly.
                Log.println(prio, tag, buffer.toString());
                buffer = new StringBuilder();
                
            }
        }

        try {
            in.close();        
        } catch (java.io.IOException e) {
            Log.e(tag, "Caught trying to close stream: ", e);
            return;
        }
    }
}