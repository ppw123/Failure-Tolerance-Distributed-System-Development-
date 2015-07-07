public class ReadWriteLock {
    private int readingReaders=0;
    private int waitingWriters=0;
    private int writingWriters=0;
    private boolean preferWriter=true;
             

    public synchronized void readLock()
    {
        while(writingWriters>0||(preferWriter&&waitingWriters>0))
        {
        	try {
        		wait();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        readingReaders++;
    }
             
    public synchronized void readUnlock()
    {
        readingReaders--;
        preferWriter=true;
        notifyAll();
    }
             

    public synchronized void writeLock()
    {
        waitingWriters++;
        try{
            while(readingReaders>0||writingWriters>0)
            {
            	try {
            		wait();
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }
        }finally{
            waitingWriters--;
        }
        writingWriters++;
    }

    public synchronized void writeUnlock()
    {
        writingWriters--;
        preferWriter=false;
        notifyAll();
    }
}
