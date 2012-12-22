package uk.me.jandj.runPerl;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.content.res.Resources;
import java.io.*;
// Yes, I know this is redundant.
import java.lang.Process;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import android.widget.Toast;
import uk.me.desert_island.theorbtwo.bridge.*;

public class RunPerlActivity extends JavaBridgeActivity
{

    // END IN A SLASH!
    public static String runtime_path = "/mnt/sdcard/uk.me.jandj.runPerl/";
    public static String libs_path = runtime_path + "extras/lib/";
    //public static String executable_path = "/data/data/uk.me.jandj.runPerl/perl";
    public static String script_path = runtime_path + "runperl.pl";
    private static String log_tag = "RunPerl";
    
    Process perlProcess;

    public StreamLoggerThread stdout_thread;
    public StreamLoggerThread stderr_thread;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		if (!Environment.getExternalStorageState().equals("mounted")) {
            Log.e(log_tag, "External storage is not mounted");
		  
            Toast toast = Toast.makeText( getApplicationContext(), "External storage not mounted", Toast.LENGTH_LONG);
            toast.show();
            return;
		}

        String abs_path = this.getFilesDir().getAbsolutePath();
        //String abs_path = runtime_path+"installed/";
        String executable_path = abs_path + "/perl/perl";
        Log.w(log_tag, "starting, doing boilerplate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.w(log_tag, "abs_path is "+abs_path);
        Log.w(log_tag, "executable_path is "+executable_path);

        // If our executable doesn't exist, we need to unzip our res file.
        try {
            doFirstTimeSetup();
        } catch(Exception e) {
            Log.e(log_tag, "Failed to extract Perl binary: ", e);
        }
        
        if (!(new File(executable_path).exists())) {
            Log.e(log_tag, "Perl binary seems to not exist");
        }

        Log.d(log_tag, "Starting executable in "+executable_path);
        ProcessBuilder proc_build = new ProcessBuilder(executable_path, script_path);
        proc_build.directory(new File(runtime_path));
        proc_build.environment().put("PERL5LIB", abs_path+"/perl/5.17.4:"+libs_path);

        Toast toast = Toast.makeText( getApplicationContext(), "Attempting to run run_perl.pl ... ", Toast.LENGTH_LONG);

        try {
            Log.w(log_tag, "Starting perl process");
            perlProcess = proc_build.start();
            Log.w(log_tag, "Started perl process");
        } catch(IOException e) {
            Log.e(log_tag, "Failed to start process: " + executable_path, e);
        }

        try {
            stdout_thread = 
                new StreamLoggerThread(
                                       perlProcess.getInputStream(),
                                       log_tag +"-stdout",
                                       android.util.Log.INFO);
            stdout_thread.start();
            
            stderr_thread =
                new StreamLoggerThread(
                                       perlProcess.getErrorStream(),
                                       log_tag +"-stderr",
                                       android.util.Log.ERROR);
            stderr_thread.start();


        } catch(Exception e) {
            Log.e(log_tag, "Failed to log STDOUT and STDERR streams: ", e);
        }
        /* finally {
            perlProcess.destroy();
        }
            */
    }

    private void doFirstTimeSetup() throws FileNotFoundException, IOException {
        //String abs_path = runtime_path+"installed/";
        String abs_path = this.getFilesDir().getAbsolutePath();
        String executable_path = abs_path + "/perl/perl";
        File executable = new File(executable_path);
        File runtime = new File(script_path);
        File test_lib = new File(libs_path+"Android.pm");
        // this isa Activity isa Context.
        Resources res = getResources();

        Toast toast = Toast.makeText( getApplicationContext(), "Extracting Perl all over the disk ... ", Toast.LENGTH_LONG);

        if (!executable.exists()) {

            // FIXME: Make version a variable.
            InputStream perl_zip = res.openRawResource(R.raw.perl_5_17_4);
            unzip(perl_zip, abs_path);
            // +x, for everyone
            executable.setExecutable(true, false);
        }
        // Unzip accompanying test dir.
        File extstorage = new File(Environment.getExternalStorageDirectory(), "/Perl");

        if(!runtime.exists()) {
            putFile(res.openRawResource(R.raw.runperl), runtime);
        }

        // Always unpack the libs
        unzip(res.openRawResource(R.raw.perl_libs), libs_path);
    }


    // "borrowed" from perl-android, com.android.perl.common.Utils.java
    public static void unzip(InputStream inputStream, String dest) throws FileNotFoundException, IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
        ZipEntry zipEntry;
		
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String zipEntryName = zipEntry.getName();
            
            // extract
            // We need a slash, but a double-slash is fine too.
            File file = new File(dest + "/" + zipEntryName);
            putFile(zipInputStream, file);
            
            // TODO perl special permissions ?
            //            Log.d(log_tag, "Unzip extracted " + file);
        }
		
        zipInputStream.close();

        
    }

    private static void putFile(InputStream input, File target)  throws FileNotFoundException, IOException {
        final int BUFFER_SIZE = 4096;

        if (!target.getParentFile().isDirectory()) {
            //            Log.w(log_tag, target + " 's parent isn't a directory, deleting it!");
            if (!target.getParentFile().delete()) {
                Log.e(log_tag, "Couldn't delete "+ target +"'s parent, which exists but isnt a directory" );
            }
        }

        if (!target.getParentFile().exists()) {
            if (!target.getParentFile().mkdirs()) {
                throw new IOException("putFile: Cannot make parent directory of target: " + target.getParentFile());
            }
            target.getParentFile().setExecutable(true, false);
            target.getParentFile().setReadable(true, false);
            target.getParentFile().setWritable(true, true);
        }
        
        //        Log.w(log_tag, "putFile("+target+")");

        if(target.exists()) {
            if(!target.delete()) {
                Log.w(log_tag, "putFile tried to delete " +target + ", but failed");
            }
        }
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(target));
        int count;
        byte buffer[] = new byte[BUFFER_SIZE];
        while ((count = input.read(buffer, 0, BUFFER_SIZE)) != -1) {
            writer.write(buffer, 0, count);
        }
        
        writer.flush();
        writer.close();
        
        // Make it rwxr-xr-x
        target.setExecutable(true, false);
        target.setReadable(true, false);
        target.setWritable(true, true);

    }
}
