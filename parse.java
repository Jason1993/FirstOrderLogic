import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wenqingcao on 10/24/16.
 */
//((~(Parent(x,y) & Ancestor(y,z))) | Ancestor(x,z))

public class parse {
    private int lab;
    public int getLab()
    {
        return lab;
    }
    public void readLine(String s, int label, HashMap<String, ArrayList<String>> kb)
    {
        int i,j,m,n,x,y;

        String tempstr = s.replaceAll("\\s+","");
        //System.out.println(tempstr);
        Stack<Integer> preindex = new Stack<Integer>();
        ArrayList<String> part = new ArrayList<>();
        String namepattern = "[A-z]+\\([A-z](,[A-z])*\\)"; //[A-z]+\([A-z]+(,[A-z])*\)
        Pattern pattern = Pattern.compile(namepattern);
        Matcher matcher = pattern.matcher(tempstr);
        while (matcher.find()) {
            String temp = matcher.group();
            part.add(temp);
        }
        if (!part.isEmpty()) {
            for (i = 0; i < part.size(); i++) {
                String temp = part.get(i);
                tempstr = tempstr.replace(temp, String.valueOf(i));
            }


            int templength = tempstr.length();
            //build a Hashmap of parethesis index;
            HashMap<Integer, Integer> parethesisindex = new HashMap<Integer, Integer>();
            for (i = 0; i < templength; i++) {
                if (tempstr.charAt(i) == '(') {
                    preindex.push(i);
                }
                if (tempstr.charAt(i) == ')') {
                    int pre = preindex.pop();
                    parethesisindex.put(i, pre);
                }
            }
            //do parse job next;
            //convert to inference format

        /*int templength = tempstr.length();
        for (i = 0; i < tempstr.length(); i++)
        {
            if (tempstr.charAt(i) == '(')
            {
                preindex.push(i);
            }
            if (tempstr.charAt(i) == ')')
            {
                int pre = preindex.pop();
                if (i<(tempstr.length()-1)) {
                    if (tempstr.charAt(pre + 1) == '~' && tempstr.charAt(i + 1) == '|' ) {

                        StringBuilder builder = new StringBuilder();

                        builder.append(tempstr.substring(0, pre));
                        builder.append(tempstr.substring(pre+2, i));
                        builder.append("=>");
                        builder.append(tempstr.substring(i + 2));
                        tempstr = builder.toString();
                        i = -1;
                        templength = tempstr.length();
                        preindex.clear();
                    }
                }
            }
        }*/
            String infersymbol = "=>";
            int index = 0;
            while (tempstr.indexOf(infersymbol, index) != -1) {
                x = tempstr.indexOf(infersymbol, index);
                if (tempstr.charAt(x - 1) == ')') {
                    int indexpre = parethesisindex.get(x - 1);
                    StringBuilder builder = new StringBuilder();
                    builder.append(tempstr.substring(0, indexpre));
                    builder.append("(~");
                    builder.append(tempstr.substring(indexpre, x));
                    builder.append(")|");
                    builder.append(tempstr.substring(x + 2));
                    tempstr = builder.toString();
                    templength = tempstr.length();
                    parethesisindex.clear();
                    for (i = 0; i < templength; i++) {
                        if (tempstr.charAt(i) == '(') {
                            preindex.push(i);
                        }
                        if (tempstr.charAt(i) == ')') {
                            int pre = preindex.pop();
                            parethesisindex.put(i, pre);
                        }
                    }
                } else {
                    StringBuilder builder = new StringBuilder();
                    builder.append(tempstr.substring(0, x - 1));
                    builder.append("(~");
                    builder.append(tempstr.charAt(x - 1));
                    builder.append(")|");
                    builder.append(tempstr.substring(x + 2));
                    tempstr = builder.toString();
                    templength = tempstr.length();
                    parethesisindex.clear();
                    for (i = 0; i < templength; i++) {
                        if (tempstr.charAt(i) == '(') {
                            preindex.push(i);
                        }
                        if (tempstr.charAt(i) == ')') {
                            int pre = preindex.pop();
                            parethesisindex.put(i, pre);
                        }
                    }
                }
            }
            //System.out.println(tempstr);
            //move negation inwards;
            String movein = "~(";
            while (tempstr.indexOf(movein) != -1) {
                x = tempstr.indexOf(movein);
                //int indexadd = parethesisindex.get(x+1);
                //add ~ to inner part iteratively and convert operator (the only one operator)
                preindex.push(x + 1);
                m = 0;
                for (i = x + 2; i < templength; i++) {
                    if (tempstr.charAt(i) == '(') {
                        preindex.push(i);
                    }
                    if (tempstr.charAt(i) == ')') {
                        y = preindex.pop();
                        if (y == x + 1) {
                            m = i;
                            break;
                        }
                    }
                }
                String subtemp = tempstr.substring(x + 1, m + 1);
                //deal with subtemp situations: ~ & |
                if (subtemp.charAt(1) == '~') {
                    StringBuilder builder = new StringBuilder();
                    builder.append(tempstr.substring(0, x));
                    builder.append(subtemp.substring(2, subtemp.length() - 1));
                    builder.append(tempstr.substring(m + 1));
                    tempstr = builder.toString();
                } else {
                    if (subtemp.charAt(1) == '(') {
                        n = 0;
                        preindex.push(1);
                        for (i = 2; i < subtemp.length(); i++) {
                            if (subtemp.charAt(i) == '(') {
                                preindex.push(i);
                            }
                            if (subtemp.charAt(i) == ')') {
                                y = preindex.pop();
                                if (y == 1) {
                                    n = i;
                                    break;
                                }
                            }
                        }
                        if (subtemp.charAt(n + 1) == '|') {
                            StringBuilder builder = new StringBuilder();
                            builder.append(tempstr.substring(0, x));
                            builder.append("((~");
                            builder.append(subtemp.substring(1, n + 1));
                            builder.append(")&(~");
                            builder.append(subtemp.substring(n + 2));
                            builder.append(")");
                            builder.append(tempstr.substring(m + 1));
                            tempstr = builder.toString();
                        }
                        if (subtemp.charAt(n + 1) == '&') {
                            StringBuilder builder = new StringBuilder();
                            builder.append(tempstr.substring(0, x));
                            builder.append("((~");
                            builder.append(subtemp.substring(1, n + 1));
                            builder.append(")|(~");
                            builder.append(subtemp.substring(n + 2));
                            builder.append(")");
                            builder.append(tempstr.substring(m + 1));
                            tempstr = builder.toString();
                        }
                    } else {
                        if (subtemp.charAt(2) == '|') {
                            StringBuilder builder = new StringBuilder();
                            builder.append(tempstr.substring(0, x));
                            builder.append("((~");
                            builder.append(subtemp.charAt(1));
                            builder.append(")&(~");
                            builder.append(subtemp.substring(3));
                            builder.append(")");
                            builder.append(tempstr.substring(m + 1));
                            tempstr = builder.toString();
                        }
                        if (subtemp.charAt(2) == '&') {
                            StringBuilder builder = new StringBuilder();
                            builder.append(tempstr.substring(0, x));
                            builder.append("((~");
                            builder.append(subtemp.charAt(1));
                            builder.append(")|(~");
                            builder.append(subtemp.substring(3));
                            builder.append(")");
                            builder.append(tempstr.substring(m + 1));
                            tempstr = builder.toString();
                        }
                    }
                }
            }
            //System.out.println(tempstr);
            //Double negation elimination
        /*templength =tempstr.length();
        for (i = 0; i < templength; i++)
        {
            if (tempstr.charAt(i) == '(')
            {
                preindex.push(i);
            }
            if (tempstr.charAt(i) == ')')
            {
                int pre1 = preindex.pop();
                if (tempstr.charAt(pre1+1) == '~') {
                    if (i < (tempstr.length() - 1)) {
                        if (tempstr.charAt(i + 1) == ')') {
                            int pre2 = preindex.pop();
                            if ((pre1-pre2)==2 && tempstr.charAt(pre2+1)=='~')
                            {
                                StringBuilder builder = new StringBuilder();
                                builder.append(tempstr.substring(0,pre2));
                                builder.append(tempstr.substring(pre1+2,i));
                                builder.append(tempstr.substring(i+2));
                                tempstr = builder.toString();
                                i = -1;
                                templength = tempstr.length();
                                preindex.clear();
                                continue;
                            }
                        }
                    }
                }
            }
        }
        System.out.println(tempstr);*/
            //remove useless parethesis;
            templength = tempstr.length();
            preindex.clear();
            for (i = 0; i < templength; i++) {
                if (tempstr.charAt(i) == '(') {
                    preindex.push(i);
                }
                if (tempstr.charAt(i) == ')') {
                    int temppre = preindex.pop();
                    if (tempstr.charAt(temppre + 1) == '~')
                        continue;
                    else {
                        if (tempstr.charAt(temppre + 1) == '(') {
                            Stack<Integer> tempcal = new Stack<>();
                            tempcal.push(temppre + 1);
                            for (j = temppre + 2; j < templength; j++) {
                                if (tempstr.charAt(j) == '(') {
                                    tempcal.push(j);
                                }
                                if (tempstr.charAt(j) == ')') {
                                    int indextemp = tempcal.pop();
                                    if (indextemp == temppre + 1) {
                                        if (j == i - 1) {
                                            StringBuilder builder = new StringBuilder();
                                            builder.append(tempstr.substring(0, temppre));
                                            builder.append(tempstr.substring(temppre + 1, i));
                                            builder.append(tempstr.substring(i + 1));
                                            tempstr = builder.toString();
                                            templength = tempstr.length();
                                            preindex.clear();
                                            i = -1;
                                            break;
                                        } else
                                            break;
                                    }
                                }
                            }
                        } else {
                            if (i == temppre + 2) {
                                StringBuilder builder = new StringBuilder();
                                builder.append(tempstr.substring(0, temppre));
                                builder.append(tempstr.charAt(temppre + 1));
                                builder.append(tempstr.substring(i + 1));
                                tempstr = builder.toString();
                                templength = tempstr.length();
                                preindex.clear();
                                i = -1;
                            }
                        }
                    }
                }
            }
            //System.out.println(tempstr);
        /* for (i = 0; i < templength; i++)
        {
            if (tempstr.charAt(i) == '(')
            {
                preindex.push(i);
            }
            if (tempstr.charAt(i) == ')')
            {
                int pre = preindex.pop();
                String tempsub = tempstr.substring(pre,i+1);
                String operatorPattern = "\\~|\\||\\&|(=>)";
                Pattern opPattern = Pattern.compile(operatorPattern);
                Matcher opMatcher = opPattern.matcher(tempsub);
                if (opMatcher.find() == false)
                {
                    StringBuilder builder = new StringBuilder();
                    builder.append(tempstr.substring(0,pre));
                    builder.append(tempstr.substring(pre+1,i));
                    builder.append(tempstr.substring(i+1));
                    tempstr = builder.toString();
                    i = -1;
                    templength = tempstr.length();
                    preindex.clear();
                }
            }
        }
        for (i = 0; i< templength; i++)
        {
            if (tempstr.charAt(i) == '(')
            {
                preindex.push(i);
            }
            if (tempstr.charAt(i) == ')')
            {
                int pre = preindex.pop();
                if (tempstr.charAt(pre+1) == '(' && tempstr.charAt(i-1)==')')
                {
                    StringBuilder builder = new StringBuilder();
                    builder.append(tempstr.substring(0,pre+1));
                    builder.append(tempstr.substring(pre+2,i));
                    builder.append(tempstr.substring(i+1));
                    tempstr = builder.toString();
                    i = -1;
                    templength = tempstr.length();
                    preindex.clear();
                }
            }
        }
        System.out.println(tempstr);*/

            //convert to CNF
            templength = tempstr.length();
            parethesisindex.clear();
            for (i = 0; i < templength; i++) {
                if (tempstr.charAt(i) == '(') {
                    preindex.push(i);
                }
                if (tempstr.charAt(i) == ')') {
                    int pre = preindex.pop();
                    parethesisindex.put(i, pre);
                }
            }
            for (i = 0; i < templength; i++) {
                if (tempstr.charAt(i) == '&') {
                    int indexpre, indexaft = 0, indexApre, indexBpre = 0, indexAaft, indexBaft = 0;
                    if (tempstr.charAt(i - 1) == ')') {
                        indexpre = parethesisindex.get(i - 1) - 1;
                        indexApre = indexpre + 1;
                        indexAaft = i;
                    } else {
                        indexpre = i - 2;
                        indexApre = i - 1;
                        indexAaft = i;
                    }
                    if (tempstr.charAt(i + 1) == '(') {
                        Stack<Integer> subpre = new Stack<>();
                        subpre.push(i + 1);
                        for (j = i + 2; j < templength; j++) {
                            if (tempstr.charAt(j) == '(') {
                                subpre.push(j);
                            }
                            if (tempstr.charAt(j) == ')') {
                                int tempindex = subpre.pop();
                                if (tempindex == i + 1) {
                                    indexaft = j + 1;
                                    indexBpre = i + 1;
                                    indexBaft = j + 1;
                                    break;
                                }
                            }
                        }
                    } else {
                        indexaft = i + 2;
                        indexBpre = i + 1;
                        indexBaft = i + 2;
                    }
                    if (indexpre != 0) {
                        if (tempstr.charAt(indexpre - 1) == '|') {
                            int indexpre2, indexaft2;
                            if (tempstr.charAt(indexpre - 2) == ')') {
                                indexpre2 = parethesisindex.get(indexpre - 2);
                                indexaft2 = indexpre - 1;

                            } else {
                                indexpre2 = indexpre - 2;
                                indexaft2 = indexpre - 1;

                            }
                            StringBuilder builder = new StringBuilder();
                            builder.append(tempstr.substring(0, indexpre2));
                            builder.append("(");
                            builder.append(tempstr.substring(indexpre2, indexaft2));
                            builder.append("|");
                            builder.append(tempstr.substring(indexApre, indexAaft));
                            builder.append(")&(");
                            builder.append(tempstr.substring(indexpre2, indexaft2));
                            builder.append("|");
                            builder.append(tempstr.substring(indexBpre, indexBaft));
                            builder.append(")");
                            builder.append(tempstr.substring(indexaft + 1));
                            tempstr = builder.toString();
                            templength = tempstr.length();
                            parethesisindex.clear();
                            for (i = 0; i < templength; i++) {
                                if (tempstr.charAt(i) == '(') {
                                    preindex.push(i);
                                }
                                if (tempstr.charAt(i) == ')') {
                                    int pre = preindex.pop();
                                    parethesisindex.put(i, pre);

                                }
                            }
                            i = -1;
                        } else {
                            if (tempstr.charAt(indexaft + 1) == '|') {
                                int indexpre2 = 0, indexaft2 = 0;
                                if (tempstr.charAt(indexaft + 2) == '(') {
                                    preindex.clear();
                                    preindex.push(indexaft + 2);
                                    for (j = indexaft + 3; j < templength; j++) {
                                        if (tempstr.charAt(j) == '(') {
                                            preindex.push(j);
                                        }
                                        if (tempstr.charAt(j) == ')') {
                                            n = preindex.pop();
                                            if (n == indexaft + 2) {
                                                indexpre2 = indexaft + 2;
                                                indexaft2 = j + 1;
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    indexpre2 = indexaft + 2;
                                    indexaft2 = indexaft + 3;
                                }
                                StringBuilder builder = new StringBuilder();
                                builder.append(tempstr.substring(0, indexpre));
                                builder.append("(");
                                builder.append(tempstr.substring(indexApre, indexAaft));
                                builder.append("|");
                                builder.append(tempstr.substring(indexpre2, indexaft2));
                                builder.append(")&(");
                                builder.append(tempstr.substring(indexBpre, indexBaft));
                                builder.append("|");
                                builder.append(tempstr.substring(indexpre2, indexaft2));
                                builder.append(")");
                                builder.append(tempstr.substring(indexaft2));
                                tempstr = builder.toString();
                                i = -1;
                                templength = tempstr.length();
                                parethesisindex.clear();
                                for (i = 0; i < templength; i++) {
                                    if (tempstr.charAt(i) == '(') {
                                        preindex.push(i);
                                    }
                                    if (tempstr.charAt(i) == ')') {
                                        int pre = preindex.pop();
                                        parethesisindex.put(i, pre);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //System.out.println(tempstr);


            //split by &;
            String[] tempParse = tempstr.split("&");
            for (i = 0; i < tempParse.length; i++) {
                String testtemp = tempParse[i];
                int opcount = 0, precount = 0, aftcount = 0;
                for (j = 0; j < testtemp.length(); j++) {
                    if (testtemp.charAt(j) == '~' || testtemp.charAt(j) == '|') {
                        opcount++;
                    }
                    if (testtemp.charAt(j) == '(') {
                        precount++;
                    }
                    if (testtemp.charAt(j) == ')') {
                        aftcount++;
                    }
                }
                int difpre = precount - opcount;
                int difaft = aftcount - opcount;
                String mod = testtemp.substring(difpre, (testtemp.length() - difaft));
                tempParse[i] = mod;
                //System.out.println(tempParse[i]);
            }

            //Standardize variable names
            HashMap<Character, String> variable = new HashMap<>();
            for (i = 0; i < part.size(); i++) {
                int indexa = 0, indexb = 0;
                String tempfunc = part.get(i);
                for (j = 0; j < tempfunc.length(); j++) {
                    if (tempfunc.charAt(j) == '(') {
                        indexa = j;
                    }
                    if (tempfunc.charAt(j) == ')') {
                        indexb = j;
                    }
                }
                String subtemp = tempfunc.substring(indexa + 1, indexb);

                String[] vars = subtemp.split(",");
                for (m = 0; m < vars.length; m++) {
                    if (vars[m].length() == 1) {
                        Character tempchar = vars[m].charAt(0);
                        if (!variable.containsKey(tempchar)) {
                            StringBuilder builder = new StringBuilder();
                            builder.append("x");
                            builder.append(label);
                            String tempvar = builder.toString();
                            variable.put(tempchar, tempvar);
                            vars[m] = tempvar;
                            label++;
                        } else {
                            String tempvar = variable.get(tempchar);
                            vars[m] = tempvar;
                        }
                    }
                }
                StringBuilder funct = new StringBuilder();
                funct.append(tempfunc.substring(0, indexa + 1));
                funct.append(vars[0]);
                if (vars.length > 1) {
                    for (n = 1; n < vars.length; n++) {
                        funct.append(",");
                        funct.append(vars[n]);
                    }
                }
                funct.append(tempfunc.substring(indexb));
                part.set(i, funct.toString());
            }
            if (!part.isEmpty()) {
                for (j = 0; j < tempParse.length; j++) {
                    tempstr = tempParse[j];
                    StringBuilder fin = new StringBuilder();
                    for (i = 0; i < tempstr.length(); i++) {
                        if (Character.isDigit(tempstr.charAt(i))) {
                            String num1 = String.valueOf(tempstr.charAt(i));
                            int num = Integer.valueOf(num1);
                            fin.append(part.get(num));
                        } else {
                            fin.append(tempstr.charAt(i));

                        }
                    }
                    tempstr = fin.toString();
                    //System.out.println(tempstr);
                    tempParse[j] = tempstr;
                }
            }

            lab = label;

            //Store phrase into KB;
            ArrayList<String> sentence = new ArrayList<>();

            for (i = 0; i < tempParse.length; i++) {
                tempstr = tempParse[i];
                String[] tempsplit = tempstr.split("\\|");
                ArrayList<String> unittemp = new ArrayList<>();

                for (j = 0; j < tempsplit.length; j++) {
                    String unit = tempsplit[j];
                    int indexstart = 0, indexend = 0;
                    for (m = 0; m < unit.length(); m++) {
                        if (Character.isLetter(unit.charAt(m))) {
                            if (m != 0) {
                                if (unit.charAt(m - 1) == '~') {
                                    indexstart = m - 1;
                                } else {
                                    indexstart = m;
                                }
                                for (n = indexstart + 1; n < unit.length(); n++) {
                                    if (unit.charAt(n) == ')') {
                                        indexend = n;
                                        break;
                                    }
                                }
                                unittemp.add(unit.substring(indexstart, indexend + 1));
                                break;
                            } else {
                                indexstart = m;
                                for (n = indexstart + 1; n < unit.length(); n++) {
                                    if (unit.charAt(n) == ')') {
                                        indexend = n;
                                        break;
                                    }
                                }
                                unittemp.add(unit.substring(indexstart, indexend + 1));
                                break;
                            }
                        }
                    }

                }
                StringBuilder restemp = new StringBuilder();
                restemp.append(unittemp.get(0));
                if (unittemp.size() > 1) {
                    for (x = 1; x < unittemp.size(); x++) {
                        restemp.append("|");
                        restemp.append(unittemp.get(x));
                    }
                }
                String finalstr = restemp.toString();
                //sentence.add(restemp.toString());
                for (y=0;y < unittemp.size();y++)
                {
                    String unittest = unittemp.get(y);
                    int k;
                    for (k=0; k < unittest.length(); k++)
                    {
                        if (unittest.charAt(k) == '(')
                            break;
                    }
                    String key = unittest.substring(0,k);
                    if (kb.containsKey(key))
                    {
                        ArrayList<String> modify =kb.get(key);
                        modify.add(finalstr);
                        kb.put(key,modify);
                    }
                    else
                    {
                        ArrayList<String> value = new ArrayList<>();
                        value.add(finalstr);
                        kb.put(key,value);
                    }
                }
            }

        }
        else
        {
            kb.put(tempstr,null);
        }
    }

}
