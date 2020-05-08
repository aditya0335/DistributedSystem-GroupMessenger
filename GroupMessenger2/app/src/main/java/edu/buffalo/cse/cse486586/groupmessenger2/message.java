package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;
import java.util.Comparator;

public class message implements Serializable {
    String message;
    String portno;
    int avd_id;
    double local_no;
    double global_no;
    boolean deliverable;
    double total;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    long time;

    message(String message, String portno, double local_no, boolean deliverable) {
        this.message = message;
        this.portno = portno;
        if (portno.compareTo("11108")==0) {
            this.avd_id = 0;
        } else if (portno.compareTo("11112")==0) {
            this.avd_id = 1;
        } else if (portno.compareTo("11116")==0) {
            this.avd_id = 2;
        } else if (portno.compareTo("11120")==0) {
            this.avd_id = 3;
        } else if(portno.compareTo("11124")==0){
            this.avd_id = 4;
        }
        this.local_no = local_no;  //setting sequence of all the messeages sent by avd for FIFO
        this.deliverable = deliverable;


    }


    public String getMessage() {
        return message;
    }

    public String getPortno() {
        return portno;
    }

    public double getLocal_no() {
        return local_no;
    }

    public boolean isDeliverable() {
        return deliverable;
    }

    public void setDeliverable(boolean deliverable) {
        this.deliverable = deliverable;
    }

    public void setGlobal_no(double global_no) {
        this.global_no = global_no;
    }

    public double getGlobal_no() {
        return global_no;
    }

    public int getAvd_id() {
        return avd_id;
    }

    public void setTotal(double total) {
        this.total = total;
    }



    public double getTotal() {
        return total;
    }


//    @Override
//    public int compareTo(message another) {
//        return (int)(this.total-another.total);
//    }



}
