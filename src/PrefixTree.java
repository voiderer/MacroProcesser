import java.util.TreeMap;

/**
 * Created by autoy on 2015/11/28.
 */
public class PrefixTree {
    PrefixNode root;

    PrefixTree(String[] strings) {
        root = new PrefixNode();
        for (String s : strings) {
            insert(s);
        }
    }

    boolean insert(String s) {
        PrefixNode pointer = root;
        Character a;
        for (int i = 0; i < s.length(); i++) {
            a = s.charAt(i);
            if (pointer.children.containsKey(a)) {
                pointer = pointer.children.get(a);
            } else {
                pointer.children.put(a, new PrefixNode());
                pointer=pointer.children.get(a);
            }
        }
        if (pointer.isExist) {
            return false;
        } else {
            return pointer.isExist = true;
        }
    }
}
class PrefixNode{
    TreeMap<Character,PrefixNode> children;
    boolean isExist;
    PrefixNode()
    {
        isExist=false;
       children=new TreeMap<>();
    }
}