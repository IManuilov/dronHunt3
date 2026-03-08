package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WordLadder127_3 {


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

//        String beginWord = "qa";//""hit";
//        String endWord = "sq";//""cog";
//        List<String> dict = List.of(
//                "si","go","se","cm","so","ph","mt","db","mb","sb","kr","ln","tm","le","av","sm","ar","ci","ca","br","ti","ba","to","ra","fa","yo","ow","sn","ya","cr","po","fe","ho","ma","re","or","rn","au","ur","rh","sr","tc","lt","lo","as","fr","nb","yb","if","pb","ge","th","pm","rb","sh","co","ga","li","ha","hz","no","bi","di","hi","qa","pi","os","uh","wm","an","me","mo","na","la","st","er","sc","ne","mn","mi","am","ex","pt","io","be","fm","ta","tb","ni","mr","pa","he","lr","sq","ye"
//        );

        String beginWord = "hit";
        String endWord = "cog";
        List<String> dict = List.of(
                //"hot","dot","dog","lot","log"
                "hot","dot","dog","lot","log","cog"
        );


        int res = new WordLadder127_3().ladderLength(
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
    int wordSize;

    List<String> wordList;
    char[][] words;


    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        this.wordList = wordList;
        this.endWord = endWord;
        this.beginWord = beginWord;
        begin = beginWord.toCharArray();
        end = endWord.toCharArray();
        wordSize = begin.length;

        // make dictionary
        makeDict();
        if (endIndex == -1)
            return 0;

        return check();

    }

    private int check() {

        char[] sw4 = words[size-1];
        words[size-1] = words[beginIndex];
        words[beginIndex] = sw4;
        if (endIndex == size-1) {
            endIndex = beginIndex;
        }
        beginIndex = size-1;

        sw4 = words[0];
        words[0] = words[endIndex];
        words[endIndex] = sw4;
        endIndex = 0;

        int steps = 1;
        int wordsSize = size-1;
        int curStart = size-1;
        int curFinish = size;

        while (true) {
            steps++;


            for (int i1 = curStart; i1 < curFinish; i1++) {

                char[] w1 = words[i1];

                all2:
                for (int i2 = 0; i2 < wordsSize; ) {

                    char[] w2 = words[i2];

                    int diff = 0;
                    for (int i = 0; i < wordSize; i++) {
                        if (w1[i] != w2[i]) {
                            diff++;
                            if (diff >= 2) {
                                i2++;
                                continue all2;
                            }
                        }
                    }

                    if (i2 == 0) {//endIndex) {
                        return steps;
                    }

                    wordsSize--;

                    char[] swp = words[wordsSize];
                    words[wordsSize] = words[i2];
                    words[i2] = swp;


                }
            }
            if (curStart == wordsSize) {
                return 0;
            }

            curFinish = curStart;
            curStart = wordsSize;
        }

    }

    private void makeDict() {

        words = new char[wordList.size()+1][];

        for (int i=0; i< wordList.size(); i++) {

            char[] w1 = words[i] = wordList.get(i).toCharArray();

            if (beginIndex == -1 && Arrays.compare(w1, begin) == 0) {
                beginIndex = i;
            }

            if (endIndex == -1 && Arrays.compare(w1, end) == 0) {
                endIndex = i;
            }
        }

        size = wordList.size();

        if (beginIndex == -1) {
            beginIndex = size;
            words[size] = begin;
            size++;
        }

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
