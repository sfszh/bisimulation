import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

//import Process.Transition;

/**
* The process class to create each processes values
*/
class Process {
    HashSet<String> states, actions;
    HashSet<Transition> transitions;

    /** Transition Class to keep track of transitions */
    public static class Transition {
        private String source, destination, action;

        /** Constructor */
        public Transition(String source, String destination, String action) {
            this.source = source;
            this.destination = destination;
            this.action = action;
        }
        
        /**
         * Formats the transition for output
         * @return - the formatted string
         */
        public String transitionOutput() {
            return "(" + source.substring(1) + "," + action + "," + destination.substring(1) + ")";
        }
        
        /** Override the default hashCode and equals functions */
        @Override
        public int hashCode() {
            return Arrays.hashCode(new String[] { source, destination, action });
        }

        @Override
        public boolean equals(Object compare) {
            return this.source.equalsIgnoreCase(((Transition) compare).source)
                    && this.action.equalsIgnoreCase(((Transition) compare).action)
                    && this.destination.equalsIgnoreCase(((Transition) compare).destination);
        }
    }
    
    /** Constructor */
    public Process() {
        this.states = new HashSet<String>();
        this.actions = new HashSet<String>();
        this.transitions = new HashSet<Transition>();
    }

    /**
     * Iterates through a list and appends ',' where needed
     * @param str - string to look at
     * @param sub - boolean if true add ','
     * @return - modified string for output
     */
    private String iterateList(String[] str, boolean sub){
        String out = "";
        Iterator<String> it = Arrays.asList(str).iterator();
        while (it.hasNext()) {
            out += (sub ? it.next().substring(1) : it.next());
            out = (it.hasNext() ? out += "," : out);
        }
        return out;
    }
    
    /**
     * Makes the desired format for the output file
     * @return - string ready to write to file
     */
    public String finalOutput() {
        String output = "S = ";
        String[] statesOut = states.toArray(new String[states.size()]);
        Arrays.sort(statesOut);
        output += iterateList(statesOut, true);
        
        output += "\nA = ";
        String[] actionsOut = actions.toArray(new String[actions.size()]);
        Arrays.sort(actionsOut);
        output += iterateList(actionsOut, false);
        
        output += "\nT = ";
        Transition[] transitionsOut = transitions.toArray(new Transition[transitions.size()]);
        Arrays.sort(transitionsOut, new Comparator<Transition>() {
            @Override
            public int compare(Transition x, Transition y) {
                if (!x.source.equalsIgnoreCase(y.source))
                    return x.source.compareTo(y.source);

                if (!x.action.equalsIgnoreCase(y.action))
                    return x.action.compareTo(y.action);

                return x.destination.compareTo(y.destination);
            }
        });
        
        Iterator<Transition> it = Arrays.asList(transitionsOut).iterator();
        while (it.hasNext()) {
            output += it.next().transitionOutput();
            output = (it.hasNext() ? output += "," : output);
        }
        
        return output += "\n";
    }
}