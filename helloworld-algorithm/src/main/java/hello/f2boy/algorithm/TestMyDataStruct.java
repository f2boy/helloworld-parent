package hello.f2boy.algorithm;

/**
 * Created by 000474 on 2018/6/6.
 */
public class TestMyDataStruct {
    public static void main(String[] args) {

        IMyDataStruct ds = new MyDataStruct5();

        ds.add(new MyData(6, "老六"));
        ds.add(new MyData(8, "老八"));
        ds.add(new MyData(5, "老五"));
        ds.add(new MyData(1, "老一"));
        ds.add(new MyData(2, "老二"));
        ds.add(new MyData(4, "老四"));
        ds.add(new MyData(9, "老九"));
        ds.add(new MyData(3, "老三"));
        ds.add(new MyData(7, "老七"));
        ds.iterate();

        System.out.println(ds.get(8));
        System.out.println(ds.get(1));
        System.out.println(ds.get(9));
        System.out.println(ds.get(12));
        System.out.println();
        
        ds.delete(2);
        ds.delete(5);
        ds.delete(6);
        ds.delete(5);
        ds.delete(1);
        ds.delete(9);
        ds.delete(10);
        ds.iterate();

        ds.add(new MyData(5, "老五"));
        ds.add(new MyData(25, "老二十五"));
        ds.add(new MyData(10, "老十"));
        ds.add(new MyData(9, "老九九九"));
        ds.iterate();
    }

}
