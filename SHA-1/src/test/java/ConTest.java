import org.junit.Test;

public class ConTest {

    @Test
    public void test01(){
        byte a = 34;
        byte b = 21;
        int c = (int)a;
        System.out.println(c);
        c = c<<8;
        System.out.println(c);
        c+=(int)b;

        System.out.println(c);
    }


    @Test
    public void test02(){
        int a = 324123123;
        int b = a & 0x0FFFFFFFF;
        System.out.println(a & 0x0FFFFFFFF);
        System.out.println(a & 0x0FFFFFFFFL);
    }
}
