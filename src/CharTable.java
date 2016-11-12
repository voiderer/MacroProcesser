import java.util.TreeMap;

/**
 * Created by autoy on 2015/11/27.
 */
public class CharTable {
    TreeMap<Character,Object> map;
    public CharTable(String s)
    {
        map=new TreeMap<>();
        for(int i=0;i<s.length();i++)
        {
            map.put(s.charAt(i),null);
        }
    }
    boolean isInclude(Character c)
    {
        return map.containsKey(c);
    }
}
