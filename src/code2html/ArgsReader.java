package code2html;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Class used to read arguments from the command line, for example:<br/>
 *       &emsp;ArgsReader reader = new ArgsReader();<br/>
 *       &emsp;reader.addFlag("-h", "HELP", "Prints argument descriptions");<br/>
 *       &emsp;reader.addFlag("--help", "HELP");<br/>
 *       &emsp;reader.addProperty("-i", "INPUT", "Some input file");<br/>
 *       &emsp;reader.loadArgs(args);<br/>
 *       &emsp;if(reader.hasFlag("HELP")){<br/>
 *           &emsp;&emsp;System.out.println(reader.helpText());<br/>
 *           &emsp;&emsp;System.exit(0);<br/>
 *       &emsp;}<br/>
 *       &emsp;if(reader.hasProperty("INPUT")){<br/>
 *           &emsp;&emsp;System.out.println(reader.getString("INPUT"));<br/>
 *           &emsp;&emsp;System.exit(0);<br/>
 *       &emsp;}<br/>
 * @author Max Dupenois
 */
public class ArgsReader {

    private class Argument {

        private boolean hasValue;
        private String propertyName;
        private String description;
        private String value;

        public Argument(String propertyName, boolean hasValue) {
            this(propertyName, hasValue, "");
        }

        public Argument(String propertyName, boolean hasValue, String description) {
            this.propertyName = propertyName;
            this.hasValue = hasValue;
            if (!this.hasValue) {
                this.value = "false";
            }
            this.description = description;
        }

        private void setValue(String value) {
            this.value = value;
        }
    }
    private Hashtable<String, Argument> propertyNameToArgument;
    private Hashtable<String, String> keyToPropertyName;

    public ArgsReader() {
        keyToPropertyName = new Hashtable<String, String>();
        propertyNameToArgument = new Hashtable<String, Argument>();
    }

    public void addProperty(String key, String propertyName) {
        this.addProperty(key, propertyName, "");
    }

    public void addProperty(String key, String propertyName, String description) {
        if (keyToPropertyName.containsKey(key)) {
            throw new IllegalArgumentException("Already have property with key: '" + key + "'");
        }
        if (!propertyNameToArgument.containsKey(propertyName)) {
            Argument argument = new Argument(propertyName, true, description);
            propertyNameToArgument.put(propertyName, argument);
        }
        keyToPropertyName.put(key, propertyName);
    }

    public void addFlag(String key, String propertyName) {
        this.addFlag(key, propertyName, "");
    }

    public void addFlag(String key, String flagName, String description) {
        if (keyToPropertyName.containsKey(key)) {
            throw new IllegalArgumentException("Already have property with key: '" + key + "'");
        }
        if (!propertyNameToArgument.containsKey(flagName)) {
            Argument argument = new Argument(flagName, false, description);
            propertyNameToArgument.put(flagName, argument);
        }
        keyToPropertyName.put(key, flagName);
    }

    public void loadArgs(String[] args) throws IllegalArgumentException {
        String key;
        String propertyName;
        String value;
        Argument argument;
        for (int i = 0; i < args.length; i++) {
            key = args[i];
            if (keyToPropertyName.containsKey(key)) {
                propertyName = keyToPropertyName.get(key);
                argument = propertyNameToArgument.get(propertyName);
                if (argument.hasValue) {
                    if (args.length <= i + 1) {
                        throw new IllegalArgumentException("Key '" + key + "' for property '" + propertyName + "' requires a value");
                    }
                    value = args[i + 1];
                    argument.setValue(value);
                    i++; // skip next as it is not an argument
                } else {
                    argument.setValue("true");
                }
            } else {
                throw new IllegalArgumentException("'" + key + "' not a recognised key");
            }
        }
    }

    public String getString(String propertyName) {
        if (!propertyNameToArgument.containsKey(propertyName)) {
            return null;
        }
        return propertyNameToArgument.get(propertyName).value;
    }

    public boolean hasProperty(String popertyName) {
        if (!propertyNameToArgument.containsKey(popertyName)) {
            return false;
        }
        return (propertyNameToArgument.get(popertyName).value != null);
    }

    public boolean hasFlag(String flagName) {
        if (!propertyNameToArgument.containsKey(flagName)) {
            return false;
        }
        return getBoolean(flagName);
    }

    public int getInt(String propertyName) {
        return Integer.parseInt(getString(propertyName));
    }

    public double getDouble(String propertyName) {
        return Double.parseDouble(getString(propertyName));
    }

    public float getFloat(String propertyName) {
        return Float.parseFloat(getString(propertyName));
    }

    public boolean getBoolean(String propertyName) {
        return Boolean.parseBoolean(getString(propertyName));
    }

    private String[] getKeys(String propertyName) {
        ArrayList<String> keys = new ArrayList<String>();
        for (String key : keyToPropertyName.keySet()) {
            if (keyToPropertyName.get(key).equals(propertyName)) {
                keys.add(key);
            }
        }
        return keys.toArray(new String[keys.size()]);
    }

    public String helpText() {
        StringBuffer helpText = new StringBuffer();
        String[] keys;
//        String key;
        boolean firstKey;
        boolean firstProperty = true;
        String description;
        for (String propertyName : propertyNameToArgument.keySet()) {
            if (!firstProperty) {
                helpText.append("\n");
            }
            keys = getKeys(propertyName);
            firstKey = true;
            for (String key : keys) {
                helpText.append((!firstKey ? ", " : "") + key);
                firstKey = false;
            }
            helpText.append("\t\t" + propertyName);
            description = propertyNameToArgument.get(propertyName).description;
            if (description != null && description.length() > 0) {
                helpText.append("\t\t" + description);
            }
            firstProperty = false;
        }
        return helpText.toString();
    }
}
