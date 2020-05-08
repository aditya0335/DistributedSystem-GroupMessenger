package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

import static edu.buffalo.cse.cse486586.groupmessenger2.GroupMessengerProvider.TAG;



/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 */


public class GroupMessengerActivity extends Activity {
    static final String[] REMOTE_PORT = {"11108", "11112", "11116", "11120", "11124"};

    ArrayList<String> PORTS=new ArrayList<String>() {{    //For accssing ports
        add("11108");
        add("11112");
        add("11116");
        add("11120");
        add("11124");
    }};

    static final int SERVER_PORT = 10000;

    static final String TAG = GroupMessengerActivity.class.getName();

    String myPort = null;

    double local_count = 0;   //For client proposed priority

    double getLocal_count() {
        return local_count;
    }

    public void setLocal_count(double local_count) {
        this.local_count = local_count;
    }

    double process_count = 0;  //For server proposed priority

    double getProcess_count() {
        return process_count;
    }

    public void setProcess_count(double process_count) {
        this.process_count = process_count;
    }

    int count = 0;  //For content provider key

    int get_count() {
        return count++;
    }

    PriorityBlockingQueue<message> queue = new
            PriorityBlockingQueue<message>(10, new messagecomparator());



    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);


        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());


        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        Log.d(TAG, "Running Running");
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        Log.d(TAG, "AVD- " + myPort + "OnCreate Process LogNo-1");


        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.d(TAG, "AVD- " + myPort + "OnCreate Process ErrorLogNo-1 " + "can't create ServerSocket");
            e.printStackTrace();
        }


        final View button = findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText editText1 = (EditText) findViewById(R.id.editText1);
                String msg = editText1.getText().toString() + "\n";
                editText1.setText("");
                Log.d(TAG, "AVD- " + myPort + "OnCreate Process LogNo-2 " + "sending message to client- " + msg);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
            // Code here executes on main thread after user presses button

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                                   //Not in use method
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }



    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {         //Server Class

        private static final String KEY_FIELD = "key";
        private static final String VALUE_FIELD = "value";


        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        double id = 0;

        @Override
        protected Void doInBackground(ServerSocket... serverSockets) {

            ServerSocket serverSocket = serverSockets[0];

            if (myPort.compareTo("11108") == 0) {
                id = 0;
            } else if (myPort.compareTo("11112") == 0) {
                id = 1;
            } else if (myPort.compareTo("11116") == 0) {
                id = 2;
            } else if (myPort.compareTo("11120") == 0) {
                id = 3;
            } else if (myPort.compareTo("11124") == 0) {
                id = 4;
            }
            id= id*(0.1);
            Log.d(TAG,"ID is- "+ id);


            setProcess_count(getProcess_count() + id);

            ObjectInputStream ois_server;
            ObjectOutputStream oos_server;
            ObjectInputStream ois_server_secondary;

            while (true){

                Socket clientSocket = null;

                try {

                    clientSocket = serverSocket.accept();

                    InputStream is = clientSocket.getInputStream();  //Opening Input Stream
                    ois_server = new ObjectInputStream(is);

                    OutputStream os = clientSocket.getOutputStream();  //Opening Output Stream
                    oos_server = new ObjectOutputStream(os);

                    message servermsgnew = (message) ois_server.readObject();  //Reading first message from client

                    double proposed_priority = getProcess_count();  //Setting proposed priority to message
                    setProcess_count(getProcess_count() + 1);
                    servermsgnew.setTotal(proposed_priority);


                    queue.add(servermsgnew);                       //Adding message with false status to queue


                    oos_server.writeObject(servermsgnew);       //Sending message with proposed priority to client
                    oos_server.flush();


                    message servermsgsecondary = (message) ois_server.readObject();  //Reading final message from client

                    if ((int) servermsgsecondary.getTotal() > (int) getProcess_count()) {    //Setting priority of server according to priority of message recieved
                        setProcess_count((int) servermsgsecondary.getTotal() + id);
                    }

                    Iterator<message> queueIterator = queue.iterator();


                    while (queueIterator.hasNext()) {             //Replacing message with final priority message
                        message a = queueIterator.next();
                        if (a.getAvd_id() == servermsgsecondary.getAvd_id() && a.getLocal_no() == servermsgsecondary.getLocal_no()) {
                            queue.remove(a);

                            queue.add(servermsgsecondary);
                            break;
                        }
                    }

                    publishProgress(servermsgsecondary.getMessage());


//                    while (!queue.isEmpty() && queue.peek().deliverable == true) {
//
//                        publishProgress(queue.poll().getMessage());
//                    }



                }
                catch (OptionalDataException e){e.printStackTrace();}
                catch (StreamCorruptedException e){e.printStackTrace();}
                catch (SocketTimeoutException e){e.printStackTrace();}
                catch (EOFException e){e.printStackTrace();}
                catch (UnknownHostException e){e.printStackTrace();}
                catch (IOException e){e.printStackTrace();}
                catch (Exception e){e.printStackTrace();}

            }

        }

        protected void onProgressUpdate(String... strings) {

            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");


            int count1 = get_count();
            String countstr = Integer.toString(count1);
            final ContentResolver mContentResolver = getContentResolver();
            final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
            ContentValues keyValuetoInsert = new ContentValues();
            keyValuetoInsert.put("key", countstr);
            keyValuetoInsert.put("value", strReceived);
            mContentResolver.insert(mUri, keyValuetoInsert);
            return;
        }



    }



    private class ClientTask extends AsyncTask<String, Void, Void> {                //Client Class

        @Override
        protected Void doInBackground(String... strings) {

            Socket[] socket = new Socket[5];
            ObjectOutputStream[] oos = new ObjectOutputStream[5];
            ObjectInputStream[] ois = new ObjectInputStream[5];
            ArrayList<ObjectOutputStream> oos_list=new ArrayList<ObjectOutputStream>(5);



            ArrayList<message> recievedmessages = new ArrayList<message>();

            String msgToSend = strings[0];

            setLocal_count(getLocal_count() + 1);
            double object_index = getLocal_count();

            int index=10;

            for (int i = 0; i < PORTS.size(); i++) {
                try {
                    socket[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),  //Making connection with server
                            Integer.parseInt(PORTS.get(i)));


//                    socket[i].setSoTimeout(2000);
                    oos[i] = new ObjectOutputStream(socket[i].getOutputStream());  //Initializing Object Output stream

                    message msg_send = new message(msgToSend, strings[1], object_index, false);

                    oos[i].writeObject(msg_send);
                    oos[i].flush();

                    InputStream is = socket[i].getInputStream();         //Initializing Object Input stream
                    ois[i] = new ObjectInputStream(is);


                    recievedmessages.add((message) ois[i].readObject());
                    oos_list.add(oos[i]);



                } catch (OptionalDataException e) {
                    index = i;
                    e.printStackTrace();
                } catch (StreamCorruptedException e) {
                    index = i;
                    e.printStackTrace();
                } catch (SocketTimeoutException e) {
                    index = i;
                    e.printStackTrace();
                } catch (EOFException e) {
                    index = i;
                    e.printStackTrace();
                } catch (IOException e) {
                    index = i;
                    e.printStackTrace();
                } catch (Exception e) {
                    index = i;
                    e.printStackTrace();
                }
            }

//                if(index!=10)
//                {PORTS.remove(index);
//                oos_list.remove(index);}



                double[] totalordering = new double[recievedmessages.size()];

                for (int r = 0; r < recievedmessages.size(); r++) {


                    totalordering[r] = recievedmessages.get(r).getTotal();
                }

                double max = getLocal_count();
                for (int b = 0; b < totalordering.length; b++) {
                    if (totalordering[b] > max)
                        max = totalordering[b];
                }

                setLocal_count(max);

                recievedmessages.get(0).setGlobal_no(max);
                recievedmessages.get(0).setTotal(max);
                recievedmessages.get(0).setDeliverable(true);


            for (int i = 0; i < PORTS.size(); i++) {
                try{


                    message finalmessage = recievedmessages.get(0);


                    oos_list.get(i).writeObject(finalmessage);
                    oos_list.get(i).flush();
                    socket[i].close();

                }
                catch (OptionalDataException e) {
                    index = i;
                    e.printStackTrace();
                } catch (StreamCorruptedException e) {
                    index = i;
                    e.printStackTrace();
                } catch (SocketTimeoutException e) {
                    index = i;
                    e.printStackTrace();
                } catch (EOFException e) {
                    index = i;
                    e.printStackTrace();
                } catch (IOException e) {
                    index = i;
                    e.printStackTrace();
                } catch (Exception e) {
                    index = i;
                    e.printStackTrace();
                }
            }

//            if(index!=10)
//            {PORTS.remove(index);}


            return null;
        }
    }


//End of code


}
