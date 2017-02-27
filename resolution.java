import java.util.*;

/**
 * Created by wenqingcao on 11/23/16.
 */
public class resolution {
    KB kb = new KB();
    HashMap<String,ArrayList<String>> CNF = kb.getCNF();
    ArrayList<String> factPre = kb.getFactsPre();
    ArrayList<String> queryList = new ArrayList<>();
    ArrayList<String> entailedList = new ArrayList<>();
    ArrayList<String> resultList = new ArrayList<>();
    boolean contradiction = false;
    public ArrayList<String> getResult()
    {
        return resultList;
    }
    public void setQueryList(String s)
    {
        this.queryList.add(s);
    }
    public ArrayList<String> performEntailment() throws Exception
    {
        for (int i = 0 ; i < queryList.size() ; i++)
        {
            String w_query = queryList.get(i);
            String negQuery;
            if (w_query.charAt(0) == '~')
            {
                negQuery = w_query.substring(1,w_query.length());
            }
            else
            {
                negQuery = "~" + w_query;
            }
            //kb.addFacts(negQuery);
            LinkedHashMap<String, String> w_temp = new LinkedHashMap<String,String>();
            ArrayList<LinkedHashMap<String, String>> w_theta = new ArrayList<LinkedHashMap<String,String>>();
            w_theta.add(w_temp);
            ArrayList<String> w_allEntailedSentences = new ArrayList<String>();

            contradiction = false;
            boolean isProvable = false;
            String w_predicateName = getPredicateName(w_query);

            if (kb != null)
            {
                ArrayList<String> w_facts = kb.getFacts();
                if (w_facts.contains(w_query) || entailedList.contains(w_query))
                {
                    isProvable = true;
                }
            }

            try
            {
                if (!isProvable)
                    w_theta = backwardChaining(negQuery, w_theta, w_allEntailedSentences, 1);
            }
            catch (StackOverflowError a_ex)
            {
                w_theta = new ArrayList<LinkedHashMap<String,String>>();
            }
            catch (Exception e)
            {
                w_theta = new ArrayList<LinkedHashMap<String,String>>();
            }

            if ((w_theta != null && w_theta.size() != 0 && contradiction) || isProvable)
            {
                entailedList.add(w_query);
                resultList.add("TRUE");
                System.out.println("TRUE");
            }
            else
            {
                    resultList.add("FALSE");
                    System.out.println("FALSE");
            }
            //kb.reset(negQuery);
        }
        return resultList;
    }

    public String getPredicateName(String a_predicate) throws Exception
    {
        return a_predicate.substring(0,a_predicate.indexOf("(")).trim();
    }
    public static String getPredicateVariables(String a_predicate) throws Exception
    {
        return a_predicate.substring(a_predicate.indexOf("(") + 1, a_predicate.indexOf(")"));
    }
    public static boolean isConstant(String a_string) throws Exception
    {
        return Character.isUpperCase(a_string.charAt(0));
    }
    public boolean isFact(String a_query) throws Exception
    {
        boolean w_isFact = true;
        int indexP = a_query.indexOf("(");
        String[] a_variables = a_query.substring(indexP+1,a_query.length()-1).split(",");
        for (int i = 0 ; i < a_variables.length; i++)
        {
            if (!Character.isUpperCase(a_variables[i].charAt(0)))
            {
                w_isFact = false;
                break;
            }
        }

        return w_isFact;
    }
    public ArrayList<LinkedHashMap<String, String>> UNIFY(Object a_rhsClause,Object a_goal, ArrayList<LinkedHashMap<String, String>> a_theta) throws Exception
    {
        if (a_theta == null)
            return null;
        if (a_rhsClause instanceof ArrayList && a_goal instanceof ArrayList)
        {
            ArrayList<String> w_tempRHS = (ArrayList<String>)a_rhsClause;
            ArrayList<String> w_tempGoal = (ArrayList<String>)a_goal;
            if (w_tempRHS.size() > 0 && w_tempRHS.size() > 0)
            {
                String firstRhsVar = w_tempRHS.remove(0);
                String firstGoalVar = w_tempGoal.remove(0);
                return UNIFY(a_rhsClause, a_goal, UNIFY(firstRhsVar, firstGoalVar, a_theta));
            }
            else
                return a_theta;
        }
        else if (a_rhsClause instanceof String && a_goal instanceof String)
        {
            String firstRhsVar = (String)a_rhsClause;
            String firstGoalVar = (String)a_goal;
            if (firstGoalVar.equals(firstRhsVar))
                return a_theta;
            else if (!isConstant(firstRhsVar))
                return UNIFY_VAR(firstRhsVar, firstGoalVar, a_theta);
            else if (!isConstant(firstGoalVar))
                return UNIFY_VAR(firstGoalVar, firstRhsVar, a_theta);
        }
        return null;
    }
    public ArrayList<LinkedHashMap<String, String>> UNIFY_VAR(String a_rhsClause, String a_goal, ArrayList<LinkedHashMap<String, String>> a_theta) throws Exception
    {
        LinkedHashMap<String, String> w_map = a_theta.get(0);
        String w_temp = w_map.get(a_rhsClause);
        String w_tempGoal = w_map.get(a_goal);

        if (w_temp != null)
            return UNIFY(w_temp, a_goal, a_theta);
        else if (w_tempGoal != null)
            return UNIFY(a_rhsClause, w_tempGoal, a_theta);
        else
        {
            w_map.put(a_rhsClause, a_goal);
            return a_theta;
        }
    }
    public static void AddPreviousThetaValues(ArrayList<LinkedHashMap<String, String>>a_AndTheta,
                                              LinkedHashMap<String, String> a_UnifyTheta) throws Exception
    {
        for (int i = 0 ; i < a_AndTheta.size() ; i++)
        {
            Iterator<String> itr = a_UnifyTheta.keySet().iterator();
            while (itr.hasNext()) {
                String w_key = itr.next();
                String w_value = a_UnifyTheta.get(w_key);
                if (!(!isConstant(w_key) && !isConstant(w_value)))
                    a_AndTheta.get(i).put(w_key, w_value);
            }
        }
    }
    public ArrayList<LinkedHashMap<String, String>> substituteVariables(ArrayList<LinkedHashMap<String, String>>a_AndTheta,ArrayList<LinkedHashMap<String, String>>a_UnifyTheta, ArrayList<String> a_variables) throws Exception
    {
        LinkedHashMap<String, String> w_map = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> w_varMap = a_UnifyTheta.get(0);

        Iterator<String> itr = w_varMap.keySet().iterator();
        while (itr.hasNext()) {
            String w_key = itr.next();
            String w_value = w_varMap.get(w_key);
            if (!isConstant(w_key) && !isConstant(w_value))
                w_map.put(w_key, w_value);
        }

        itr = w_map.keySet().iterator();
        while (itr.hasNext()) {
            String w_key = itr.next();
            String w_value = w_varMap.get(w_key);
            for (int i = 0 ; i < a_AndTheta.size() ; i++)
            {
                LinkedHashMap<String, String> w_LinkedHashMap = a_AndTheta.get(i);
                String w_MapValue = w_LinkedHashMap.get(w_key);
                if (w_MapValue != null && isConstant(w_MapValue))
                {
                    w_LinkedHashMap.put(w_value, w_MapValue);
                    w_LinkedHashMap.remove(w_key);
                }
            }
        }

        if (a_variables != null)
        {
            for (int k = 0; k < a_AndTheta.size() ; k++)
            {
                LinkedHashMap<String, String> w_tempMap = a_AndTheta.get(k);
                for (int i = 0; i < a_variables.size() ; i++)
                {
                    w_tempMap.remove(a_variables.get(i));
                }
            }
        }

        return a_AndTheta;
    }



    public ArrayList<LinkedHashMap<String,String>> backwardChaining(String a_goal,ArrayList<LinkedHashMap<String,String>> a_theta,ArrayList<String> a_entailedsentence,int a_depth) throws Exception
    {
        if (isFact(a_goal))
        {
            if (a_entailedsentence.contains(a_goal))
                return new ArrayList<LinkedHashMap<String, String>>();
            else
                a_entailedsentence.add(a_goal);
        }


        String funcName = a_goal.substring(0,a_goal.indexOf("("));
        String negfuncName,negGoal;
        ArrayList<LinkedHashMap<String,String>> theta = new ArrayList<>();
        if (funcName.charAt(0) == '~') {
            negfuncName = funcName.substring(1);
            negGoal = a_goal.substring(1);
        }
        else {
            negfuncName = "~" + funcName;
            negGoal = "~" + a_goal;
        }
        if (!CNF.containsKey(negfuncName) && !factPre.contains(negfuncName))
        {
            ArrayList<String> facts = kb.getFacts();
            if (isFact(a_goal))
            {
                if (facts.contains(negGoal) || a_entailedsentence.contains(negGoal)) {
                    contradiction =true;
                    a_entailedsentence.add(a_goal);
                    return a_theta;
                }
            }
            else
                return new ArrayList<LinkedHashMap<String, String>>();
        }
        else
        {
            ArrayList<String> currentGet= new ArrayList<>();
            if (CNF.containsKey(negfuncName)) {
                currentGet.addAll(CNF.get(negfuncName));
            }
            ArrayList<String> facts = kb.getFacts();
            ArrayList<String> selFacts = new ArrayList<>();
            for (int m = 0; m < facts.size(); m++)
            {
                String tempname = getPredicateName(facts.get(m));
                if (tempname.equals(negfuncName))
                {
                    selFacts.add(facts.get(m));
                }
            }
            ArrayList<String> rules = new ArrayList<>();
            rules.addAll(selFacts);
            rules.addAll(currentGet);
            if (isFact(a_goal))
            {
                if (facts.contains(negGoal) || a_entailedsentence.contains(negGoal)) {
                    contradiction = true;
                    a_entailedsentence.add(a_goal);
                    return a_theta;
                }
            }
            for (int i = 0; i < rules.size(); i++)
            {
                ArrayList<LinkedHashMap<String,String>> newOrTheta = new ArrayList<LinkedHashMap<String, String>>();
                LinkedHashMap<String,String> map = new LinkedHashMap<>();
                newOrTheta.add(map);
                if (i < selFacts.size())
                {
                    ArrayList<String> factVarList = new ArrayList<>(Arrays.asList(getPredicateVariables(selFacts.get(i)).split(",")));
                    ArrayList<String> goalVarList = new ArrayList<>(Arrays.asList(getPredicateVariables(a_goal).split(",")));

                    ArrayList<LinkedHashMap<String,String>> newTheta = UNIFY(factVarList,goalVarList,newOrTheta);

                    if (newTheta != null)
                    {
                        contradiction = true;
                        AddPreviousThetaValues(newTheta,a_theta.get(0));
                        newTheta = substituteVariables(newTheta,a_theta,null);
                        theta.addAll(newTheta);
                    }
                }
                else
                {
                    ArrayList<String> allEntailedList = new ArrayList<String>();
                    allEntailedList.addAll(a_entailedsentence);
                    int tempindex = 0;
                    String[] tempc = currentGet.get(i-selFacts.size()).split("\\|");
                    for (int k = 0; k < tempc.length; k++)
                    {
                        if (tempc[k].indexOf(negfuncName) == 0)
                            tempindex = k;
                    }

                    ArrayList<String> factVarList = new ArrayList<>(Arrays.asList(tempc[tempindex].substring(tempc[tempindex].indexOf("(")+1,tempc[tempindex].length()-1).split(",")));
                    ArrayList<String> goalVarList = new ArrayList<>(Arrays.asList(getPredicateVariables(a_goal).split(",")));
                    ArrayList<LinkedHashMap<String,String>> newTheta = UNIFY(factVarList,goalVarList,newOrTheta);

                    ArrayList<String> goals = new ArrayList<>();
                    String[] unit = currentGet.get(i - selFacts.size()).split("\\|");
                    for (int j = 0; j < unit.length; j++)
                    {
                        if (unit[j].indexOf(negfuncName) != 0)
                        {
                            goals.add(unit[j]);
                        }
                    }

                    if (newTheta != null)
                    {
                        ArrayList<LinkedHashMap<String,String>> tempTheta =  subFOL(goals,newTheta,a_depth+1,a_entailedsentence);
                        tempTheta = substituteVariables(tempTheta,newTheta,factVarList);
                        AddPreviousThetaValues(tempTheta, a_theta.get(0));
                        theta.addAll(tempTheta);

                        if (a_depth == 1 && tempTheta.size() == 1)
                        {
                            return theta;
                        }
                    }
                }
            }
        }
        return theta;
    }
    public ArrayList<LinkedHashMap<String, String>> subFOL(ArrayList<String> a_goals, ArrayList<LinkedHashMap<String, String>> a_theta, int a_level, ArrayList<String> a_allEntailedSentences) throws Exception
    {
        ArrayList<LinkedHashMap<String, String>> w_theta = new ArrayList<LinkedHashMap<String,String>>();
        if (a_goals.size() == 0)
            return a_theta;
        String w_firstGoal = a_goals.remove(0);
        for (int i = 0 ; i < a_theta.size(); i++)
        {
            LinkedHashMap<String, String> w_temporaryMap = a_theta.get(i);
            String w_newGoal = substitureTheta(w_firstGoal, w_temporaryMap);
            String w_negGoal;
            ArrayList<String> w_allEntailedList = new ArrayList<String>();
            w_allEntailedList.addAll(a_allEntailedSentences);


            ArrayList<LinkedHashMap<String, String>> w_tempTheta = backwardChaining(w_newGoal, a_theta, w_allEntailedList, a_level);
            if (a_goals.size() > 0)
            {
                for (int j = 0 ; j < w_tempTheta.size() ; j++)
                {
                    w_allEntailedList = new ArrayList<String>();
                    w_allEntailedList.addAll(a_allEntailedSentences);

                    ArrayList<String> w_goals = new ArrayList<String>();
                    w_goals.addAll(a_goals);
                    ArrayList<LinkedHashMap<String, String>> w_new = new ArrayList<LinkedHashMap<String, String>>();
                    w_new.add(w_tempTheta.get(j));
                    w_theta.addAll(subFOL(w_goals, w_new, a_level ,w_allEntailedList));
                }
            }
            else
                w_theta.addAll(w_tempTheta);
        }

        return w_theta;
    }

    public String substitureTheta(String a_goal, LinkedHashMap<String,String> a_map) throws Exception
    {
        String w_predicateName = getPredicateName(a_goal) + "(";
        String[] w_variable = getPredicateVariables(a_goal).split(",");


        for (Map.Entry<String, String> entry : a_map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            for (int i = 0 ; i < w_variable.length ; i++)
            {
                if (w_variable[i].equals(key))
                {
                    w_variable[i] = value;
                }
            }
        }

        for (int i = 0; i < w_variable.length ; i++)
            w_predicateName +=  w_variable[i] + "," ;

        if (w_predicateName.endsWith(","))
            w_predicateName = w_predicateName.substring(0, w_predicateName.length()-1);
        return (w_predicateName + ")");

    }

}
