package org.example;

import java.util.*;

public class WordLadder127_2 {


    public static void main(String [] args) {

//        String beginWord = "cet";
//        String endWord = "ism";
//        List<String> dict = new ArrayList<>(Arrays.asList(
//                "kid","tag","pup","ail","tun","woo","erg","luz","brr","gay","sip","kay","per","val","mes","ohs","now","boa","cet","pal","bar","die","war","hay","eco","pub","lob","rue","fry","lit","rex","jan","cot","bid","ali","pay","col","gum","ger","row","won","dan","rum","fad","tut","sag","yip","sui","ark","has","zip","fez","own","ump","dis","ads","max","jaw","out","btu","ana","gap","cry","led","abe","box","ore","pig","fie","toy","fat","cal","lie","noh","sew","ono","tam","flu","mgm","ply","awe","pry","tit","tie","yet","too","tax","jim","san","pan","map","ski","ova","wed","non","wac","nut","why","bye","lye","oct","old","fin","feb","chi","sap","owl","log","tod","dot","bow","fob","for","joe","ivy","fan","age","fax","hip","jib","mel","hus","sob","ifs","tab","ara","dab","jag","jar","arm","lot","tom","sax","tex","yum","pei","wen","wry","ire","irk","far","mew","wit","doe","gas","rte","ian","pot","ask","wag","hag","amy","nag","ron","soy","gin","don","tug","fay","vic","boo","nam","ave","buy","sop","but","orb","fen","paw","his","sub","bob","yea","oft","inn","rod","yam","pew","web","hod","hun","gyp","wei","wis","rob","gad","pie","mon","dog","bib","rub","ere","dig","era","cat","fox","bee","mod","day","apr","vie","nev","jam","pam","new","aye","ani","and","ibm","yap","can","pyx","tar","kin","fog","hum","pip","cup","dye","lyx","jog","nun","par","wan","fey","bus","oak","bad","ats","set","qom","vat","eat","pus","rev","axe","ion","six","ila","lao","mom","mas","pro","few","opt","poe","art","ash","oar","cap","lop","may","shy","rid","bat","sum","rim","fee","bmw","sky","maj","hue","thy","ava","rap","den","fla","auk","cox","ibo","hey","saw","vim","sec","ltd","you","its","tat","dew","eva","tog","ram","let","see","zit","maw","nix","ate","gig","rep","owe","ind","hog","eve","sam","zoo","any","dow","cod","bed","vet","ham","sis","hex","via","fir","nod","mao","aug","mum","hoe","bah","hal","keg","hew","zed","tow","gog","ass","dem","who","bet","gos","son","ear","spy","kit","boy","due","sen","oaf","mix","hep","fur","ada","bin","nil","mia","ewe","hit","fix","sad","rib","eye","hop","haw","wax","mid","tad","ken","wad","rye","pap","bog","gut","ito","woe","our","ado","sin","mad","ray","hon","roy","dip","hen","iva","lug","asp","hui","yak","bay","poi","yep","bun","try","lad","elm","nat","wyo","gym","dug","toe","dee","wig","sly","rip","geo","cog","pas","zen","odd","nan","lay","pod","fit","hem","joy","bum","rio","yon","dec","leg","put","sue","dim","pet","yaw","nub","bit","bur","sid","sun","oil","red","doc","moe","caw","eel","dix","cub","end","gem","off","yew","hug","pop","tub","sgt","lid","pun","ton","sol","din","yup","jab","pea","bug","gag","mil","jig","hub","low","did","tin","get","gte","sox","lei","mig","fig","lon","use","ban","flo","nov","jut","bag","mir","sty","lap","two","ins","con","ant","net","tux","ode","stu","mug","cad","nap","gun","fop","tot","sow","sal","sic","ted","wot","del","imp","cob","way","ann","tan","mci","job","wet","ism","err","him","all","pad","hah","hie","aim"
//        ));

        //        String beginWord = "qa";
//        String endWord = "sq";
//        List<String> dict = Arrays.asList(
//            "si","go","se","cm","so","ph","mt","db","mb","sb","kr","ln","tm","le","av","sm","ar","ci","ca","br","ti","ba","to","ra","fa","yo","ow","sn","ya","cr","po","fe","ho","ma","re","or","rn","au","ur","rh","sr","tc","lt","lo","as","fr","nb","yb","if","pb","ge","th","pm","rb","sh","co","ga","li","ha","hz","no","bi","di","hi","qa","pi","os","uh","wm","an","me","mo","na","la","st","er","sc","ne","mn","mi","am","ex","pt","io","be","fm","ta","tb","ni","mr","pa","he","lr","sq","ye"
//        );

//        String beginWord = "a";
//        String endWord = "c";
//        List<String> dict = List.of("a", "b", "c");

        String beginWord = "qa";//""hit";
        String endWord = "sq";//""cog";
        List<String> dict = List.of(
                "si","go","se","cm","so","ph","mt","db","mb","sb","kr","ln","tm","le","av","sm","ar","ci","ca","br","ti","ba","to","ra","fa","yo","ow","sn","ya","cr","po","fe","ho","ma","re","or","rn","au","ur","rh","sr","tc","lt","lo","as","fr","nb","yb","if","pb","ge","th","pm","rb","sh","co","ga","li","ha","hz","no","bi","di","hi","qa","pi","os","uh","wm","an","me","mo","na","la","st","er","sc","ne","mn","mi","am","ex","pt","io","be","fm","ta","tb","ni","mr","pa","he","lr","sq","ye"
        );

//        String beginWord = "hit";
//        String endWord = "cog";
//        List<String> dict = List.of(
//                //"hot","dot","dog","lot","log"
//                "hot","dot","dog","lot","log","cog"
//        );


        int res = new WordLadder127_2().ladderLength(
                beginWord, endWord, new ArrayList<>(dict));

        System.out.println(res);
    }





    String endWord;
    String beginWord;
    char[] begin;
    char[] end;
    int beginIndex = -1;
    int endIndex = -1;
    int size;

    List<String> wordList;
    char[][] words;
    int[] used;


    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        this.wordList = wordList;
        this.endWord = endWord;
        this.beginWord = beginWord;
        begin = beginWord.toCharArray();
        end = endWord.toCharArray();


        // make dictionary
        makeDict();
        if (endIndex == -1)
            return 0;

        return check();

    }

    private int check() {

        int[] current = new int[size];
        int[] next = new int[size];
        int[] swap;
        current[0] = beginIndex;
        used[beginIndex] = 1;
        int currentSize = 1;
        int nextSize;

        int steps = 1;

        while (true) {
            steps++;
            nextSize = 0;


            for (int i1 = 0; i1 < currentSize; i1++) {

                char[] w1 = words[current[i1]];

                for (int i2 = 0; i2 < size; i2++) {

                    if (   used[i2] == 0
                           && pair(w1, words[i2])) {

                        if (i2 == endIndex) {
                            return steps;
                        }

                        next[nextSize++] = i2;
                        used[i2] = 1;

                    }
                }
            }
            if (nextSize == 0) {
                return 0;
            }

            swap = current;
            current = next;
            currentSize = nextSize;
            next = swap;
        }

    }



    private void makeDict() {
        //dict = new int[wordList.size()+1][wordList.size()+1];

        words = new char[wordList.size()+1][];
        int i = 0;
        for (String s : wordList) {

            words[i++] = s.toCharArray();
        }

        size = wordList.size();

        for (int i1 = wordList.size()-1; i1 >= 0; i1--) {

            //addToMap(i1);
            char[] w1 = words[i1];

            if (Arrays.compare(w1, begin) == 0) {
                beginIndex = i1;
                if (endIndex != -1) {
                    break;
                }

            }
            if (Arrays.compare(w1, end) == 0) {
                endIndex = i1;

                if (beginIndex != -1) {
                    break;
                }
            }

        }
        if (beginIndex == -1) {
            beginIndex = size;
            words[size] = begin;
            size++;
        }
        used = new int[size];
    }

    protected boolean pair(char[] w1, char[] w2) {
        int diff = 0;
        for (int i=0; i< w1.length; i++) {
            if (w1[i] != w2[i]) {
                diff++;
                if (diff >= 2) {
                    return false;
                }
            }
        }
        return true;
    }

}
