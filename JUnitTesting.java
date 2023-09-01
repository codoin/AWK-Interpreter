import org.junit.*;

public class JUnitTesting {

    @Test
    public static void IsDoneMethod(){
        StringHandler s = new StringHandler(null)
        Assert.assertEquals(True,s.IsDone());
    }
}
