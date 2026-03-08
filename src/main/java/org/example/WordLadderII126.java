package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WordLadderII126 {

    public static void main(String [] args) {

        String beginWord = "cet";
        String endWord = "ism";
        List<String> dict = Arrays.asList(
            "kid","tag","pup","ail","tun","woo","erg","luz","brr","gay","sip","kay","per","val","mes","ohs","now","boa","cet","pal","bar","die","war","hay","eco","pub","lob","rue","fry","lit","rex","jan","cot","bid","ali","pay","col","gum","ger","row","won","dan","rum","fad","tut","sag","yip","sui","ark","has","zip","fez","own","ump","dis","ads","max","jaw","out","btu","ana","gap","cry","led","abe","box","ore","pig","fie","toy","fat","cal","lie","noh","sew","ono","tam","flu","mgm","ply","awe","pry","tit","tie","yet","too","tax","jim","san","pan","map","ski","ova","wed","non","wac","nut","why","bye","lye","oct","old","fin","feb","chi","sap","owl","log","tod","dot","bow","fob","for","joe","ivy","fan","age","fax","hip","jib","mel","hus","sob","ifs","tab","ara","dab","jag","jar","arm","lot","tom","sax","tex","yum","pei","wen","wry","ire","irk","far","mew","wit","doe","gas","rte","ian","pot","ask","wag","hag","amy","nag","ron","soy","gin","don","tug","fay","vic","boo","nam","ave","buy","sop","but","orb","fen","paw","his","sub","bob","yea","oft","inn","rod","yam","pew","web","hod","hun","gyp","wei","wis","rob","gad","pie","mon","dog","bib","rub","ere","dig","era","cat","fox","bee","mod","day","apr","vie","nev","jam","pam","new","aye","ani","and","ibm","yap","can","pyx","tar","kin","fog","hum","pip","cup","dye","lyx","jog","nun","par","wan","fey","bus","oak","bad","ats","set","qom","vat","eat","pus","rev","axe","ion","six","ila","lao","mom","mas","pro","few","opt","poe","art","ash","oar","cap","lop","may","shy","rid","bat","sum","rim","fee","bmw","sky","maj","hue","thy","ava","rap","den","fla","auk","cox","ibo","hey","saw","vim","sec","ltd","you","its","tat","dew","eva","tog","ram","let","see","zit","maw","nix","ate","gig","rep","owe","ind","hog","eve","sam","zoo","any","dow","cod","bed","vet","ham","sis","hex","via","fir","nod","mao","aug","mum","hoe","bah","hal","keg","hew","zed","tow","gog","ass","dem","who","bet","gos","son","ear","spy","kit","boy","due","sen","oaf","mix","hep","fur","ada","bin","nil","mia","ewe","hit","fix","sad","rib","eye","hop","haw","wax","mid","tad","ken","wad","rye","pap","bog","gut","ito","woe","our","ado","sin","mad","ray","hon","roy","dip","hen","iva","lug","asp","hui","yak","bay","poi","yep","bun","try","lad","elm","nat","wyo","gym","dug","toe","dee","wig","sly","rip","geo","cog","pas","zen","odd","nan","lay","pod","fit","hem","joy","bum","rio","yon","dec","leg","put","sue","dim","pet","yaw","nub","bit","bur","sid","sun","oil","red","doc","moe","caw","eel","dix","cub","end","gem","off","yew","hug","pop","tub","sgt","lid","pun","ton","sol","din","yup","jab","pea","bug","gag","mil","jig","hub","low","did","tin","get","gte","sox","lei","mig","fig","lon","use","ban","flo","nov","jut","bag","mir","sty","lap","two","ins","con","ant","net","tux","ode","stu","mug","cad","nap","gun","fop","tot","sow","sal","sic","ted","wot","del","imp","cob","way","ann","tan","mci","job","wet","ism","err","him","all","pad","hah","hie","aim"
        );

        //        String beginWord = "qa";
//        String endWord = "sq";
//        List<String> dict = Arrays.asList(
//            "si","go","se","cm","so","ph","mt","db","mb","sb","kr","ln","tm","le","av","sm","ar","ci","ca","br","ti","ba","to","ra","fa","yo","ow","sn","ya","cr","po","fe","ho","ma","re","or","rn","au","ur","rh","sr","tc","lt","lo","as","fr","nb","yb","if","pb","ge","th","pm","rb","sh","co","ga","li","ha","hz","no","bi","di","hi","qa","pi","os","uh","wm","an","me","mo","na","la","st","er","sc","ne","mn","mi","am","ex","pt","io","be","fm","ta","tb","ni","mr","pa","he","lr","sq","ye"
//        );

//        String beginWord = "a";
//        String endWord = "c";
//        List<String> dict = List.of("a", "b", "c");

//        String beginWord = "hit";
//        String endWord = "cog";
//        List<String> dict = List.of("hot","dot","dog","lot","log");


        List<List<String>> res = new WordLadderII126().findLadders(
            beginWord, endWord, dict);

        System.out.println(res);
    }


    Map<String, List<String>> map = new HashMap<>();
    List<String> path = new LinkedList<>();
    String endWord;
    String beginWord;
    int steplimit = 0;

    List<List<String>> results = new ArrayList<>();

    public List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
        this.endWord = endWord;
        this.beginWord = beginWord;
        // make dictionary


        for (int i1 = 0; i1 < wordList.size()-1; i1++) {

            String w1 = wordList.get(i1);
            addToMap(wordList, i1, w1);
        }
        if (!map.containsKey(beginWord)) {
            addToMap(wordList, -1, beginWord);
        }
        System.out.println("dictionary: " + map);

        //steplimit = check();

        int step = 0;
        Set<String> except = new HashSet<>();
        List<Set<String>> current = new ArrayList<>();
        current.add(Set.of(beginWord));

        Set<String> next = null;
        while ((next = getall(current.get(current.size()-1), except)) != null) {
            current.add(next);
            step++;

            System.out.println(" " + step + " " + current.size() + " " + except.size() + "   " + except);
            if (next.contains(endWord)) {
                System.out.println("found: " + step);
                //return step;
                break;
            }
        }
        if (next == null) {
            return List.of();
        }
        //current.getLast().clear();
        //current.getLast().add(endWord);
        Set<String> prev = Set.of(endWord);
        for (int i= current.size()-1; i>=0; i++) {
            prev = getall(prev, except);

        }







//        if (steplimit == 0) {
//            return List.of();
//        }

        //
//        path.addLast(beginWord);
//
//        recursive();

        return results;
    }

    Set<String> getall(Set<String> keys, Set<String> except) {

        Set<String> next = keys.stream()
            .map(map::get)
            .flatMap(List::stream)
            .collect(Collectors.toSet());

        if (next.isEmpty()) {
            return null;
        }

        //System.out.println("next1: " + next);
        next.removeAll(except);
        //System.out.println("next2: " + next);
        except.addAll(next);

        return next;
    }

    private int check() {

        int step = 0;
        Set<String> except = new HashSet<>();
        List<Set<String>> current = new ArrayList<>();
        current.add(Set.of(beginWord));

        Set<String> next;
        while ((next = getall(current.get(current.size()-1), except)) != null) {
            current.add(next);
            step++;

            System.out.println(" " + step + " " + current.size() + " " + except.size() + "   " + except);
            if (current.contains(endWord)) {
                System.out.println("step: " + step);
                return step;
            }
        }




        return 0;
    }

    private void addToMap(List<String> wordList, int i1, String w1) {
        for (int i2 = i1 + 1; i2 < wordList.size(); i2++) {
            String w2 = wordList.get(i2);

            if (pair(w1, w2)) {

                map.computeIfAbsent(w1, (k) -> new ArrayList<>()).add(w2);
                map.computeIfAbsent(w2, (k) -> new ArrayList<>()).add(w1);
            }
        }
    }

    boolean recursive() {
        //System.out.println(path);
        if (path.size() >= steplimit + 1) {
            return false;
        }

        String current = path.getLast();
        //System.out.println(path.size() + " rec: " + current);

        List<String> lst = map.get(current);
        if (lst == null) {
            return false;
        }

        for (String next: lst) {
            if (path.contains(next)) {
                continue;
            }

            path.addLast(next);

            //System.out.println("next "+next);
            if (next.equals(endWord)) {

                found();

            } else {

                recursive();
            }


            path.removeLast();
        }
        return true;
    }

    protected void found() {

        if (!results.isEmpty()) {
            if (results.get(0).size() > path.size()) {
                //System.out.println("clear");
                results.clear();
            } else
            if (results.get(0).size() < path.size()) {
                return;
            }
        }
        //System.out.println(path);
        results.add(new ArrayList<>(path));

    }

    protected boolean pair(String w1, String w2) {
        int diff = 0;
        for (int i=0; i< w1.length(); i++) {
            if (w1.charAt(i) != w2.charAt(i)) {
                diff++;
                if (diff >= 2) {
                    return false;
                }
            }
        }
        return true;
    }
}
