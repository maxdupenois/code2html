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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mpd209
 */
public abstract class ParserOld {

    protected File inFile;
    protected File outFile;
    private boolean singleLine;

//    protected class Keyword{
//        public int index;
//        public int
//    }
//
//    private String[] literals;
//    private String[] other;

    public ParserOld(String inFileStr, String outFileStr) throws IOException {
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
            reader.close();
            String[] outLines = convert(input).split("\n");


            writer.write(""); //Clear file
            for(String outLine : outLines){
                writer.append(outLine+(singleLine?"":"\n"));
//                System.out.println(outLine);
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

    protected final boolean inString(int index, String contents){
        return (inDoubleQuotes(index, contents)||inSingleQuotes(index, contents));
    }
    protected final boolean inDoubleQuotes(int index, String contents){
        String sub = contents.substring(0, index);
        //Negativ zerowidth backlook to ignore escaping
        Pattern doubleQuote = Pattern.compile("(?<!\\\\)\"");
        Matcher m = doubleQuote.matcher(sub);
        int doubleQuoteCount = 0;
        while (m.find()) {doubleQuoteCount++;}
        return (doubleQuoteCount%2!=0);
    }
    protected final boolean inSingleQuotes(int index, String contents){
        String sub = contents.substring(0, index);
        Pattern singleQuote = Pattern.compile("'");
        Matcher m = singleQuote.matcher(sub);
        int singleQuoteCount = 0;
        while (m.find()) {singleQuoteCount++;}
        return (singleQuoteCount%2!=0);
    }
    protected final boolean inHtmlTags(int index, String contents){
        String sub = contents.substring(0, index);
        Pattern openTag = Pattern.compile("\\<");
        Pattern closeTag = Pattern.compile("\\>");
        Matcher m = openTag.matcher(sub);
        int openTagCount = 0;
        while (m.find()) {openTagCount++;}

        m = closeTag.matcher(sub);
        int closeTagCount = 0;
        while (m.find()) {closeTagCount++;}
//        System.out.println(index+" open: "+openTagCount+" closed: "+closeTagCount);
        return (openTagCount>closeTagCount);
    }
    protected final String keyword(String keyword, String prepend, String append, String contents, boolean htmlTagCheck) {
        String regex = "(\\W|\\b)"+keyword+"(\\W)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(contents);
        StringBuffer sb = new StringBuffer();
        String match1;
        String match2;
        while (m.find()) {
            match1 = m.group(1).replaceAll("\\$", "\\\\\\$");
            match2 = m.group(2).replaceAll("\\$", "\\\\\\$");
            if(!inString(m.start(), contents)&&
                    (!htmlTagCheck||
                    (htmlTagCheck&&!inHtmlTags(m.start(), contents)))){
                m.appendReplacement(sb, match1 + prepend + keyword + append+match2);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
    protected final String keywordSpan(String keyword, String classname, String contents) {
        return this.keyword(keyword, "<span class=\""+classname+"\">", "</span>", contents, false);
    }

    protected final String keywordSpan(String keyword, String classname, String contents, boolean htmlTagCheck) {
        return this.keyword(keyword, "<span class=\""+classname+"\">", "</span>", contents, htmlTagCheck);
    }

    protected final String bigComment(String commentStart, String commentFinish, String prepend, String append, String contents){
        String regex = commentStart+"(.*?)"+commentFinish;
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);

        Matcher m = p.matcher(contents);
        StringBuffer sb = new StringBuffer();
        String match;
        while (m.find()) {
            match = m.group().replaceAll("\\$", "\\\\\\$");
//            System.out.println("Big Comment Match: "+match);
            if(!inString(m.start(), contents)){
                m.appendReplacement(sb, prepend + match + append);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
    protected final String bigCommentSpan(String commentStart, String commentFinish, String classname, String contents) {
        return this.bigComment(commentStart, commentFinish, "<span class=\""+classname+"\">", "</span>", contents);
    }
    protected final String lineComment(String comment, String prepend, String append, String contents){
        String regex = "("+comment+".*"+")\\n";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(contents);
        StringBuffer sb = new StringBuffer();
        String match;
        while (m.find()) {
            match = m.group(1).replaceAll("\\$", "\\\\\\$");
            if(!inString(m.start(), contents)){
                m.appendReplacement(sb, prepend + match + append + "\n");
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
    protected final String lineCommentSpan(String commentStart, String classname, String contents) {
        return this.lineComment(commentStart, "<span class=\""+classname+"\">", "</span>", contents);
    }
    
    protected final String doubleQuotes(String prepend, String append, String contents){
        String regex = "\\\"[^\\\"]*?\\\"";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(contents);
        StringBuffer sb = new StringBuffer();
        String match;
        while (m.find()) {
            match = m.group().replaceAll("\\$", "\\\\\\$");
//            System.out.println("Double Quotes match "+match);
            if(!inSingleQuotes(m.start(), contents)){
                m.appendReplacement(sb, prepend + match + append);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
    protected final String singleQuotes(String prepend, String append, String contents){
        String regex = "\\'[^\\']*?\\'";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(contents);
        StringBuffer sb = new StringBuffer();
        String match;
        while (m.find()) {
            match = m.group().replaceAll("\\$", "\\\\\\$");
            if(!inDoubleQuotes(m.start(), contents)){
                m.appendReplacement(sb, prepend + match + append);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
   protected final String doubleQuotesSpan(String classname, String contents) {
        return this.doubleQuotes("<span class=\""+classname+"\">", "</span>", contents);
    }
    protected final String singleQuotesSpan(String classname, String contents) {
        return this.singleQuotes("<span class=\""+classname+"\">", "</span>", contents);
    }
    protected final String function(String prepend, String append, String contents){
        String regex = "(\\w\\w*)\\(";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(contents);
        StringBuffer sb = new StringBuffer();
        String match;
        while (m.find()) {
            match = m.group(1).replaceAll("\\$", "\\\\\\$");
//            System.out.println("Function match "+match);
            if(!inString(m.start(), contents)){
                m.appendReplacement(sb, prepend + match + append + "(");
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
    protected final String functionSpan(String classname, String contents) {
        return this.function("<span class=\""+classname+"\">", "</span>", contents);
    }
    protected abstract String convert(String contents);
}
