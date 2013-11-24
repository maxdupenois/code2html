/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code2html;

import code2html.parsers.JavaParser;
import code2html.parsers.PhPParser;
import java.io.IOException;

/**
 *
 * @author mpd209
 */
public class Main {
    private class ArgsReaderValues{
        private final static String HELP = "help";
        private final static String INPUT = "input";
        private final static String OUTPUT = "output";
        private final static String NEWLINE = "newline";
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException{
        ArgsReader reader = new ArgsReader();
        reader.addFlag("-h",        ArgsReaderValues.HELP, "Prints argument descriptions");
        reader.addFlag("--help",    ArgsReaderValues.HELP);
        reader.addProperty("-i",        ArgsReaderValues.INPUT, "Input file to convert to html");
        reader.addProperty("-o",    ArgsReaderValues.OUTPUT, "File to read out to");
        reader.addFlag("-l",    ArgsReaderValues.NEWLINE, "If included the file is one line long");

        reader.loadArgs(args);
        if(reader.hasFlag(ArgsReaderValues.HELP)){
            System.out.println(reader.helpText());
            System.exit(0);
        }
        if(!reader.hasProperty(ArgsReaderValues.INPUT)){
            System.out.println("No input file given");
            System.exit(1);
        }
        if(!reader.hasProperty(ArgsReaderValues.OUTPUT)){
            System.out.println("No output file given");
            System.exit(1);
        }
        String inFile = reader.getString(ArgsReaderValues.INPUT);
        String outFile = reader.getString(ArgsReaderValues.OUTPUT);
        String ext = inFile.substring(inFile.lastIndexOf(".")+1).toUpperCase();
        Parser parser = null;
        if(ext.equals("PHP")){
            try {
                System.out.println("- Loading Files");
                parser = new PhPParser(inFile, outFile);
                parser.setSingleLine(reader.hasFlag(ArgsReaderValues.NEWLINE));
                System.out.println("- Loaded Files");
            } catch (IOException iOException) {
                System.out.println("Parser Construction Failed '"+iOException.getMessage()+"'");
                iOException.printStackTrace();
                System.exit(1);
            }
        }else if(ext.equals("JAVA")){
            try {
                System.out.println("- Loading Files");
                parser = new JavaParser(inFile, outFile);
                parser.setSingleLine(reader.hasFlag(ArgsReaderValues.NEWLINE));
                System.out.println("- Loaded Files");
            } catch (IOException iOException) {
                System.out.println("Parser Construction Failed '"+iOException.getMessage()+"'");
                iOException.printStackTrace();
                System.exit(1);
            }
        }
        if(parser==null){
            System.out.println("Extension '"+ext+"' not recognised.");
            System.exit(1);
        }else{
            try {
                System.out.println("- Starting Parse");
                parser.parse();
                System.out.println("- Finished conversion");
            } catch (IOException iOException) {
                System.out.println("Parse Failed '"+iOException.getMessage()+"'");
                iOException.printStackTrace();
                System.exit(1);
            }
        }
    }

}
