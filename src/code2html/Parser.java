/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package code2html;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mpd209
 */
public abstract class Parser {

    protected File inFile;
    protected File outFile;
    protected char[] original;

    private TreeMap<Integer, Modification> replacements;
    private TreeMap<Integer, ArrayList<Modification>> inserts;
    TreeMap<Integer, Integer> doubleQuotes;
    TreeMap<Integer, Integer> singleQuotes;
    private boolean singleLine;

    private static class Modification{
        /**
         * @return the index
         */
        public int getIndex() {return index;}
        /**
         * @return the type
         */
        public Type getType() {return type;}

        /**
         * @return the value
         */
        public String getValue() {return value;}
        public enum Type{
            INSERT,
            REPLACE;
        }
        private int index;
        private Type type;
        private String value;
        public Modification(int index, String value, Type type){
            this.index = index;
            this.value = value;
            this.type = type;
        }
    }

    protected void addInsert(int index, String value){
        if(inserts.get(index)==null){
            inserts.put(index, new ArrayList<Modification>());
        }
        inserts.get(index).add(new Modification(index, value, Modification.Type.INSERT));
    }
    protected void addReplacement(int index, String value){
        replacements.put(index, new Modification(index, value, Modification.Type.REPLACE));
    }

//    protected class Keyword{
//        public int index;
//        public int
//    }
//
//    private String[] literals;
//    private String[] other;

    public Parser(String inFileStr, String outFileStr) throws IOException {
        inFile = new File(inFileStr);
        outFile = new File(outFileStr);
        if (!inFile.isFile()) {
            throw new IOException("File '" + inFile + "' not found");
        }
        if (!outFile.isFile()) {
            if (!outFile.createNewFile()) {
                throw new IOException("Could not create '" + outFile + "'");
            }
        }
    }
    public final void setSingleLine(boolean singleLine){
        this.singleLine = singleLine;
    }
    public final void parse() throws IOException {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(inFile));
            writer = new BufferedWriter(new FileWriter(outFile));

            String input = new String();
            String line = null;
            while ((line = reader.readLine()) != null) {
                input += line+"\n";
            }
//            input = "a 1 \"'a\" 2 a 3 4 'a'";
//            System.out.println(input);
            this.original = input.toCharArray();
            fillDoubleQuotes();
            fillSingleQuotes();
            replacements = new TreeMap<Integer, Modification>();
            inserts = new TreeMap<Integer, ArrayList<Modification>>();

            reader.close();
            convert();

            String modified = makeModifications();

//            System.out.println(modified);
            
            String[] outLines = modified.split("\n");

            writer.write(""); //Clear file
            for(String outLine : outLines){
                writer.append(outLine+(singleLine?"":"\n"));
            }
            writer.close();
        }finally{
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException iOException) {
                //Not much we can do here
                //And it's probably already closed
            }
        }
    }

    private final String makeModifications(){
        StringBuffer modified = new StringBuffer();
        Modification replacement;
        ArrayList<Modification> insertsList;
        Iterator<Modification> insertsIter;
        Modification insert;
        for(int i=0; i<this.original.length; i++){
            replacement = replacements.get(i);
            insertsList = inserts.get(i);

            if(insertsList!=null){
                insertsIter = insertsList.iterator();
                while(insertsIter.hasNext()){
                    insert = insertsIter.next();
                    modified.append(insert.getValue());
                }
            }

            if(replacement!=null){
                modified.append(replacement.getValue());
            }else{
                modified.append(this.original[i]);
            }

        }
        //Add any final insert
        if((insertsList = inserts.get(this.original.length))!=null){
            insertsIter = insertsList.iterator();
            while(insertsIter.hasNext()){
                insert = insertsIter.next();
                modified.append(insert.getValue());
            }
        }
        return modified.toString();
    }

    protected final boolean inString(int index){
        return (inDoubleQuotes(index)||inSingleQuotes(index));
    }


    private void fillDoubleQuotes(){
        String input = new String(original).substring(0);
        Pattern doubleQuote = Pattern.compile("(?<!\\\\)\".*?(?<!\\\\)\"", Pattern.DOTALL);
        Matcher m = doubleQuote.matcher(input);
        doubleQuotes = new TreeMap<Integer, Integer>();
        while (m.find()) {
            doubleQuotes.put(m.start(), m.end());
        }
    }
    private void fillSingleQuotes(){
        String input = new String(original).substring(0);
        Pattern doubleQuote = Pattern.compile("(?<!\\\\)'", Pattern.DOTALL);
        Matcher m = doubleQuote.matcher(input);
        singleQuotes = new TreeMap<Integer, Integer>();
        int start = -1;
        while (m.find()) {
            if(!inDoubleQuotes(m.start())){
                if(start<0){
                    start = m.start();
                }else{
                    singleQuotes.put(start, m.start()+1);
                    start = -1;
                }
            }
        }
    }

    protected final boolean inDoubleQuotes(int index){
        Integer end = doubleQuotes.get(index);
        Integer start = null;
        if(end==null){
            start = doubleQuotes.lowerKey(index);
            if(start==null)return false;
            end = doubleQuotes.get(start);
            return (start.intValue()<index&&index<end.intValue());
        }else{
            //Falls on a starting quote
            return false;
        }
    }
    protected final boolean inSingleQuotes(int index){
        Integer end = singleQuotes.get(index);
        Integer start = null;
        if(end==null){
            start = singleQuotes.lowerKey(index);
            if(start==null)return false;
            end = singleQuotes.get(start);
            return (start.intValue()<index&&index<end.intValue());
        }else{
            //Falls on a starting quote
            return false;
        }

    }
    protected final void doubleQuotesSpan(String classname){
        this.doubleQuotes("<span class=\""+classname+"\">", "</span>");
    }
    protected final void doubleQuotes(String prepend, String append) {
        Iterator<Integer> iter = doubleQuotes.keySet().iterator();
        Integer start;
        Integer end;
        while(iter.hasNext()){
            start = iter.next();
            end = doubleQuotes.get(start);
            addInsert(start, prepend);
            addInsert(end, append);
        }
    }

    protected final void environmentSpan(String environmentStart, String environmentEnd,
            String classname, boolean ignoreQuotes){
        this.environment(environmentStart, environmentEnd, "<span class=\""+classname+"\">", "</span>", ignoreQuotes);
    }

    protected final void environmentSpan(String environmentStart, String environmentEnd,
            String classname){
        this.environment(environmentStart, environmentEnd, "<span class=\""+classname+"\">", "</span>", true);
    }

    protected final void environment(String environmentStart, String environmentEnd,
            String prepend, String append){
        this.environment(environmentStart, environmentEnd, prepend, append, true);
    }
    protected final void environment(String environmentStart, String environmentEnd, 
            String prepend, String append, boolean ignoreQuotes){
        String regex = environmentStart+".*?"+environmentEnd;
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(new String(original));
        while (m.find()) {
            if(!ignoreQuotes&&inString(m.start()))continue;
            addInsert(m.start(), prepend);
            addInsert(m.end(), append);
        }
    }

    protected final void singleQuotesSpan(String classname){
        this.singleQuotes("<span class=\""+classname+"\">", "</span>");
    }
    protected final void singleQuotes(String prepend, String append) {
        Iterator<Integer> iter = singleQuotes.keySet().iterator();
        Integer start;
        Integer end;
        while(iter.hasNext()){
            start = iter.next();
            end = singleQuotes.get(start);
            addInsert(start, prepend);
            addInsert(end, append);
        }
    }
    protected final void keyword(String keyword, String prepend, String append){
        this.keyword(keyword, prepend, append, false);
    }
    protected final void keyword(String keyword, String prepend, String append, boolean ignoreQuotes) {
        String regex = "(?<=\\b)"+keyword+"(?=\\b)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(new String(original));
        while (m.find()) {
            if(!ignoreQuotes&&inString(m.start()))continue;
            addInsert(m.start(), prepend);
            addInsert(m.end(), append);
        }
    }

    protected final void keywordSpan(String keyword, String classname, boolean ignoreQuotes) {
        this.keyword(keyword, "<span class=\""+classname+"\">", "</span>", ignoreQuotes);
    }
    protected final void keywordSpan(String keyword, String classname) {
        this.keyword(keyword, "<span class=\""+classname+"\">", "</span>", false);
    }
    protected final void replace(String search, String replacement) {
        replace(search, replacement, true);
    }
    protected final void replace(String search, String replacement, boolean ignoreQuotes) {
        String regex = search;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(new String(original));
        while (m.find()) {
            if(!ignoreQuotes&&inString(m.start()))continue;
            createReplacement(m.start(), m.end(), replacement);
        }
    }

    private void createReplacement(int start, int end, String replacement){
        char[] replacementArr = replacement.toCharArray();
        String replacementAtIndex = "";
        int replacementCounter=0;
        for(int i = start; i < end; i++, replacementCounter++){
            if(replacementCounter>=replacementArr.length){
                replacementAtIndex = "";
            }else{
                replacementAtIndex = new Character(replacementArr[replacementCounter]).toString();
            }
            addReplacement(i, replacementAtIndex);
        }
        if(replacementCounter<replacementArr.length){
            StringBuffer insert = new StringBuffer();
            for(int i = replacementCounter; i < replacementArr.length; i++){
                insert.append(replacementArr[i]);
            }
            addInsert(end, insert.toString());
        }
    }

    

    protected abstract void convert();
}
