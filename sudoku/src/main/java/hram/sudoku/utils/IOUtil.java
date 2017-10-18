package hram.sudoku.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class IOUtil {
    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int l;
            byte[] data = new byte[1024];
            while ((l = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, l);
            }
            buffer.flush();
            return buffer.toByteArray();
        } finally {
            buffer.close();
        }
    }

    public static String InpitStreamToString(File file) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return InpitStreamToString(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    public static String InpitStreamToString(InputStream in) throws IOException {
        InputStreamReader is = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        String read = br.readLine();

        while (read != null) {
            //System.out.println(read);
            sb.append(read);
            read = br.readLine();

        }

        return sb.toString();
    }

    public static String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        reader.close();
        return stringBuilder.toString();
    }

    public static String readFile(InputStream in) throws IOException {
        InputStreamReader is = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(is);
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        reader.close();
        return stringBuilder.toString();
    }

    /**
     * ����������� double � ������ ����
     *
     * @param value
     * @return ������ ����
     */
    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    /**
     * ����������� ������ ���� � double
     *
     * @param bytes ������ ����
     * @return double �������� �������
     */
    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static int[] toIntArray(byte[] barr) {
        //Pad the size to multiple of 4 
        int size = (barr.length / 4) + ((barr.length % 4 == 0) ? 0 : 1);

        ByteBuffer bb = ByteBuffer.allocate(size * 4);
        bb.put(barr);

        //Java uses Big Endian. Network program uses Little Endian. 
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.rewind();
        IntBuffer ib = bb.asIntBuffer();
        int[] result = new int[size];
        ib.get(result);

        return result;
    }

    public static int[] toIntArray2(byte[] barr) {
        IntBuffer buffer = ByteBuffer.wrap(barr).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        int[] ints = new int[barr.length / 4];
        buffer.get(ints);
        return ints;
    }

    public static byte[] toByteArray(int[] array) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(array);

        return byteBuffer.array();
    }

    public static File[] FindFiles(String dirName, final String ext) {
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(ext);
            }
        });
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
