package com.manish.redirectionjni;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;

/*
 * This application redirects the STDIN & STDOUT of C code segment running in JNI.
 * 
 * It creates 2 named pipes (FIFO), one for input and other for output.
 * 
 * It opens up one end of the pipe in write only mode in native code and other end of the pipe in read only mode in Java code. 
 * The file descriptor in native code is mapped to STDOUT i.e. 1, thereafter any writes to STDOUT in native code, will be 
 * redirected to other end of pipe which can read in Java code.
 * 
 * It opens up one end of the pipe in read only mode in native code and other end of the pipe in write only mode in Java code. 
 * The file descriptor in native code is mapped to STDIN i.e. 0, thereafter any writes to other end of pipe in Java code, will be
 * read by native code using STDIN.
 */
public class RedirectionJni extends Activity
{
	Handler mHandler;
	
	String mOutfile; // This file is used for STDOUT redirection. Java code will read content written by native code using STDOUT.
	String mInfile; // This file is used for STDIN redirection. Java code will write content which will be read by native code using STDIN.
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        File dir = getFilesDir();
        if(!dir.exists()) {
        	dir.mkdir();
        }
        
        mOutfile = this.getFilesDir() + "/out";
        mInfile = this.getFilesDir() + "/in";
        mHandler = new Handler();
        /* Create a TextView and set its content.
         * the text is retrieved by calling a native
         * function.
         */
        TextView  tv = new TextView(this);
		tv.setText("Click me to use stdin & stdout");
        setContentView(tv);
        
        new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				stringFromJNI(mOutfile, mInfile);
			}
        	
        }).start();
        
                
        tv.setOnClickListener(new View.OnClickListener() {
			
        	/*
        	 * This thread is used for reading content which will be written by native code using STDOUT.
        	 */
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						BufferedReader in = null;
						try {
							in = new BufferedReader(new FileReader(mOutfile));
							while(in.ready()) {
								final String str = in.readLine();
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(RedirectionJni.this, str, Toast.LENGTH_LONG).show();
									}
									
								});
							}
							in.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}).start();

				/*
				 * This thread is used for writing content which will be read by native code using STDIN.
				 */
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						BufferedWriter out = null;
						try {
							out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mInfile)));
							String content = "This content is written to " + mInfile;
							out.write(content.toCharArray(), 0, content.toCharArray().length);
							out.flush();
							out.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}).start();
								
			}
		});
    }

    /* A native method that is implemented by the
     * 'redirection-jni' native library, which is packaged
     * with this application.
     */
    public native String  stringFromJNI(String outfile, String infile);

    static {
        System.loadLibrary("redirection-jni");
    }
}
