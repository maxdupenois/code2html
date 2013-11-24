/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package code2html.parsers;

import code2html.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mpd209
 */
public class PhPParser extends Parser {

    public PhPParser(String inFile, String outFile) throws IOException {
        super(inFile, outFile);
    }

    @Override
    protected void convert() {
        replace("\\&", "&amp;");
        replace("\\<", "&lt;");
        replace("\\>", "&gt;");
        replace("\\n|\\r", "<br/>\n");
        replace("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        replace(" {2}", "&nbsp;&nbsp;");

        doubleQuotesSpan("php_doublequotes");
        singleQuotesSpan("php_singlequotes");
        keywordSpan("class", "php_class");
        keywordSpan("return", "php_return");
        keywordSpan("public", "php_modifier");
        keywordSpan("private", "php_modifier");
        keywordSpan("protected", "php_modifier");
        keywordSpan("static", "php_modifier");
        keywordSpan("function", "php_function");
        keywordSpan("super", "php_super");
        keywordSpan("this", "php_this");
        keywordSpan("new", "php_new");
        keywordSpan("throw", "php_throw");
        keywordSpan("if", "php_if");
        keywordSpan("else", "php_if");
        keywordSpan("switch", "php_switch");
        keywordSpan("case", "php_case");
        keywordSpan("break", "php_break");
        keywordSpan("default", "php_default");

        functionName();
        variable();
        
        environmentSpan("\\/\\*", "\\*\\/", "php_bigcomment");
        environmentSpan("\\/\\/", "(?=\\n)", "php_linecomment");

    }
    private void functionName(){
        String regex = "(\\w\\w*)(?=\\()";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(new String(original));
        while (m.find()) {
            if(inString(m.start()))continue;
            addInsert(m.start(), "<span class=\"php_functionname\">");
            addInsert(m.end(), "</span>");
        }
    }
    private void variable(){
        String regex = "(\\$\\w\\w*)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(new String(original));
        while (m.find()) {
            if(inString(m.start()))continue;
            addInsert(m.start(), "<span class=\"php_variable\">");
            addInsert(m.end(), "</span>");
        }
    }
}
