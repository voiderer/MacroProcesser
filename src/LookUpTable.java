import java.util.TreeMap;

/**
 * Created by autoy on 2015/11/27.
 */
public class LookUpTable {
    TreeMap<String,Object> map;

    public LookUpTable(String[] list)
    {
        map=new TreeMap<>();
        for(String s :list)
        {
            map.put(s,null);
        }
    }

    void add(String[] list)
    {
        for(String s :list)
        {
            map.put(s,null);
        }
    }
    boolean isInclude(String s)
    {
        return map.containsKey(s);
    }
}
