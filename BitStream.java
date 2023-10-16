import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;



public class BitStream {

    private static RandomAccessFile raf;
    private int buffer;
    private int index;
    private int deadBitCount;
    public boolean eof;

    public BitStream(String fileName) throws IOException{

        raf = new RandomAccessFile(fileName, "rw");
        if(raf.length() == 0) raf.write(0);
        deadBitCount = raf.read();

        index = 7;
        buffer = 0;
        eof = false;
    }


    public void seek(long bits) throws IOException{

        bits += 8;
        long pos = bits/8;
        int index = (int)bits%8;

        if(pos >= raf.length() - 1){
            if(pos == raf.getFilePointer()) pos--;
            if(index <= deadBitCount) index = deadBitCount;
        }

        raf.seek(pos);
        fill();
        raf.seek(pos);
        this.index = index;

    }

    public boolean read() throws IOException{
        boolean resp;

        if(index < 0) fill();

        resp = (buffer >> index & 1) == 1;
        index--;
        return resp;
        
    }

    private int setBit(int pos, int target){
        return target | (1 << pos);
    }


    public void write(boolean x) throws IOException{

        buffer = (x) ? setBit(index, buffer) : buffer;
        index--;

        if (index < 0){
            raf.write(buffer);
            index = 7;
            buffer = 0;
        }
    }

    public void close() throws IOException{

        raf.write(buffer);

        if(raf.getFilePointer() == raf.length()){
            raf.seek(0);
            raf.write(index);
        }
        
        buffer = 0;
    }
    

    private void fill() throws IOException{
        
        if(eof) throw new EOFException("end of file");
        
        if(raf.length() == raf.getFilePointer()) {
            index = deadBitCount;
            eof = true;
        }else{
            index = 7;
        }
        
        buffer = raf.read();   

    }

    public long length() throws IOException{
        return ((raf.length() - 2)*8) + deadBitCount;
    }

    public long getFilePointer() throws IOException{
        return ((raf.getFilePointer() - 1)*8) - index;
    }
    

}
