package cn.net.astoria;


import java.util.Locale;

/**
 * 用于存储32位无符号整型
 * @author Astoria
 */
public class UInt_32 {
    private long baseData;

    public UInt_32(){}
    private UInt_32(int initNumber){
        /**
         * 使用 a & 0x0FFFFFFFFL 和 a & 0x0FFFFFFFF是不同的，其最大的不同在于表达式的值的类型，前者表达式的值的类型位int，后者的表达
         * 表达式的类型为long
         */
        this.baseData = initNumber & 0x0FFFFFFFFL;
    }

    /**
     * 对int数据进行初始化
     * @param initNumber
     * @return
     */
    public static UInt_32 getValue(int initNumber){
        return new UInt_32(initNumber);
    }

    public UInt_32 leftShift(int offset){
        this.baseData = this.baseData << offset;
        return this;
    }

    public UInt_32 leftShiftCycle(int offset){
        this.baseData = this.baseData << offset;
        long highValue = this.baseData & 0xFFFFFFFF00000000L;
        this.baseData = this.baseData & 0x00000000FFFFFFFFL;
        highValue = highValue >>> 32;
        this.baseData += highValue;
        return this;
    }
    public UInt_32 rightShift(int offset){
        this.baseData = this.baseData >>> offset;
        return this;
    }

    public UInt_32 AND(UInt_32 value){
        this.baseData = this.baseData & value.baseData;
        return this;
    }

    public UInt_32 OR(UInt_32 value){
        this.baseData = this.baseData | value.baseData;
        return this;
    }

    public UInt_32 XOR(UInt_32 value){
        this.baseData = this.baseData^ value.baseData;
        return this;
    }

    public UInt_32 ADD(UInt_32 value){
        this.baseData += value.baseData;
        return this;
    }
    public UInt_32 NOT(){
        this.baseData = ~this.baseData;
        this.baseData = this.baseData & 0x00000000FFFFFFFFL;
        return this;
    }
    public UInt_32 cutOverHighValueCicle(){
        long testResult = this.baseData & 0x0000000100000000L;
        this.baseData = this.baseData << 32;
        this.baseData = this.baseData >>> 32;
        if(testResult!=0){
            this.baseData+=1;
        }
        return this;
    }
    public UInt_32 cutOverHighValue(){
        long testResult = this.baseData & 0x0000000100000000L;
        this.baseData = this.baseData << 32;
        this.baseData = this.baseData >>> 32;
        return this;
    }
    public UInt_32 tailAppent(byte value){
        this.baseData += Byte.toUnsignedInt(value);
        return this;
    }

    public UInt_32 clearByZero(){
        this.baseData = 0;
        return this;
    }

    public String toHexString(){
        String str = Long.toHexString(baseData).toUpperCase(Locale.ROOT);
        if(str.length() < 8){
            int zeroLength = 8-str.length();
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < zeroLength; i++) {
                stringBuffer.append("0");
            }
            str = stringBuffer.toString()+str;
        }
        return str;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new UInt_32();
    }


    public long getBaseData() {
        return baseData;
    }

    public void setBaseData(long baseData) {
        this.baseData = baseData;
    }

    public static void main(String[] args){
        UInt_32 value = UInt_32.getValue(Integer.MAX_VALUE);
        value.ADD(UInt_32.getValue(217879)).cutOverHighValueCicle();
        System.out.println(value.toHexString());
        System.out.println(value.leftShiftCycle(8).toHexString());
    }

}
