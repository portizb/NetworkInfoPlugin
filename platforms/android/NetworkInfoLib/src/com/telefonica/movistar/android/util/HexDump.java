package com.telefonica.movistar.android.util;

/**
 * Created by pablo on 2/09/16.
 */
public class HexDump {

    private final static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String dumpHexString (byte[] array)
    {
        return dumpHexString(array, 0, array.length);
    }

    public static String dumpHexString (byte[] array, int offset, int length)
    {
        StringBuilder result = new StringBuilder();

        byte[] line = new byte[16];
        int lineIndex = 0;

        result.append("\n0x");
        result.append(toHexString(offset));

        return result.toString();
    }

    public static String toHexString(byte b)
    {
        return toHexString(toByteArray(b));
    }

    public static String toHexString(byte[] array)
    {
        return toHexString(array, 0, array.length);
    }

    public static String toHexString(byte[] array, int offset, int length)
    {
        char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset ; i < offset + length; i++)
        {
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(int i) {
        return toHexString(toByteArray(i));
    }


    public static byte[] toByteArray(byte b)
    {
        byte[] array = new byte[1];
        array[0] = b;
        return array;
    }

    public static byte[] toByteArray(int i)
    {
        byte[] array = new byte[4];

        array[3] = (byte)(i & 0xFF);
        array[2] = (byte)((i >> 8) & 0xFF);
        array[1] = (byte)((i >> 16) & 0xFF);
        array[0] = (byte)((i >> 24) & 0xFF);

        return array;
    }
}
