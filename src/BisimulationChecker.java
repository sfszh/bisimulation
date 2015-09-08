import java.io.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Comparator;

public class BisimulationChecker {
    Process processP, processQ;
    Set<Set<String>> formatedOutput;
    
    // additional requirement 1
    public BisimulationChecker() {
        this.processP = null;
        this.processQ = null;
        this.formatedOutput = null;
    }
    
 // additional requirement 2
    public void readInput(String fileP, String fileQ) {
    	String p = "P", q = "Q";
    	try {	
	    	BufferedReader brP = new BufferedReader(new FileReader(fileP));
	        processP = makeLTS(brP, p);
	        brP.close();
	
	        BufferedReader brQ = new BufferedReader(new FileReader(fileQ));
	        processQ = makeLTS(brQ, q);
	        brQ.close();
    	}catch(Exception e){
    		System.out.println(usage);
    		e.printStackTrace();
    		System.exit(-1);
    	}
    }

    // additional requirement 3
    public void performBisimulation() {
    	if (processP == null || processQ == null){
    		System.err.println("No valid input files given");
    	} else {
    		formatedOutput = bisimulationComputation(processP, processQ);
    	}
    }
    
    // additional requirement 4
    public void writeOutput(String filename) {
    	try {
	        while (filename == null || filename.length() < 1) {
	            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
	            System.out.print("Enter an output filename: ");
	            System.out.flush();
	            filename = r.readLine();
	            r.close();
	        }
	
	        BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(filename));
	        bufferWriter.write("Process P\n" + processP.finalOutput());
	        bufferWriter.write("Process Q\n" + processQ.finalOutput());
	        bufferWriter.write("Bisimulation Results\n");
	        boolean bisimilar = true;
	        for (Set<String> states : this.formatedOutput) {
	            Set<Character> prefix = new HashSet<Character>();
	
	            for (String s : states)
	                prefix.add(s.charAt(0));
	
	            bisimilar = prefix.contains('P') && prefix.contains('Q') && bisimilar;
	            Iterator<String> it = states.iterator();
	            while (it.hasNext())
	                bufferWriter.write(it.next().substring(1) + (it.hasNext() ? "," : ""));
	            
	            bufferWriter.write("\n");
	        }
	        bufferWriter.write("Bisimulation Answer\n" + (bisimilar ? "Yes" : "No"));
	        bufferWriter.close();
    	}catch(IOException e){ 
    		e.printStackTrace();
    	}
    }
    
    /**
     * Makes each process from the files given
     * @param br - bufferedreader to read in file
     * @param processName - current process we are loading in
     * @return - null on fail
     */
	private Process makeLTS(BufferedReader br, String processName) throws IOException {
        Process pr = new Process();
        String line = "";
       try{
        while (line != null) {
            line = br.readLine();
            if (line.startsWith("!"))
                return pr;

            String values[] = line.split("[,:]"), 					// values:
                   source = processName + values[0].trim(), 		//   start
                   action = values[1].trim(),           			//  action
                   destination = processName + values[2].trim();   	//     end

            pr.states.add(source);
            pr.actions.add(action);
            pr.states.add(destination);
            pr.transitions.add(new Process.Transition(source, destination, action));
        }
       }
       catch(Exception e){
    	   e.printStackTrace();
    	   System.err.println("maybe you should end file with \"!\" in " + processName );
    	   System.exit(-1);
       }
        return null;
    }

	
    /**
     * Performs the bisimulation computation based on the algorithm from the course book
     * @param p	- Process P
     * @param q	- Process Q
     * @return	rho - the final output of the bisimulation check
     */
    private Set<Set<String>> bisimulationComputation(Process p, Process q) {
    	// 1.
		Set<Process.Transition> ts = new HashSet<Process.Transition>();
    	ts.addAll(p.transitions);
    	ts.addAll(q.transitions);

		Set<String> sigma = new HashSet<String>();
    	sigma.addAll(p.actions);
    	sigma.addAll(q.actions);

    	Set<Set<String>> rho1 = new HashSet<Set<String>>();
    	Set<String> initial = new HashSet<String>();
    	rho1.add(initial);
    	initial.addAll(p.states);
    	initial.addAll(q.states);
		
    	// 2.
    	Set<Set<String>> rho = new HashSet<Set<String>>(rho1);

    	// 3.
    	Set<Set<String>> waiting = new HashSet<Set<String>>(rho1);

    	// 4.
    	while (!waiting.isEmpty()) {
    		// 4.1
			Set<String> pPrime = waiting.iterator().next();
			waiting.remove(pPrime);

    		// 4.2
    		for (String a : sigma) {
    			// 4.2.1
				Set<Set<String>> matchP = new HashSet<Set<String>>();
				
				for (Set<String> s : rho) {
					Set<String> taP = buildTaP(s, pPrime, a, ts);

					if (!taP.isEmpty() && !taP.equals(s))
						matchP.add(s);
				}

				// 4.2.2
    			for (Set<String> p2 : matchP) {
    				Set<Set<String>> splitP = split(p2, a, pPrime, ts);

    				rho.remove(p2);
    				rho.addAll(splitP);

    				waiting.removeAll(p2);
    				waiting.addAll(splitP);
    			}
    		}
    	}

    	return rho;
	}
    
    /**
     * based on Ta[P] from course book
     */
    private static Set<String> buildTaP(Set<String> p, Set<String> pPrime, String a, Set<Process.Transition> ts) {
		Set<String> out = new HashSet<String>();

		for (String s : p)
			for (String sPrime : pPrime)
				if (ts.contains(new Process.Transition(s, sPrime, a))) {
					out.add(s);
					break;
				}

        return out;
    }
    
	/**
	 *  Split based on course book
	 */
    private static Set<Set<String>> split(Set<String> p, String a, Set<String> pPrime, Set<Process.Transition> ts) {
    	Set<Set<String>> splitP = new HashSet<Set<String>>();
        Set<String> tap = buildTaP(p, pPrime, a, ts);
        Set<String> partition = new HashSet<String>(p);

		partition.removeAll(tap);
		splitP.add(tap);
		splitP.add(partition);

        return splitP;
    }

    public static void main(String[] args) {
        BisimulationChecker checker = new BisimulationChecker();
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        if(args.length >= 2) {
        	checker.readInput(args[0], args[1]);
        	checker.performBisimulation();
        	checker.writeOutput(args[2]);
        }else{
        	System.out.println(usage);
        	System.exit(-1);
        }
    }
    
    public static String usage = "usage: BisimulationChecker ProcessPFile ProcessQFile [outputfileName] \n";

}

