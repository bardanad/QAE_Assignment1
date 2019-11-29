import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;


public class Script {
    private HashSet<String> names;
    private TreeMap<String, Integer> substringToFreq;
    private HashSet<String>[] lengthToSubstrings;
    private char[][] freqPrevChar;
    private char[] firstLetterFreq;
    final int LONGEST_NAME = 500;
    final int LENGTH_OF_AUTO_GENERATED_NAME = 5;


    private Script() {
        names = new HashSet<>();
        lengthToSubstrings = new HashSet[LONGEST_NAME];
        substringToFreq = new TreeMap<>();
        /* The next parameters are to support auto-name creation functionality. */
        freqPrevChar = new char[26][26];
        firstLetterFreq = new char[26];
    }

    private int countSpecificString(String string) {
        return (substringToFreq.getOrDefault(string, 0));
    }

    private void CountAllStrings(int n) {
        if (lengthToSubstrings[n] == null)
            return;
        HashSet strings = lengthToSubstrings[n];
        for (Object s : strings) {
            System.out.println(s + ":" + n);
        }
    }

    private HashSet<String> CountMaxString(int n) {
        HashSet<String> substrings = lengthToSubstrings[n];
        TreeMap<Integer, HashSet<String>> freqToSubstrings = new TreeMap<>();

        for (String substring : substrings) {
            substring = substring.toLowerCase();
            int freq = 0;
            /* Ignoring case-sensitive issue, We sum the frequency*/
            if (substringToFreq.containsKey(substring))
                freq += substringToFreq.get(substring);
            if (substringToFreq.containsKey(substring.toUpperCase()))
                freq += substringToFreq.get(substring.toUpperCase());
            if (freqToSubstrings.containsKey(freq)) {
                HashSet<String> oldSubstrings = freqToSubstrings.get(freq);
                oldSubstrings.add(substring);
                freqToSubstrings.put(freq, oldSubstrings);
            } else {
                HashSet<String> newSubstrings = new HashSet<>();
                newSubstrings.add(substring);
                freqToSubstrings.put(freq, newSubstrings);
            }
        }

        HashSet<String> res = freqToSubstrings.pollLastEntry().getValue();
        for (String subString : res) {
            System.out.println(subString);
        }
        return res;
    }

    private ArrayList<String> AllIncludesString(String string) {
        ArrayList<String> res = new ArrayList<>();
        string = string.toLowerCase();
        for (String name : names) {
            name = name.toLowerCase();
            if (string.contains(name)) {
                System.out.println(name);
                res.add(name);
            }
        }
        return res;
    }

    private String GenerateName(){
        StringBuilder sb = new StringBuilder();
        char currentChar = getFrequentChar(firstLetterFreq);
        sb.append(Character.toUpperCase(currentChar));

        for (int i = 1; i < LENGTH_OF_AUTO_GENERATED_NAME; i++){
            char nextChar = getCharByPrevFreq(currentChar);
            sb.append(nextChar);
            currentChar = nextChar;
        }
        return null;
    }

    private char getCharByPrevFreq(char prefChar) {
        prefChar = Character.toLowerCase(prefChar);
        return 't';
    }

    private char getFrequentChar(char[] charArray){
        return 't';
    }


    private void parseSite() {
        try {
            String prefixUrl = "https://www.behindthename.com";
            Document doc = Jsoup.connect("https://www.behindthename.com/names/usage/english").get();
            ArrayList<String> urls = new ArrayList<String>();
            Elements webPagesHrefElements = doc.getElementsByAttributeValueContaining("href", "/names/usage/english/");
            urls.add("/names/usage/english");
            for (Element urlElement : webPagesHrefElements)
                urls.add(urlElement.attr("href"));
            for (int i = 0; i < urls.size(); i++) {
                String suffixUrl = urls.get(i);
                String fullUrl = prefixUrl + suffixUrl;
                Document pageDoc = Jsoup.connect(fullUrl).get();
                Elements nameElements = pageDoc.getElementsByClass("listName");
                for (Element nameElement : nameElements) {
                    String name = parseFullLine(nameElement);
                    addName(name);
                }
            }
            System.out.println("done");
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }


    private void addName(String name) {
        subString(name);
        names.add(name);
    }

    // Function to print all sub strings
    private void subString(String name) {
        char[] str = name.toCharArray();
        firstLetterFreq[Character.toLowerCase(str[0]) - 97] += 1;
        if (name.length() > 1){
            freqPrevChar[str[1] - 97][Character.toLowerCase(str[0]) - 97] += 1;
        }
        for (int i = 2; i < str.length; i++){
            freqPrevChar[str[i] - 97][str[i-1] - 97] += 1;
        }
        int n = name.length();
        // Pick starting point
        for (int len = 1; len <= n; len++) {
            // Pick ending point
            for (int i = 0; i <= n - len; i++) {
                String tmp = "";
                //  Print characters from current
                // starting point to current ending
                // point.
                int j = i + len - 1;
                for (int k = i; k <= j; k++) {
                    tmp += str[k];
                }
                System.out.println(tmp);
                addSubstringToDS(tmp);
            }
        }
    }

    private void addSubstringToDS(String tmp) {
        addToLengthToSubstrings(tmp);
        addToSubstringsToFreq(tmp);

    }

    private void addToSubstringsToFreq(String tmp) {
        if (substringToFreq.containsKey(tmp)) {
            int currFreq = substringToFreq.get(tmp);
            substringToFreq.put(tmp, currFreq + 1);
        } else {
            substringToFreq.put(tmp, 1);
        }
    }

    private void addToLengthToSubstrings(String tmp) {
        int length = tmp.length();
        if (lengthToSubstrings[length] != null) {
            HashSet<String> substrings = lengthToSubstrings[length];
            substrings.add(tmp);
            lengthToSubstrings[length] = substrings;
        } else {
            HashSet<String> substrings = new HashSet<String>();
            substrings.add(tmp);
            lengthToSubstrings[length] = substrings;
        }
    }

    private String parseFullLine(Element nameElement) {
        String nameFullLine = nameElement.toString();
        int startIndexOfName = nameFullLine.lastIndexOf("/name/");
        String trimmedLine = nameFullLine.substring(startIndexOfName + 1);
        int firstIndexOfSlash = trimmedLine.indexOf("/");
        int firstIndexOfQuote = trimmedLine.indexOf('"');
        String name = trimmedLine.substring(firstIndexOfSlash + 1, firstIndexOfQuote);
        name = name.toLowerCase();
        name = name.replaceAll("[^a-zA-Z]", "");
        char[] arrName = name.toCharArray();
        arrName[0] = Character.toUpperCase(arrName[0]);
        return new String(arrName);
    }


    public static void main(String[] args) {
        Script s = new Script();
        //s.parseSite();
        s.addName("Assaf");
        s.addName("Yarden");
        s.addName("Yaaaen");
        System.out.println(s.countSpecificString("Ya"));
        System.out.println(s.countSpecificString("As"));
        s.CountAllStrings(2);
        s.AllIncludesString("mkgddassafbadayardenfg");
        s.CountMaxString(1);
        s.GenerateName();
    }

}





