package foundationgames.nilchasm.chasmix.test;

public class TestTargetClass {
    private final String myField = "hello";

    private static void myStaticMethod() {
    }

    public static void init() {
        var cls = TestTargetClass.class;

        for (var f : cls.getDeclaredFields()) {
            for (var a : f.getDeclaredAnnotations()) {
                System.out.print(a.toString() + " ");
            }
            System.out.println(f);
        }
        for (var m : cls.getDeclaredMethods()) System.out.println(m);
    }
}
