import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
/**
 * Created by wenqingcao on 11/24/16.
 */
public class KB {
    private ArrayList<String> facts = new ArrayList<>();
    private HashMap<String, ArrayList<String>> CNF = new HashMap<>();
    private HashMap<String, ArrayList<String>> tempkb = new HashMap<>();
    private ArrayList<String> factsPre = new ArrayList<>();
    public void setTempkb(HashMap<String, ArrayList<String>> parsed)
    {
        this.tempkb = parsed;
    }

    public void set()
    {
        Set<String> templist = this.tempkb.keySet();
        Iterator<String> iterator = templist.iterator();
        while (iterator.hasNext() == true)
        {
            String keys = iterator.next();
            if (keys.contains("("))
            {
                this.facts.add(keys);
            }
            else
            {
                this.CNF.put(keys,tempkb.get(keys));
            }
        }
        int i;
        for (i = 0; i < facts.size(); i++)
        {
            int indextemp = facts.get(i).indexOf("(");
            String pre = facts.get(i).substring(0,indextemp);
            if (!factsPre.contains(pre))
            {
                factsPre.add(pre);
            }
        }
    }

    public ArrayList<String> getFacts()
    {
        return this.facts;
    }
    public ArrayList<String> getFactsPre(){
        return this.factsPre;
    }
    public HashMap<String, ArrayList<String>> getCNF() {
        return this.CNF;
    }
    public void addFacts(String s)
    {
        this.facts.add(s);
    }
    public void reset(String s)
    {
        this.facts.remove(s);
    }
}
