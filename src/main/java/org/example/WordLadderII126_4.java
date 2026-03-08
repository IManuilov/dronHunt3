package org.example;

import java.util.*;

public class WordLadderII126_4 {

    public static void main(String [] args) {

//        String beginWord = "cet";
//        String endWord = "ism";
//        List<String> dict = Arrays.asList(
//            "kid","tag","pup","ail","tun","woo","erg","luz","brr","gay","sip","kay","per","val","mes","ohs","now","boa","cet","pal","bar","die","war","hay","eco","pub","lob","rue","fry","lit","rex","jan","cot","bid","ali","pay","col","gum","ger","row","won","dan","rum","fad","tut","sag","yip","sui","ark","has","zip","fez","own","ump","dis","ads","max","jaw","out","btu","ana","gap","cry","led","abe","box","ore","pig","fie","toy","fat","cal","lie","noh","sew","ono","tam","flu","mgm","ply","awe","pry","tit","tie","yet","too","tax","jim","san","pan","map","ski","ova","wed","non","wac","nut","why","bye","lye","oct","old","fin","feb","chi","sap","owl","log","tod","dot","bow","fob","for","joe","ivy","fan","age","fax","hip","jib","mel","hus","sob","ifs","tab","ara","dab","jag","jar","arm","lot","tom","sax","tex","yum","pei","wen","wry","ire","irk","far","mew","wit","doe","gas","rte","ian","pot","ask","wag","hag","amy","nag","ron","soy","gin","don","tug","fay","vic","boo","nam","ave","buy","sop","but","orb","fen","paw","his","sub","bob","yea","oft","inn","rod","yam","pew","web","hod","hun","gyp","wei","wis","rob","gad","pie","mon","dog","bib","rub","ere","dig","era","cat","fox","bee","mod","day","apr","vie","nev","jam","pam","new","aye","ani","and","ibm","yap","can","pyx","tar","kin","fog","hum","pip","cup","dye","lyx","jog","nun","par","wan","fey","bus","oak","bad","ats","set","qom","vat","eat","pus","rev","axe","ion","six","ila","lao","mom","mas","pro","few","opt","poe","art","ash","oar","cap","lop","may","shy","rid","bat","sum","rim","fee","bmw","sky","maj","hue","thy","ava","rap","den","fla","auk","cox","ibo","hey","saw","vim","sec","ltd","you","its","tat","dew","eva","tog","ram","let","see","zit","maw","nix","ate","gig","rep","owe","ind","hog","eve","sam","zoo","any","dow","cod","bed","vet","ham","sis","hex","via","fir","nod","mao","aug","mum","hoe","bah","hal","keg","hew","zed","tow","gog","ass","dem","who","bet","gos","son","ear","spy","kit","boy","due","sen","oaf","mix","hep","fur","ada","bin","nil","mia","ewe","hit","fix","sad","rib","eye","hop","haw","wax","mid","tad","ken","wad","rye","pap","bog","gut","ito","woe","our","ado","sin","mad","ray","hon","roy","dip","hen","iva","lug","asp","hui","yak","bay","poi","yep","bun","try","lad","elm","nat","wyo","gym","dug","toe","dee","wig","sly","rip","geo","cog","pas","zen","odd","nan","lay","pod","fit","hem","joy","bum","rio","yon","dec","leg","put","sue","dim","pet","yaw","nub","bit","bur","sid","sun","oil","red","doc","moe","caw","eel","dix","cub","end","gem","off","yew","hug","pop","tub","sgt","lid","pun","ton","sol","din","yup","jab","pea","bug","gag","mil","jig","hub","low","did","tin","get","gte","sox","lei","mig","fig","lon","use","ban","flo","nov","jut","bag","mir","sty","lap","two","ins","con","ant","net","tux","ode","stu","mug","cad","nap","gun","fop","tot","sow","sal","sic","ted","wot","del","imp","cob","way","ann","tan","mci","job","wet","ism","err","him","all","pad","hah","hie","aim"
//        );

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
//        List<String> dict = new ArrayList<>(List.of(
//                "hot","dot","dog","lot","log","cog"
//                //"hot","dot","dog","lot","log"
//        ));


        String beginWord = "qa";
        String endWord = "sq";
        List<String> dict = new ArrayList<>(List.of(
                "si","go","se","cm","so","ph","mt","db","mb","sb","kr","ln","tm","le","av","sm","ar","ci","ca","br","ti","ba","to","ra","fa","yo","ow","sn","ya","cr","po","fe","ho","ma","re","or","rn","au","ur","rh","sr","tc","lt","lo","as","fr","nb","yb","if","pb","ge","th","pm","rb","sh","co","ga","li","ha","hz","no","bi","di","hi","qa","pi","os","uh","wm","an","me","mo","na","la","st","er","sc","ne","mn","mi","am","ex","pt","io","be","fm","ta","tb","ni","mr","pa","he","lr","sq","ye"
        ));



        List<List<String>> res = new WordLadderII126_4().findLadders(
            beginWord, endWord, dict);

        System.out.println(res);
    }




    int[][] dict;
    int[][] prev;
    int[] used;
    int[] used2;

    String endWord;
    String beginWord;
    int beginIndex = -1;
    int endIndex = -1;
    int size;

    List<String> wordList;
    List<List<String>> results = new ArrayList<>();


    public List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
        this.wordList = wordList;
        this.endWord = endWord;
        this.beginWord = beginWord;

        // make dictionary
        makeDict(beginWord, wordList);


        int[] curLayer = new int[size];
        curLayer[beginIndex] = 1;

        boolean found = false;
        int steps = 0;
        while (!found) {
            Arrays.fill(used2, 0);
            steps++;

            int[] nextLayer = new int[size];
            int nextLayerSize = 0;

            for (int cur = 0;  cur < size; cur++) {
                if (curLayer[cur] == 0) {
                    continue;
                }

                for (int i=0; i<size; i++) {

                    if (dict[cur][i] == 0 || used[i] == 1) {
                        continue;
                    }

                    used2[i] = 1;

                    if (i == endIndex) {
                        found = true;
                    }

                    nextLayer[i] = 1;
                    nextLayerSize = 1;
                    prev[i][cur] = 1;

                }

            }
            if (nextLayerSize == 0) {
                return List.of();
            }
            curLayer = nextLayer;
            for(int u=0; u<size; u++) {
                if(used2[u] == 1)
                    used[u] = 1;
            }

        }


        //
        List<String> path = new ArrayList<>();
        path.add(endWord);
        rec(endIndex, path, steps-1);


        return results;
    }

    private void rec(int itemIndex, List<String> path, int deep) {

        for (int n=0; n < size; n++) {

            if (prev[itemIndex][n] == 0) {
                continue;
            }

            path.add(wordList.get(n));

            if (deep == 0) {
                results.add(new ArrayList<>(path).reversed());
            } else {
                rec(n, path, deep-1);
            }

            path.removeLast();
        }


    }

    private void makeDict(String beginWord, List<String> wordList) {
        dict = new int[wordList.size()+1][wordList.size()+1];

        prev = new int[wordList.size()+1][wordList.size()+1];


        size = wordList.size();

        for (int i1 = wordList.size()-1; i1 >= 0; i1--) {

            addToMap(wordList, i1, wordList.get(i1));

        }
        if (beginIndex == -1) {
            addToMap(wordList, wordList.size(), beginWord);
            wordList.add(beginWord);
            size++;
        }

        used = new int[size];
        used2 = new int[size];

    }



    private void addToMap(List<String> wordList, int i1, String w1) {


        if (w1.equals(beginWord)) {
            beginIndex = i1;
        }
        if (w1.equals(endWord)) {
            endIndex = i1;
        }

        for (int i2 = 0; i2 < i1; i2++) {
            String w2 = wordList.get(i2);

            if (pair(w1, w2)) {

                dict[i1][i2] = 1;
                dict[i2][i1] = 1;
                //map.computeIfAbsent(w1, (k) -> new ArrayList<>()).add(w2);
                //map.computeIfAbsent(w2, (k) -> new ArrayList<>()).add(w1);
            }
        }
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
