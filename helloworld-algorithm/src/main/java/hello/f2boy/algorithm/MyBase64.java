package hello.f2boy.algorithm;

import sun.misc.BASE64Encoder;

import java.nio.charset.Charset;

public class MyBase64 {

    private static Charset charset = Charset.defaultCharset();

    private static final int MASK_8BITS = 0xff;

    private static final int MASK_6BITS = 0x3f;

    private static final byte PAD = '=';

    private static final byte[] ENCODE_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    private static final byte[] DECODE_TABLE = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            DECODE_TABLE[i] = -1;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            DECODE_TABLE[i] = (byte) (i - 'A');
        }
        for (int i = 'a'; i <= 'z'; i++) {
            DECODE_TABLE[i] = (byte) (26 + i - 'a');
        }
        for (int i = '0'; i <= '9'; i++) {
            DECODE_TABLE[i] = (byte) (52 + i - '0');
        }
        DECODE_TABLE['+'] = 62;
        DECODE_TABLE['/'] = 63;
    }

    public static String encode(String s) {
        byte[] b = s.getBytes(charset);
        int steps = b.length / 3;
        int mod = b.length % 3;

        int resultLength = steps * 4 + (mod == 0 ? 0 : 4);
        byte[] result = new byte[resultLength];

        // 先处理前面可以每4个数组元素接纳的内容
        for (int i = 0; i < steps; i++) {

            // 用一个int型（占4个字节）的低3位存储这3个字节
            byte b1 = b[i * 3];
            byte b2 = b[i * 3 + 1];
            byte b3 = b[i * 3 + 2];
            int x = ((int) b1 & MASK_8BITS) << 16;
            x |= ((int) b2 & MASK_8BITS) << 8;
            x |= ((int) b3 & MASK_8BITS);

            // 从低位开始，依次将每6个字节存入到要编码的字节数组
            result[i * 4 + 3] = ENCODE_TABLE[MASK_6BITS & x];
            x >>= 6;
            result[i * 4 + 2] = ENCODE_TABLE[MASK_6BITS & x];
            x >>= 6;
            result[i * 4 + 1] = ENCODE_TABLE[MASK_6BITS & x];
            x >>= 6;
            result[i * 4] = ENCODE_TABLE[MASK_6BITS & x];
        }

        // 多出一个字符，即8位，需要base64转换后的2个字节，base64后的数组最后2个补'='
        if (mod == 1) {
            byte b1 = b[b.length - 1];

            // 1个字节是8位，为了与2个6位对齐，要多左移4位
            int x = ((int) b1 & MASK_8BITS) << 4;
            result[resultLength - 3] = ENCODE_TABLE[MASK_6BITS & x];
            x >>= 6;
            result[resultLength - 4] = ENCODE_TABLE[MASK_6BITS & x];

            // 最后2个元素补'='
            result[resultLength - 1] = result[resultLength - 2] = PAD;
        }
        // 多出两个字符，即16位，需要base64转换后的3个字节，base64后的数组最后一个补'='
        else if (mod == 2) {
            byte b1 = b[b.length - 2];
            byte b2 = b[b.length - 1];

            // 两个字节是16位，为了与3个6位对齐，要多左移2位
            int x = ((int) b1 & MASK_8BITS) << 10;
            x |= ((int) b2 & MASK_8BITS) << 2;

            result[resultLength - 2] = ENCODE_TABLE[MASK_6BITS & x];
            x >>= 6;
            result[resultLength - 3] = ENCODE_TABLE[MASK_6BITS & x];
            x >>= 6;
            result[resultLength - 4] = ENCODE_TABLE[MASK_6BITS & x];

            // 最后一个元素补'='
            result[resultLength - 1] = PAD;
        }

        return new String(result, charset);
    }

    /**
     * 解码与编码是对应的，不多做说明了
     */
    public static String decode(String s) {
        if (s == null || s.length() == 0) return null;

        byte[] b = s.getBytes(charset);
        int steps = b.length / 4;

        int resultLength = steps * 3;
        if (b[b.length - 1] == PAD) {
            resultLength--;
        }
        if (b[b.length - 2] == PAD) {
            resultLength--;
        }
        byte[] result = new byte[resultLength];

        for (int i = 0; i < steps; i++) {
            int b1 = DECODE_TABLE[b[i * 4] & MASK_8BITS];
            int b2 = DECODE_TABLE[b[i * 4 + 1] & MASK_8BITS];
            int b3 = DECODE_TABLE[b[i * 4 + 2] & MASK_8BITS];
            int b4 = DECODE_TABLE[b[i * 4 + 3] & MASK_8BITS];

            int x = b1 << 18;
            x |= b2 << 12;
            x |= b3 < 0 ? 0 : b3 << 6;
            x |= b4 < 0 ? 0 : b4;

            byte rb3 = (byte) x;
            x >>= 8;
            byte rb2 = (byte) x;
            x >>= 8;
            byte rb1 = (byte) x;

            // 最后3个字节单独处理
            if (i == steps - 1) {
                result[i * 3] = rb1;
                if (b[b.length - 2] != PAD) {
                    result[i * 3 + 1] = rb2;
                }
                if (b[b.length - 1] != PAD) {
                    result[i * 3 + 2] = rb3;
                }
            } else {
                result[i * 3] = rb1;
                result[i * 3 + 1] = rb2;
                result[i * 3 + 2] = rb3;
            }
        }

        // 最后4个字节单独处理

        return new String(result, charset);
    }

    public static void main(String[] args) {
        String source = "1我fsd爱你，4你d爱s我fd吗？5";

        System.out.println("source: [" + source + "]");
        System.out.println();

        System.out.println("Encode:");
        String myResult = encode(source);
        String jdkResult = new BASE64Encoder().encode(source.getBytes()).replaceAll("\r\n", "");
        System.out.println("\tmyResult  : [" + myResult + "]");
        System.out.println("\tjdkResult : [" + jdkResult + "]");
        System.out.println("\tmyResult.equals(jdkResult) = " + myResult.equals(jdkResult));

        System.out.println();
        System.out.println("Decode:");
        String decodeResult = decode(jdkResult);
        System.out.println("\tdecodeResult : [" + decodeResult + "]");
        System.out.println("\tdecodeResult.equals(source) = " + decodeResult.equals(source));
    }

}
