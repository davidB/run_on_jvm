//#from dir:MyLib.java
import lib.MyLib;

public class main_with_local_deps {
  public static void main(String args[]) {
    String arg0 = args.length > 0 ? args[0] : "1";
    System.out.format("%d\n", MyLib.twice(Integer.parseInt(arg0, 10)));
  }
}
