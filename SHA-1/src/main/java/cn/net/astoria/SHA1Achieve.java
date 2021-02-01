package cn.net.astoria;

import java.io.*;
import java.util.Arrays;

/**
 * @author Administrator
 */
public class SHA1Achieve {

    private static final int ONE_OR_TWO_PAGE_JUDGE_FLAG = 56;
    /**
     *      运算函数常量
     */
    public static final UInt_32 FUNC_KEY_VALUE1 = UInt_32.getValue(0x5A827999);
    public static final UInt_32 FUNC_KEY_VALUE2 = UInt_32.getValue(0x6ED9EBA1);
    public static final UInt_32 FUNC_KEY_VALUE3 = UInt_32.getValue(0x8F1BBCDC);
    public static final UInt_32 FUNC_KEY_VALUE4 = UInt_32.getValue(0xCA62C1D6);


    /**
     *  存储运算中间变量以及最后的结果
     */
    public static UInt_32 LINK_VA = UInt_32.getValue(0x67452301);
    public static UInt_32 LINK_VB = UInt_32.getValue(0xEFCDAB89);
    public static UInt_32 LINK_VC = UInt_32.getValue(0x98BADCFE);
    public static UInt_32 LINK_VD = UInt_32.getValue(0x10325476);
    public static UInt_32 LINK_VE = UInt_32.getValue(0xC3D2E1F0);


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
        boolean isFilled = false;
        while((count = fileStream.read(dataBlock))!=-1){
            fileSize += count;
            if(count!=64){
            //读取到的数据不满足512位，需要进行填充,并且可以确定是最后一个或者最后连个数据块
                isFilled = true;
                FillResult fillResult = fillByte(dataBlock, fileSize, count);
                if(fillResult.oneBlock){
                    calculationSHA1(fillResult.dataBlockOne);
                }else{
                    calculationSHA1(fillResult.dataBlockOne);
                    calculationSHA1(fillResult.dataBlockSecond);
                }
            }else{
                //常规数据块，直接计算
                calculationSHA1(dataBlock);
            }
        }
        if(!isFilled){
//            数据刚好切割成完整的数据块，最后要填充一个数据块
            FillResult lastFillBlock = fillByte(new byte[64], fileSize, 0);
            calculationSHA1(lastFillBlock.dataBlockOne);
        }
        formatResult();
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
            fileSize*=8;
            reDataBlock[63] = (byte)fileSize;
            reDataBlock[62] = (byte) (fileSize>>>8);
            reDataBlock[61] = (byte) (fileSize>>>16);
            reDataBlock[60] = (byte) (fileSize>>>24);
            result.setDataBlockOne(reDataBlock);
        }
        //需要使用两个数据块进行填充
        else{
            result.setOneBlock(false);
            // 如果需要使用两个数据块进行填充，那么则需要对第一个数据块全填充0，对第二个数据块填充0，后64byte填充长度
            byte[] blockOne = dataBlock;
            byte[] blockSecond = new byte[64];
            for (int i = countSize; i < 64; i++) {
                blockOne[i] = 0x00;
            }
            for (int i = 0; i < 60; i++) {
                blockSecond[i] = 0x00;
            }
            blockSecond[63] = (byte)  fileSize;
            blockSecond[62] = (byte) (fileSize>>>8);
            blockSecond[61] = (byte) (fileSize>>>16);
            blockSecond[60] = (byte) (fileSize>>>24);
            result.setDataBlockOne(blockOne);
            result.setDataBlockSecond(blockSecond);
        }
        return result;
    }

    /**
     * 用于对文件进行末尾填充时使用，并针对最后一个数据块的不同长度做不同处理
     * 如果文件最后一个数据块长度大于0小于56，则在该数据块中有充足的长度存储文件大小
     * 如果文件最后一个数据块长度大于56，则该数据块剩余空间不够存储文件的长度数据，则需要将该数据块最后填充全0.并且创建一个新的数据块用于存储文件长度信息
     */
    private static class FillResult{
        /**
         * 第二个数据数据块是否禁用
         */
        boolean oneBlock = true;
        /**
         * 第一个数据块
         */
        byte[] dataBlockOne = null;
        /**
         * 第二个数据块
         */
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


    /**
     * 对每一个数据块进行SHA1值计算，在计算的过程中，对静态变量池进行操作
     * @param dataBlock  要进行操作的数据块，要求其长度必须为64byte 也就是512位
     */
    private static void calculationSHA1(byte[] dataBlock){
        //先将数据分为16个明文分组
        UInt_32[] baseGrouping = new UInt_32[16];
        for(int i=0;i<16;++i){
            UInt_32 temp;
            baseGrouping[i] = new UInt_32();
            baseGrouping[i].clearByZero();
            baseGrouping[i].tailAppent(dataBlock[4*i]).leftShift(8)
                    .tailAppent(dataBlock[4*i+1]).leftShift(8)
                    .tailAppent(dataBlock[4*i+2]).leftShift(8)
                    .tailAppent(dataBlock[4*i+3]);
        }

        //将16个明文分组拓展为80个链接分组
        UInt_32[] linkGrouping = new UInt_32[80];
        for(int i=0;i<80;++i){
            if(i<16){
                linkGrouping[i] = baseGrouping[i];
            }else{
                UInt_32 temp = new UInt_32();
                temp.setBaseData(linkGrouping[i-3].getBaseData());
                linkGrouping[i] = temp.XOR(linkGrouping[i-8]).XOR(linkGrouping[i-14]).XOR(linkGrouping[i-16]).leftShift(1).cutOverHighValueCicle();
            }
            //A,B,C,D,E←[(A<<<5)+ ft(B,C,D)+E+Wt+Kt],A,(B<<<30),C,D
            UInt_32 a = new UInt_32(),b = new UInt_32(),c = new UInt_32(),d = new UInt_32(),e = new UInt_32();
            a.setBaseData(LINK_VA.getBaseData());
            b.setBaseData(LINK_VB.getBaseData());
            c.setBaseData(LINK_VC.getBaseData());
            d.setBaseData(LINK_VD.getBaseData());
            e.setBaseData(LINK_VE.getBaseData());
            LINK_VE.setBaseData(d.getBaseData());
            LINK_VD.setBaseData(c.getBaseData());
            LINK_VB.setBaseData(a.getBaseData());
            UInt_32 tempa = new UInt_32();
            tempa.setBaseData(a.getBaseData());
            UInt_32 tempb = new UInt_32();
            tempb.setBaseData(b.getBaseData());
            LINK_VC = tempb.leftShiftCycle(30);
            if(i<20){
                LINK_VA = tempa.leftShiftCycle(5).ADD(calcuFunA()).ADD(e).ADD(linkGrouping[i]).ADD(FUNC_KEY_VALUE1).cutOverHighValue();
//                LINK_VA =  (tempa<<5) + calcuFunA() + e + Integer.parseUnsignedInt(String.valueOf(linkGrouping[i]))  + FUNC_KEY_VALUE1;
            }else if(i<40){
                LINK_VA = tempa.leftShiftCycle(5).ADD   (calcuFunB()).ADD(e).ADD(linkGrouping[i]).ADD(FUNC_KEY_VALUE2).cutOverHighValue();
//                LINK_VA =  (tempa<<5) + calcuFunB() + e + Integer.parseUnsignedInt(String.valueOf(linkGrouping[i])) + FUNC_KEY_VALUE2;
            }else if(i<60){
                LINK_VA = tempa.leftShiftCycle(5).ADD(calcuFunC()).ADD(e).ADD(linkGrouping[i]).ADD(FUNC_KEY_VALUE3).cutOverHighValue();
//                LINK_VA =  (tempa<<5) + calcuFunC() + e + Integer.parseUnsignedInt(String.valueOf(linkGrouping[i])) + FUNC_KEY_VALUE3;
            }else{
                LINK_VA = tempa.leftShiftCycle(5).ADD(calcuFunD()).ADD(e).ADD(linkGrouping[i]).ADD(FUNC_KEY_VALUE4).cutOverHighValue();
//                LINK_VA =  (tempa<<5) + calcuFunA() + e + Integer.parseUnsignedInt(String.valueOf(linkGrouping[i])) + FUNC_KEY_VALUE4;
            }
            System.out.println(LINK_VA.toHexString() + '\t' + LINK_VB.toHexString() + '\t' + LINK_VC.toHexString() + '\t' + LINK_VD.toHexString() + '\t' + LINK_VE.toHexString());
        }
        for(int i=0;i<80;++i){
            System.out.print(linkGrouping[i].toHexString() + '\t');
            if(i % 4 == 3){
                System.out.println();
            }
        }
        System.out.println();
//        System.out.println(LINK_VA + '\t' + LINK_VB + '\t' + LINK_VC + '\t' + LINK_VD + '\t' + LINK_VE);
    }


    /**
     * 结果格式化函数，将最后的结果进行格式化
     */
    public static void formatResult(){
//        String resultA = Integer.toHexString(LINK_VA);
//        String resultB = Integer.toHexString(LINK_VB);
//        String resultC = Integer.toHexString(LINK_VC);
//        String resultD = Integer.toHexString(LINK_VD);
//        String resultE = Integer.toHexString(LINK_VE);
//        System.out.println(resultA+resultB+resultC+resultD+resultE);
    }

    /**
     * F函数A,当i处于0-19的时候使用
     * 具体逻辑为：(B AND C) OR ((NOT B) AND D)
     * @return
     */
    public static UInt_32 calcuFunA(){
        UInt_32 vb = new UInt_32(),vc = new UInt_32(),vd = new UInt_32();
        vb.setBaseData(LINK_VB.getBaseData());
        vc.setBaseData(LINK_VC.getBaseData());
        vd.setBaseData(LINK_VD.getBaseData());
        return vb.AND(vc).OR(vb.NOT().AND(vd));
//        return (LINK_VB & LINK_VC) | (~LINK_VB & LINK_VD);
    }

    /**
     * F函数B，当i处于20-39时使用
     * 具体逻辑为：B XOR C XOR D
     * @return
     */
    public static UInt_32 calcuFunB(){
//        return LINK_VB ^ LINK_VC ^ LINK_VD;
        UInt_32 vb = new UInt_32(),vc = new UInt_32(),vd = new UInt_32();
        vb.setBaseData(LINK_VB.getBaseData());
        vc.setBaseData(LINK_VC.getBaseData());
        vd.setBaseData(LINK_VD.getBaseData());
        return vb.XOR(vc).XOR(vd);
    }
    /**
     * F函数C，当i处于40-59时使用
     * 具体逻辑为：(B AND C) OR (B AND D) OR (C AND D)
     * @return
     */
    public static UInt_32 calcuFunC(){
//        return (LINK_VB & LINK_VC) | (LINK_VB & LINK_VD) | (LINK_VC & LINK_VD);
        UInt_32 vb = new UInt_32(),vc = new UInt_32(),vd = new UInt_32();
        vb.setBaseData(LINK_VB.getBaseData());
        vc.setBaseData(LINK_VC.getBaseData());
        vd.setBaseData(LINK_VD.getBaseData());
        return vb.AND(vc).OR(vb.AND(vd)).OR(vc.AND(vd));
    }
    /**
     * F函数D，当i处于60-79时使用
     * 具体逻辑为：B XOR C XOR D
     * @return
     */
    public static UInt_32 calcuFunD(){
//        return LINK_VB ^ LINK_VC ^ LINK_VD;
        UInt_32 vb = new UInt_32(),vc = new UInt_32(),vd = new UInt_32();
        vb.setBaseData(LINK_VB.getBaseData());
        vc.setBaseData(LINK_VC.getBaseData());
        vd.setBaseData(LINK_VD.getBaseData());
        return vb.XOR(vc).XOR(vd);
    }

}
