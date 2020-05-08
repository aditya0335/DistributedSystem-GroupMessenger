package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import static edu.buffalo.cse.cse486586.groupmessenger1.GroupMessengerProvider.TAG;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String[] REMOTE_PORT = {"11108","11112","11116","11120","11124"};
    static final String TAG =GroupMessengerActivity.class.getName();
    int count=0;
    int loop=0;

    static final int SERVER_PORT = 10000;
    int get_count()
    {
        return count++;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));



        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */


//        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//
//        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
//
//        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        final View button =  findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText editText1 = (EditText) findViewById(R.id.editText1);
                String msg = editText1.getText().toString() + "\n";
                editText1.setText("");
//                String msg = v.toString();
                System.out.println(msg);


                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);
            }
            // Code here executes on main thread after user presses button

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        private static final String KEY_FIELD = "key";
        private static final String VALUE_FIELD = "value";

        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }



        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            try
            {
                while (true)
                {Socket clientSocket = serverSocket.accept();
                    DataInputStream dins=new DataInputStream(clientSocket.getInputStream());
                    String  str=(String)dins.readUTF();
                    publishProgress(str);
//                clientSocket.close();
                }
            }
            catch(Exception e){}


            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            return null;
        }




        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");


            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
            int count1=get_count();
            String countstr=Integer.toString(count1);
            final ContentResolver mContentResolver=getContentResolver();
            final Uri mUri= buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
            ContentValues keyValuetoInsert=new ContentValues();
            keyValuetoInsert.put("key",countstr);
            keyValuetoInsert.put("value",strReceived);
            mContentResolver.insert(mUri, keyValuetoInsert);
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {
//        String[] remotePort=null;
//        Socket[] socket=null;
//        DataOutputStream[] dost=null;


        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String msgToSend = msgs[0];

                String[] remotePort = new String[]{REMOTE_PORT[0],REMOTE_PORT[1],REMOTE_PORT[2],REMOTE_PORT[3],REMOTE_PORT[4]};


                Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort[0]));
                DataOutputStream dost0 = new DataOutputStream(socket0.getOutputStream());
                dost0.writeUTF(msgToSend);

//                    dost0.flush();
//                socket0.close();

                Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort[1]));
                DataOutputStream dost1 = new DataOutputStream(socket1.getOutputStream());
                dost1.writeUTF(msgToSend);
//
//                    dost1.flush();
//                socket1.close();


                Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort[2]));
                DataOutputStream dost2 = new DataOutputStream(socket2.getOutputStream());
                dost2.writeUTF(msgToSend);

//                    dost2.flush();
//                    socket2.close();


                Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort[3]));
                DataOutputStream dost3 = new DataOutputStream(socket3.getOutputStream());
                dost3.writeUTF(msgToSend);

//                    dost3.flush();
//                socket3.close();

                Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort[4]));
                DataOutputStream dost4 = new DataOutputStream(socket4.getOutputStream());
                dost4.writeUTF(msgToSend);

//                    dost4.flush();
//                socket4.close();




                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
//            socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }
}
