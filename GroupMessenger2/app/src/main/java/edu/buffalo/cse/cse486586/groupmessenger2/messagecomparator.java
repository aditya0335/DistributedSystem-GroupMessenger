package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

public class messagecomparator implements Comparator<message> {
    @Override
    public int compare(message m1,message m2) {
        if(m1.getTotal()>m2.getTotal())
            return 1;
        else if(m1.getTotal()<m2.getTotal())
            return -1;
        return 0;
    }
}
