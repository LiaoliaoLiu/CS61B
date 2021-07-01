public class IsADemo {
    public static void main(String[] args) {
        List61B<String> someList = new SLList<>();
        SLList l2 = (SLList) someList;
        l2.test(l2);
    }
}