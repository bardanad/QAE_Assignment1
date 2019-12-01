import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;


public class Script {
    private HashSet<String> names;
    private TreeMap<String, Integer> substringToFreq;
    private HashSet<String>[] lengthToSubstrings;
    private FileWriter fw;
    private BufferedWriter bw;
    private char[][] freqPrevChar;
    private char[] firstLetterFreq;
    private final int LONGEST_NAME = 500;
    private final int LENGTH_OF_AUTO_GENERATED_NAME = 6;


    private Script() {
        names = new HashSet<>();
        lengthToSubstrings = new HashSet[LONGEST_NAME];
        substringToFreq = new TreeMap<>();
        /* The next parameters are to support auto-name creation functionality. */
        freqPrevChar = new char[26][26];
        firstLetterFreq = new char[26];
        try {
            PrintWriter write = new PrintWriter("fullLog.txt");
            write.print("");
            write.close();
            fw = new FileWriter("fullLog.txt");
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int CountSpecificString(String string) {
        System.out.println(substringToFreq.getOrDefault(string, 0));
        return (substringToFreq.getOrDefault(string, 0));
    }

    private void CountAllStrings(int n) {
        if (lengthToSubstrings[n] == null)
            return;
        HashSet strings = lengthToSubstrings[n];
        System.out.println("Results <<<<<<<CountAllStrings with n = " + n + ">>>>>>>");
        try {
            bw.write("Results <<<<<<<CountAllStrings with n = " + n + ">>>>>>>" + "\n");
            TreeSet sortedStrings = new TreeSet<>(strings);
            for (Object s : sortedStrings) {
                int freq = substringToFreq.get(s.toString());
                bw.write(s + ":" + freq + "\n");
            }
            bw.flush();
            System.out.println("Total <<<<<<<CountAllStrings with n = " + n + " Total substrings: " + strings.size() + " SEE FULL RESULTS ON fullLog.txt>>>>>>>");
            bw.write("Total <<<<<<<CountAllStrings with n = " + n + " Total substrings: " + strings.size() + ">>>>>>>");
            System.out.println("---------------------------------------------------------");
            bw.write("---------------------------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashSet<String> CountMaxString(int n) {
        HashSet<String> substrings;
        substrings = lengthToSubstrings[n];
        TreeMap<Integer, HashSet<String>> freqToSubstrings;
        freqToSubstrings = new TreeMap<>();

        for (String substring : substrings) {
            int freq = 0;
            if (substringToFreq.containsKey(substring))
                freq += substringToFreq.get(substring);
            /* Ignoring case-sensitive issue, We sum the frequency*/
            if (Character.isUpperCase(substring.charAt(0))) {
                substring = substring.toLowerCase();
                if (substringToFreq.containsKey(substring))
                    freq += substringToFreq.get(substring);

            }
            /* Checks if the specific freq already has an entry on data structure */
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

        /* Polls last entry -> maximum freq value */
        HashSet<String> res = freqToSubstrings.pollLastEntry().getValue();
        System.out.println("Results <<<<<<<CountMaxString with n = " + n + ">>>>>>>");
        for (String subString : res) {
            System.out.println(subString);
        }
        System.out.println("---------------------------------------------------------");
        return res;
    }

    private ArrayList<String> AllIncludesString(String string) {
        ArrayList<String> res = new ArrayList<>();
        string = string.toLowerCase();
        System.out.println("Results <<<<<<<AllIncludesString with String = " + string + ">>>>>>>");
        for (String name : names) {
            name = name.toLowerCase();
            if (string.contains(name)) {
                System.out.println(name);
                res.add(name);
            }
        }
        System.out.println("---------------------------------------------------------");
        return res;
    }

    private String GenerateName() {
        StringBuilder sb;
        sb = new StringBuilder();
        char currentChar = getFrequentChar(firstLetterFreq).getKey();
        sb.append(Character.toUpperCase(currentChar));
        for (int i = 1; i < LENGTH_OF_AUTO_GENERATED_NAME; i++) {
            char nextChar = getCharByPrevFreq(Character.toLowerCase(currentChar));
            sb.append(nextChar);
            currentChar = nextChar;
        }
        System.out.println("Results <<<<<<<GenerateName with length = " + LENGTH_OF_AUTO_GENERATED_NAME + ">>>>>>>");
        System.out.println(sb.toString());
        System.out.println("---------------------------------------------------------");
        return sb.toString();
    }

    private char getCharByPrevFreq(char prevChar) {
        prevChar = Character.toLowerCase(prevChar);
        int max = Integer.MIN_VALUE;
        char c = 0;
        for (int i = 0; i < 26; i++) {
            char[] charArray = freqPrevChar[i];
            if (charArray[prevChar - 97] > max) {
                max = charArray[prevChar - 97];
                c = (char) (i + 97);
            }
        }
        return c;
    }

    private Pair<Character, Integer> getFrequentChar(char[] charArray) {
        int max = Integer.MIN_VALUE;
        char c = 0;
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] > max) {
                max = charArray[i];
                c = ((char) (i + 97));
            }
        }
        return new Pair(c, max);
    }

    private void parseSite() {
        try {
            String prefixUrl = "https://www.behindthename.com";
            Document doc = Jsoup.connect("https://www.behindthename.com/names/usage/english").get();
            ArrayList<String> suffixes = new ArrayList<String>();
            Elements webPagesHrefElements = doc.getElementsByAttributeValueContaining("href", "/names/usage/english/");
            suffixes.add("/names/usage/english");
            for (Element urlElement : webPagesHrefElements)
                suffixes.add(urlElement.attr("href"));
            for (String suffixUrl : suffixes) {
                String fullUrl = prefixUrl + suffixUrl;
                Document pageDoc = Jsoup.connect(fullUrl).get();
                /* Get the element which holds the names */
                Elements nameElements = pageDoc.getElementsByClass("listName");
                for (Element nameElement : nameElements) {
                    String name = parseFullLine(nameElement);
                    addName(name);
                }
            }
            printDSToFiles();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    private void printDSToFiles() {
        try {
            FileWriter fw = new FileWriter("names.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            for (String name : names) {
                bw.write(name + '\n');
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addName(String name) {
        if (!names.contains(name)) { // Prevent duplication
            String[] splitedNames = name.split(" ");
            for (int i = 0; i < splitedNames.length; i++) {
                subString(splitedNames[i]);
                names.add(splitedNames[i]);
            }
        }
    }


    // Function to print all sub strings
    private void subString(String name) {
        char[] str = name.toCharArray();
        firstLetterFreq[Character.toLowerCase(str[0]) - 97] += 1;
        if (name.length() > 1) {
            freqPrevChar[str[1] - 97][Character.toLowerCase(str[0]) - 97] += 1;
        }
        for (int i = 2; i < str.length; i++) {
            /*
            If we recognize a name that consists of two words, we chose to define the letter before the space as the letter before the word after the space.
            e.g name: John Paul
            * */
            if (str[i] == 32) {
                str[i + 1] = Character.toLowerCase(str[i + 1]);
                freqPrevChar[str[i + 1] - 97][str[i - 1] - 97] += 1;
                i++;
                continue;
            }
            freqPrevChar[str[i] - 97][str[i - 1] - 97] += 1;
        }
        int n = name.length();
        // Pick starting point
        for (int len = 1; len <= n; len++) {
            // Pick ending point
            for (int i = 0; i <= n - len; i++) {
                String tmp = "";
                int j = i + len - 1;
                for (int k = i; k <= j; k++) {
                    tmp += str[k];
                }
//                System.out.println(tmp);
                addToLengthToSubstrings(tmp);
                addToSubstringsToFreq(tmp);
            }
        }
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

    /**
     * The function of this function is to take the full line of the element that holds the name we ultimately want to receive, and clear it so that we only get the name.
     *
     * @param nameElement - The element which holds the name we want to extract.
     * @return - The name we want to add to our data structures.
     */
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
        File path = new File("names.txt");
        String name;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((name = br.readLine()) != null) {
                s.addName(name);
            }
        } catch (IOException i) {
            i.printStackTrace();
        }

//        if (args != null && args[0] != null)
//            switch (args[0]) {
//                case "CountSpecificString":
//                    s.CountSpecificString((args[1]));
//                    break;
//                case "CountAllStrings":
//                    s.CountAllStrings(Integer.parseInt(args[1]));
//                    break;
//                case "CountMaxString":
//                    s.CountMaxString(Integer.parseInt(args[1]));
//                    break;
//                case "AllIncludesString":
//                    s.AllIncludesString(args[1]);
//                    break;
//                case "GenerateName":
//                    s.GenerateName();
//                    break;
//            }


        /* CountSpecificString CHECKED */
//        s.CountSpecificString("Yan"); // 1
//        s.CountSpecificString("yan"); // 19
//        s.CountSpecificString("ett"); // 74
//        s.CountSpecificString("Ett"); // 2
//        s.CountSpecificString("chr"); // 0
//        s.CountSpecificString("Ad"); // 29


        /* CountAllStrings CHECKED*/
//        s.CountAllStrings(7);
//        s.CountAllStrings(8);



        /* CountMaxString CHECKED */
//        s.CountMaxString(1); // e
//        s.CountMaxString(2); // an
//        s.CountMaxString(3); // ell
//        s.CountMaxString(4); // anna
//        s.CountMaxString(5); // chris
//        s.CountMaxString(6); // christ
//        s.CountMaxString(7); // christi
//        s.CountMaxString(8); // alexandr


        /* AllIncludesString CHECKED */
//        s.AllIncludesString("YadkbycharlieMichal");
//        s.AllIncludesString("Egttechrisett");

        /* GenerateName CHECKED */
//        s.GenerateName();
    }

}





