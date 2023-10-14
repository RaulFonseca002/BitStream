import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;



public class BitStream {

    private static RandomAccessFile raf;
    private int buffer;
    private int index;
    private int deadBitCount;
    public boolean eof;

    public static void main(String[] args) throws IOException{

        BitStream bs = new BitStream("test.bin");
        
        for(int c = 0; c < (11*8) + 1; c++){
            bs.write(c%5 == 3);
        }

        bs.seek((0));
        for(int c = 0; c < 8; c++) bs.read();

        bs.seek((0));
        for(int c = 0; c < 8; c++) bs.write(true);

        bs.seek((0));
        for(int c = 0; c < 8; c++) bs.read();


        bs.close();

    }

    public BitStream(String fileName) throws IOException{

        raf = new RandomAccessFile(fileName, "rw");
        if(raf.length() == 0) raf.write(0);
        deadBitCount = raf.read();

        index = 7;
        buffer = 0;
        eof = false;
    }

    public void seek(long bits) throws IOException{

        bits += 8; // pular o header
        close();
        raf.seek(bits/8);
        fill();
        index = 7 - (int)bits%8;
        raf.seek(bits/8);
    }

    public boolean read() throws IOException{

        boolean resp;

        if(index < 0) fill();

        resp = (buffer >> index & 1) == 1;
        System.out.println(index + ": " + resp);
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
        System.out.println("buffer: " + buffer);;   

    }

    public long length() throws IOException{
        return ((raf.length() - 1)*8);
    }
    

}
