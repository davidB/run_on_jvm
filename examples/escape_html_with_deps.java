//#repo central m2:http://repo1.maven.org/maven2/
//#from org.apache.commons:commons-lang3:3.1
import org.apache.commons.lang3.StringEscapeUtils;

public class escape_html_with_deps {
  public static void main(String args[]) {
    String arg0 = args.length > 0 ? args[0] : "\"bread\" & \"butter\"";
    System.out.println(StringEscapeUtils.escapeHtml4(arg0));
  }
}
