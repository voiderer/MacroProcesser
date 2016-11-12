import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by autoy on 2015/11/27.
 */
public class Processor {
    static String keywordType[] = {"struct", "void", "union", "enum", "char", "short", "int", "long", "double", "float"};
    static String keywordControl[] = {"if", "else", "switch", "case", "default", "break", "goto", "return", "for", "while", "do", "continue", "typedef", "sizeof"};
    static String keywordRestriction[] = {"signed", "unsigned", "const", "static", "extern", "auto", "register", "volatile"};
    static LookUpTable restrictionTable = new LookUpTable(keywordRestriction);
    static LookUpTable controlTable = new LookUpTable(keywordControl);
    static LookUpTable typeTable = new LookUpTable(keywordType);
    static String preprocessor[] = {"define", "ifdef", "else", "endif", "elif", "pragma", "undef"};
    static LookUpTable preprocessorTable = new LookUpTable(preprocessor);
    static CharTable identifierHeadTable = new CharTable("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_");
    static CharTable identifierBodyTable = new CharTable("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_0123456789");
    static CharTable figureHeadTable = new CharTable("0123456789");
    static CharTable whitespaceTable = new CharTable(" \n\t\r\f");
    static CharTable operatorElement = new CharTable("+-|&*=%!^()[]<>,~.?:");
    static CharTable identifierFollowTable = new CharTable("{}()[]|\\,/<>~!@#%^&*() +-=;.:?\"\'\n\t\n\f");
    static String[] operators = {"+", "+=", "++", "-", "-=", "--", "->", "|", "||", "|=", "!", "!=", "&", "&&", "&=", "*", "*=", "=", "==", "%", "%=", "^", "^=", "[", "]", "(", ")", "<", "<=", "<<", "<<=", ">", ">>", ">=", ">>=", ",", "~", ".", "?", ",", ":"};
    static PrefixTree symbolTree = new PrefixTree(operators);
    TreeMap<WordType, Property> classificationMap;
    TreeMap<String, VariableWord> macroMap;
    TreeMap<String, FunctionWord> fuctionMap;
    LinkedList<Word> list;
    VariableBlock variableRoot, currentBlock;

    Processor() {
        list = new LinkedList<>();
        classificationMap = new TreeMap<>();
        macroInitialize();
        currentBlock = variableRoot = new VariableBlock(null);
        classificationMap.put(WordType.BAD, new Property(Color.red));
        classificationMap.put(WordType.FIGURE, new Property(new Color(187, 146, 33)));
        classificationMap.put(WordType.OPERATOR, new Property(new Color(135, 194, 203)));
        classificationMap.put(WordType.FUNCTION, new Property(new Color(255, 128, 0)));
        classificationMap.put(WordType.IDENTIFIER, new Property(new Color(189, 64, 107)));
        classificationMap.put(WordType.KEYWORD, new Property(new Color(86, 156, 214)));
        classificationMap.put(WordType.MACRO, new Property(new Color(189, 99, 197)));
        classificationMap.put(WordType.STRING, new Property(new Color(214, 157, 113)));
        classificationMap.put(WordType.BRACE, new Property(new Color(121, 249, 211)));
        classificationMap.put(WordType.PREPROCESSOR, new Property(new Color(155, 155, 155)));
        classificationMap.put(WordType.STOP, new Property(new Color(121, 249, 211)));
        classificationMap.put(WordType.COMMENT, new Property(new Color(87, 166, 74)));
        classificationMap.put(WordType.FILE, new Property(new Color(214, 134, 93)));
        classificationMap.put(WordType.VARIABLE, new Property(new Color(120, 141, 189)));
        classificationMap.put(WordType.ATTRIBUTE, new Property(new Color(189, 177, 85)));
        classificationMap.put(WordType.CLASS, new Property(new Color(70, 189, 143)));
        classificationMap.put(WordType.RESTRICTION, new Property(new Color(86, 190, 214)));
        classificationMap.put(WordType.CONTROL, new Property(new Color(86, 190, 214)));
        classificationMap.put(WordType.TYPENAME, new Property(new Color(86, 190, 214)));
    }

    void macroInitialize()
    {
        macroMap=new TreeMap<>();
        macroMap.put("NULL",new VariableWord("NULL", WordType.MACRO));

    }

    void analyze(JTextPane area) {
        String content = area.getText();
        Pattern wordPattern = Pattern.compile("\r");
        Matcher matcher = wordPattern.matcher(content);
        content = matcher.replaceAll("");
        list.clear();
        scan(content);
        interpret(content);
        setColor(area.getStyledDocument());
    }

    void scan(String code) {
        int cursor = 0, end, i;
        Word word;
        char a;
        String string;
        while (cursor != code.length()) {
            a = code.charAt(cursor);
            if (a == '#') {
                end = lookWithInclude(code, cursor + 1, identifierBodyTable);
                string = code.substring(cursor + 1, end);
                if (wordCompare(code, cursor + 1, end, "include")) {
                    word = add(cursor, end, WordType.PREPROCESSOR);
                    cursor = lookWithInclude(code, end, whitespaceTable);
                    if (cursor == code.length()) {
                        word.isUnderline = true;
                        return;
                    }
                    a = code.charAt(cursor);
                    if (a == '\"') {
                        end = cursor + 1;
                        word.isUnderline = true;
                        while (end != code.length()) {
                            a = code.charAt(end);
                            if (a == '"') {
                                add(cursor, end + 1, WordType.STRING);
                                end++;
                                word.isUnderline = false;
                                break;
                            } else if (whitespaceTable.isInclude(a)) {
                                add(cursor, end, WordType.BAD);
                                break;
                            }
                            end++;
                        }

                    } else if (a == '<') {
                        end = cursor + 1;
                        word.isUnderline = true;
                        while (end != code.length()) {
                            a = code.charAt(end);
                            if (a == '>') {
                                add(cursor, end + 1, WordType.STRING);
                                end++;
                                word.isUnderline = false;
                                break;
                            } else if (whitespaceTable.isInclude(a)) {
                                add(cursor, end, WordType.BAD);
                                word.isUnderline = true;
                                break;
                            }
                            end++;
                        }
                    } else {
                        word.isUnderline = true;
                    }
                } else if (preprocessorTable.isInclude(string)) {
                    add(cursor, end, WordType.PREPROCESSOR);
                } else {
                    add(cursor, end, WordType.BAD);
                }
                cursor = lookWithInclude(code, end, whitespaceTable);
            } else if (a == '\"') {
                end = cursor + 1;
                while (end < code.length()) {
                    a = code.charAt(end);
                    end++;
                    if (a == '\n') {
                        break;
                    } else if (a == '\"') {
                        i = 2;
                        while (code.charAt(end - i) == '\\') {
                            i++;
                        }
                        if (i % 2 == 0) {
                            break;
                        }
                    }

                }
                add(cursor, end, WordType.STRING);
                cursor = lookWithInclude(code, end, whitespaceTable);
            } else if (a == '\'') {
                end = cursor + 1;
                while (end < code.length()) {
                    a = code.charAt(end);
                    end++;
                    if (a == '\n') {
                        break;
                    } else if (a == '\'') {
                        i = 2;
                        while (code.charAt(end - i) == '\\') {
                            i++;
                        }
                        if (i % 2 == 0) {
                            break;
                        }
                    }
                }
                add(cursor, end, WordType.STRING);
                cursor = lookWithInclude(code, end, whitespaceTable);
            } else if (a == '/') {
                if (cursor + 1 == code.length()) {
                    add(cursor, cursor + 1, WordType.BAD);
                    return;
                }
                a = code.charAt(cursor + 1);
                if (a == '*') {
                    end = lookWithString(code, cursor + 2, "*/");
                    add(cursor, end, WordType.COMMENT);
                    cursor = lookWithInclude(code, end, whitespaceTable);
                } else if (a == '/') {
                    end = lookWithString(code, cursor + 2, "\n");
                    add(cursor, end, WordType.COMMENT);
                    cursor = lookWithInclude(code, end, whitespaceTable);
                } else {
                    add(cursor, cursor + 1, WordType.OPERATOR);
                    cursor = lookWithInclude(code, cursor + 1, whitespaceTable);
                }
            } else if (a == '{' || a == '}') {
                add(cursor, cursor + 1, WordType.BRACE);
                cursor = lookWithInclude(code, cursor + 1, whitespaceTable);
            } else if (a == ';') {
                add(cursor, cursor + 1, WordType.STOP);
                cursor = lookWithInclude(code, cursor + 1, whitespaceTable);
            } else if (operatorElement.isInclude(a)) {
                i = 1;
                if(a=='(')
                {
                    word=list.getLast();
                    if(word.type== WordType.IDENTIFIER)
                    {
                        word.type= WordType.FUNCTION;
                    }
                }
                PrefixNode node = symbolTree.root.children.get(a);
                try {
                    while (cursor + i < code.length() && node.children.containsKey(code.charAt(cursor + i))) {
                        node = node.children.get(code.charAt(cursor + i));
                        i++;
                    }
                } catch (NullPointerException e) {
                    System.out.print(a);
                }
                if (node.isExist) {
                    add(cursor, cursor + i, WordType.OPERATOR);
                    cursor = lookWithInclude(code, cursor + i, whitespaceTable);
                } else {
                    add(cursor, cursor + i, WordType.BAD);
                    cursor = lookWithInclude(code, cursor + i, whitespaceTable);
                }
            } else if (identifierHeadTable.isInclude(a)) {
                end = lookWithExclude(code, cursor + 1, identifierFollowTable);
                string = code.substring(cursor, end);
                if (!string.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    add(cursor, end, WordType.BAD);
                } else if (restrictionTable.isInclude(string)) {
                    add(cursor, end, WordType.RESTRICTION);
                } else if (controlTable.isInclude(string)) {
                    add(cursor, end, WordType.CONTROL);
                } else if (typeTable.isInclude(string)) {
                    add(cursor, end, WordType.TYPENAME);
                } else {
                    add(cursor, end, WordType.IDENTIFIER);
                }
                cursor = lookWithInclude(code, end, whitespaceTable);
            } else if (figureHeadTable.isInclude(a)) {
                cursor = searchFigure(code, cursor);
            } else {
                end = lookWithExclude(code, cursor + 1, identifierFollowTable);
                add(cursor, end, WordType.BAD);
                cursor = lookWithInclude(code, end, whitespaceTable);
            }
        }
    }

    void interpret(String code) {
        Iterator<Word> cursor = list.iterator();
        Word word, word1;
        LinkedList<Word> restrictions=new LinkedList<>();
        String string,string1;
        if (!cursor.hasNext())
            return;
        word = cursor.next();
        while (cursor.hasNext()) {
            if (word.type == WordType.PREPROCESSOR) {
                word1 = word;
                string=code.substring(word1.start, word1.end);
                if(!cursor.hasNext())
                    return;
                word = cursor.next();
                if (string.equals("#define") ) {
                    if (word.type == WordType.IDENTIFIER) {
                        word.type = WordType.MACRO;
                    }
                    string = code.substring(word.start, word.end);
                    if (macroMap.containsKey(string)) {
                        word.isUnderline = true;
                    } else {
                        macroMap.put(string, new VariableWord(string, WordType.MACRO));
                    }
                    while (cursor.hasNext()) {
                        word = cursor.next();
                        if (word.type == WordType.IDENTIFIER) {
                            string1 = code.substring(word.start, word.end);
                            if (macroMap.containsKey(string1) && (macroMap.get(string1)).type.equals(WordType.MACRO)) {
                                word.type = WordType.MACRO;
                                if(string1.equals(string))
                                {
                                    word.isUnderline=true;
                                }
                            } else {
                                word.isUnderline = true;
                            }
                        } else if (word.type != WordType.FIGURE && word.type != WordType.OPERATOR) {
                            break;
                        }
                    }
                }
            } else if (word.type == WordType.RESTRICTION) {
                restrictions=new LinkedList<>();
                while(cursor.hasNext()) {
                    if(word.type!= WordType.RESTRICTION)
                        break;
                    restrictions.addLast(word);
                    word=cursor.next();
                }
            } else if (word.type == WordType.CONTROL) {
                word = cursor.next();
            } else if (word.type == WordType.TYPENAME) {
                /*string=code.substring(word.start,word.end);
               if(string.equals("struct"))
                {
                    word=cursor.next();
                    string=code.substring(word.start,word.end);
                    if (word.type== WordType.OPERATOR)
                    {

                    }
                    else if (word.type==WordType.IDENTIFIER)
                    {

                    }
                    else if ()

                }
                else if(string.equals("union"))
                {

                }
                else  if(string.equals("void")){

                }
                else if(string.equals("enum"))
                {

                }*/
                word = cursor.next();
            } else if (word.type == WordType.IDENTIFIER) {
                if(macroMap.containsKey(code.substring(word.start,word.end)))
                {
                    word.type=WordType.MACRO;
                }
                word = cursor.next();
            } else {
                word = cursor.next();
            }
        }
    }


    boolean wordCompare(String code, int start, int end, String s) {
        if (s.length() != end - start) {
            return false;
        }
        for (int i = 0; i < s.length(); i++, start++) {
            if (code.charAt(start) != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    int lookWithInclude(String code, int cursor, CharTable include) {
        while (cursor != code.length() && include.isInclude(code.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    int lookWithExclude(String code, int cursor, CharTable exclude) {
        while (cursor != code.length() && !exclude.isInclude(code.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    int lookWithString(String code, int cursor, String target) {
        int i, j;
        char a, b;
        boolean flag;
        for (i = cursor; i < code.length(); i++) {
            flag = true;
            for (j = 0; j < target.length(); j++) {
                a = code.charAt(i + j);
                b = target.charAt(j);
                if (a != b) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return i + j;
            }
        }
        return i;
    }

    int searchFigure(String code, int cursor) {
        char a;
        int end = cursor + 1;
        while (end != code.length() && figureHeadTable.isInclude(code.charAt(end))) {
            end++;
        }
        if (end == code.length()) {
            add(cursor, end, WordType.FIGURE);
            return end;
        }
        a = code.charAt(end);
        if (a == 'e' || a == 'E') {
            end++;
            if (end != code.length() && code.charAt(end) == '-') {
                end++;
            }
            if (end == code.length()) {
                add(cursor, end, WordType.BAD);
                return end;
            } else if (!figureHeadTable.isInclude(code.charAt(end))) {
                end = lookWithExclude(code, end, identifierFollowTable);
                add(cursor, end, WordType.BAD);
            } else {
                while (end != code.length() && figureHeadTable.isInclude(code.charAt(end))) {
                    end++;
                }
                if (end == code.length() || identifierFollowTable.isInclude(code.charAt(end))) {
                    add(cursor, end, WordType.FIGURE);
                } else {
                    end = lookWithExclude(code, end, identifierFollowTable);
                    add(cursor, end, WordType.BAD);
                }
            }
        } else if (a == '.') {
            end++;
            if (end == code.length()) {
                add(cursor, end, WordType.BAD);
                return end;
            }
            if (!figureHeadTable.isInclude(code.charAt(end))) {
                end = lookWithExclude(code, end, identifierFollowTable);
                add(cursor, end, WordType.BAD);
            } else {
                while (end != code.length() && figureHeadTable.isInclude(code.charAt(end))) {
                    end++;
                }
                if (end == code.length()) {
                    add(cursor, end, WordType.FIGURE);
                    return end;
                }
                if (code.charAt(end) == 'f' || code.charAt(end) == 'F') {
                    end++;
                }
                if (end == code.length()) {
                    add(cursor, end, WordType.FIGURE);
                    return end;
                }
                if (identifierFollowTable.isInclude(code.charAt(end))) {
                    add(cursor, end, WordType.FIGURE);
                } else {
                    end = lookWithExclude(code, end, identifierFollowTable);
                    add(cursor, end, WordType.BAD);
                }
            }
        } else if (a == 'x' || a == 'X' && end == cursor + 1) {
            end++;
            if (end != code.length() && !figureHeadTable.isInclude(code.charAt(end))) {
                end = lookWithExclude(code, end, identifierFollowTable);
                add(cursor, end, WordType.BAD);
            } else {
                while (end != code.length() && figureHeadTable.isInclude(code.charAt(end))) {
                    end++;
                }
                if (end == code.length() || identifierFollowTable.isInclude(code.charAt(end))) {
                    add(cursor, end, WordType.FIGURE);
                } else {
                    end = lookWithExclude(code, end, identifierFollowTable);
                    add(cursor, end, WordType.BAD);
                }
            }
        } else if (identifierFollowTable.isInclude(a)) {
            add(cursor, end, WordType.FIGURE);
        } else {
            end = lookWithExclude(code, end, identifierFollowTable);
            add(cursor, end, WordType.BAD);

        }
        return lookWithInclude(code, end, whitespaceTable);
    }

    Word add(int start, int end, WordType type) {
        Word word;
        list.addLast(word = new Word(start, end, type));
        return word;
    }

    void setColor(StyledDocument doc) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        for (Word w : list) {
            StyleConstants.setForeground(attr, (classificationMap.get(w.type)).color);
            StyleConstants.setUnderline(attr, w.isUnderline);
            doc.setCharacterAttributes(w.start, w.end - w.start, attr, true);
        }

    }
}

enum WordType {
    BAD,
    OPERATOR,
    FIGURE,
    FUNCTION,
    IDENTIFIER,
    KEYWORD,
    RESTRICTION,
    TYPENAME,
    CONTROL,
    MACRO,
    STRING,
    BRACE,
    PREPROCESSOR,
    STOP,
    COMMENT,
    FILE,
    CLASS,
    VARIABLE,
    ATTRIBUTE
}

class Word {
    int start;
    int end;
    WordType type;
    boolean isUnderline;
    Word(int start, int end, WordType type) {
        this.start = start;
        this.end = end;
        this.type = type;
        isUnderline=false;
    }
    Word(int start, int end, WordType type,boolean isUnderline) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.isUnderline=isUnderline;
    }
}

class Property {
    Color color;
    Font font;
    Property(Color color) {
        this.color = color;
    }
}

class VariableWord {
    TreeMap<WordType, Object> restriction;
    WordType type;
    String name;
    VariableWord(String name, WordType type) {
        this.restriction = new TreeMap<>();
        this.name = name;
        this.type = type;
    }
    VariableWord(String name, WordType type,LinkedList<WordType> restrictions)
    {
        this.restriction=new TreeMap<>();
        this.name=name;
        this.type=type;
        for(WordType w: restrictions)
        {
            restriction.put(w,null);
        }
    }

}

class ClassWord {
    LinkedList<VariableWord> attributes;

    String name;

    ClassWord(String name) {
        this.name = name;
    }
}
class FunctionWord {
    boolean hasImplemented;
    String name;
    LinkedList<VariableWord> parameters;

    FunctionWord(String name, boolean hasImplemented) {
        this.name = name;
        this.hasImplemented = hasImplemented;
    }

    void addParameter(VariableWord word) {
        parameters.addLast(word);
    }
}
class VariableBlock {
    LinkedList<ClassWord> classWords;
    LinkedList<VariableWord> variableWords;
    LinkedList<VariableWord> parameterWords;
    VariableBlock parent;
    LinkedList<VariableBlock> children;

    VariableBlock(VariableBlock parent) {
        this.parent = parent;
    }

    void clear() {
    }

    boolean containsClass(String s) {
        for (ClassWord c : classWords) {
            if (Objects.equals(c.name, s)) {
                return true;
            }
        }
        return false;
    }

    void addClass(ClassWord classWord) {
        classWords.addLast(classWord);
    }

    boolean containsVariable(String s) {
        for (VariableWord v : variableWords) {
            if (v.name.equals(s)) {
                return true;
            }
        }
        for (VariableWord v : parameterWords) {
            if (v.name.equals(s)) {
                return true;
            }
        }
        return false;
    }

    void addVariable(VariableWord word) {
        variableWords.add(word);
    }

    void addParameter(VariableWord word) {
        parameterWords.add(word);
    }
}