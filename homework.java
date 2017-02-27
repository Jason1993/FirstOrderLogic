/**
 * Created by wenqingcao on 11/25/16.
 */
import java.util.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wenqingcao on 10/23/16.
 */
public class homework {
    public static int label = 1;
    public static ArrayList<String> readFile(String file) throws IOException {
        ArrayList<String> temp = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String         line = null;
        try {
            while((line = reader.readLine()) != null) {
                StringBuilder  stringBuilder = new StringBuilder();
                stringBuilder.append(line);
                temp.add(stringBuilder.toString());
            }
        } finally {
            reader.close();
        }
        return temp;
    }

    public static void writeFile(ArrayList<String> s) throws IOException {
        PrintWriter output = new PrintWriter("output.txt","UTF-8");
        Iterator<String> tempstring = s.iterator();
        while (tempstring.hasNext() == true)
        {
            String line = tempstring.next();
            output.println(line);
        }
        output.close();
    }


    public static void main(String args[]) throws Exception
    {
        ArrayList<String> temp = readFile("input.txt");
        HashMap<String,ArrayList<String>> tempkb = new HashMap<>();
        int nq = Integer.valueOf(temp.get(0));
        int ns = Integer.valueOf(temp.get(nq+1));
        parse parser = new parse();

        int i;
        for (i = 2+nq; i < 2+nq+ns; i++)
        {
            parser.readLine(temp.get(i),label,tempkb);
            label= parser.getLab();
        }
        resolution test = new resolution();
        test.kb.setTempkb(tempkb);
        test.kb.set();
        for (i = 1 ; i < 1+nq; i++) {
            test.setQueryList(temp.get(i));
        }
        //test.setQueryList("~H(Alice)");
        test.performEntailment();
        ArrayList<String> res =test.getResult();
        writeFile(res);
    }
}
