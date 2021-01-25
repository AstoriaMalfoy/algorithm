import jdk.nashorn.internal.ir.Block;

import java.io.*;
import java.util.Arrays;

public class SHA1Achieve {

    private static final int ONE_OR_TWO_PAGE_JUDGE_FLAG = 56;

    public static final int FUNC_KEY_VALUE1 = 0x5A827999;
    public static final int FUNC_KEY_VALUE2 = 0x6ED9EBA1;
    public static final int FUNC_KEY_VALUE3 = 0x8F1BBCDC;
    public static final int FUNC_KEY_VALUE4 = 0xCA62C1D6;

    public static int LINK_V1 = 0x67452301;
    public static int LINK_V2 = 0xEFCDAB89;
    public static int LINK_V3 = 0x98BADCFE;
    public static int LINK_V4 = 0x10325476;
    public static int LINK_V5 = 0xC3D2E1F0;


    /**
     * 调用参数
     * -f | -F fileName
     * -m | -M message
     * @param args
     */
    public static void main(String[] args) throws IOException {
        for(int i=0;i<args.length;++i){
            System.out.println(args[i]);
        }
        if (args.length >= 2) {
            // 使用文件进行计算
            if ("-f".equals(args[0]) || "-F".equals(args[0])) {
                String filePath = args[1];
                System.out.println("[tset] load the file " + filePath);
                String sha1Value = getSHA1Value(new File(filePath));
                System.out.println(sha1Value);
            }
            // 直接计算文本信息
            else if ("-m".equals(args[0]) || "-M".equals(args[0])) {
                String message = args[1];
                System.out.println("[test] load the message " + message);
                String sha1Value = getSHA1Value(message);
                System.out.println(sha1Value);
            } else {
                ParameterError();
            }
        } else {
            ParameterError();
        }
    }


    /**
     * 当参数错误的时候展示参数的使用格式
     */
    public static void ParameterError(){
        System.out.println("[Error] parameter error ");
        System.out.println("    use the -f|-F filepath calculation the file SHA1 value");
        System.out.println("    use the -m|-M message calculation the message SHA1 value");
    }

    /**
     * 对文件计算SHA1值
     * @param file
     * @return
     */
    public static String getSHA1Value(File file) throws IOException {
        byte[] dataBlock = new byte[64];
        InputStream fileStream = new FileInputStream(file);
        int count = 0;
        int fileSize = 0;
        while((count = fileStream.read(dataBlock))!=-1){
            //读取到的数据不满足512位，需要进行填充
            fileSize += count;
            if(count!=64){
                FillResult fillResult = fillByte(dataBlock, fileSize, count);
                System.out.println(Arrays.toString(fillResult.dataBlockOne));
            }else{
                
            }
        }
        System.out.println(fileSize);
        return null;
    }


    /**
     * 对数据进行补位填充和结尾填充
     * 补位填充：填充1000000...
     * 结尾填充：在最后填充长度位8byte的长度标识
     * @TODO 目前文件长度记录使用的int型数据类型，所以支持的文件大小为2^32也就是4GB
     * @param dataBlock
     * @param fileSize
     * @param countSize
     * @return
     */
    private static FillResult fillByte(byte[] dataBlock, int fileSize,int countSize) {
        FillResult result = new FillResult();
        //使用一个数据块进行填充
        if(countSize < ONE_OR_TWO_PAGE_JUDGE_FLAG){
            result.setOneBlock(true);
            byte[] reDataBlock;
            reDataBlock = dataBlock;
            int pointFlag = countSize;
            reDataBlock[pointFlag] = (byte) 0x80;
            for (int i = 0; i < 64 - countSize - 5; i++) {
                pointFlag++;
                reDataBlock[pointFlag] = (byte)0x00;
            }
            reDataBlock[63] = (byte)fileSize;
            reDataBlock[62] = (byte) (fileSize>>>8);
            reDataBlock[61] = (byte) (fileSize>>>16);
            reDataBlock[60] = (byte) (fileSize>>>24);
            result.setDataBlockOne(reDataBlock);
        }
        //需要使用两个数据块进行填充
        else{
            result.setOneBlock(false);

        }
        return result;
    }

    private static class FillResult{
        boolean oneBlock = true;
        byte[] dataBlockOne = null;
        byte[] dataBlockSecond = null;

        public boolean isOneBlock() {
            return oneBlock;
        }

        public void setOneBlock(boolean oneBlock) {
            this.oneBlock = oneBlock;
        }

        public byte[] getDataBlockOne() {
            return dataBlockOne;
        }

        public void setDataBlockOne(byte[] dataBlockOne) {
            this.dataBlockOne = dataBlockOne;
        }

        public byte[] getDataBlockSecond() {
            return dataBlockSecond;
        }

        public void setDataBlockSecond(byte[] dataBlockSecond) {
            this.dataBlockSecond = dataBlockSecond;
        }
    }

    /**
     * 对字符串计算SHA1值
     * @param message
     * @return
     */
    private static String getSHA1Value(String message){
        return null;
    }


}
