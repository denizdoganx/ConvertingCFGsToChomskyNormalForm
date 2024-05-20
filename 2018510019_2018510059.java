package CFG;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;


public class Test {
	public static ArrayList<String> fileOperations() throws IOException {//This part read CFG.txt and write in an arraylist line by line
		@SuppressWarnings("resource")
		BufferedReader objreader = new BufferedReader(new FileReader("CFG.txt", Charset.forName("utf-8")));
		String s;
		ArrayList<String> cfg =new ArrayList<>();
		while ((s = objreader.readLine()) != null) {
			s=s.replace(" ", "");//removing spaces from string
			cfg.add(s);
		   }
		
	
		return cfg;
	}
	public static ArrayList<String> eliminating€(ArrayList<String> cfg){
		//This function deletes the phrase € from the lines containing indirect or direct € and applies the necessary eliminating € operation.
		ArrayList<String> emptystring =new ArrayList<>();
		System.out.println("----Eliminate €----");
		for(int i=0;i<cfg.size();i++) {//find strings containing €
			String[] str = cfg.get(i).split("[|-]");
			if(cfg.get(i).charAt(0)=='E')
				continue;
			for(int j=1;j<str.length;j++) {
				if(str[j].contains("€")) {
					emptystring.add(str[0]);
				}	
			}	
		}
		int x =0;
		boolean flag=true, checking=true;
		while(flag) {//find strings with € in them implicitly
			if(x==cfg.size()) {
				x=0;
				if(!checking)
					flag=false;
				checking = false;//For terminals that are newly added to the emptystring and cannot be controlled
			}
			String[] str = cfg.get(x).split("[|-]");
			if(cfg.get(x).charAt(0)=='E') {
				x++;
				continue;
			}
			
			for(int i=1;i<str.length;i++) {
				for(int j=0; j<emptystring.size();j++) {
					if(str[i].length()==1 && str[i].contains(emptystring.get(j)) && !emptystring.contains(str[0])) {
						emptystring.add(str[0]);
						checking=true;//provides one more check for newly added emptystring
						if(str[0].equals("S"))
							cfg.set(x,cfg.get(x)+"|€");
					}				
				}
					
			}
			x++;
			
		}
		for(int i=0;i<cfg.size();i++) {//eliminate strings containing €
			if(cfg.get(i).charAt(0)=='E')
				continue;
			else if(cfg.get(i).contains("€") && cfg.get(i).charAt(0)!='S'){//removing '€' from string
				cfg.set(i,cfg.get(i).replace("|€", ""));
				cfg.set(i,cfg.get(i).replace("€", ""));
				cfg.set(i,cfg.get(i).replace("||", "|"));
			}
			for(int j=0;j<emptystring.size();j++) {
				if(cfg.get(i).contains(emptystring.get(j))) {
					String[] str = cfg.get(i).split("[|-]");
					for(int k=1;k<str.length;k++) {
						if(str[k].contains(emptystring.get(j)) && str[k].replaceFirst(emptystring.get(j), "")!="") {
							cfg=organizingCfg(cfg, emptystring, str, i, k, j);//edit the cfg
							cfg.set(i,cfg.get(i).replace(".", emptystring.get(j)));
							cfg.set(i,cfg.get(i)+"|"+str[k].replace(emptystring.get(j),""));
							cfg.set(i,cfg.get(i).replace(".", ""));
						}
						
					}
				}	
			}
		}
		cfg=removingRepetitiveStrings(cfg);
		printcfg(cfg);
		
		return cfg;
	}
	public static ArrayList<String> organizingCfg(ArrayList<String> cfg,ArrayList<String> emptystring, String[] str,int i,int k,int j){
		cfg.set(i,cfg.get(i)+"|"+str[k].replaceFirst(emptystring.get(j), ""));//Adding the terminal containing € to the continuation of the cfg
		str[k]=str[k].replaceFirst(emptystring.get(j), ".");//After editing the cfg, it puts a dot where the emtpystring is and checks if there are any other empystrings.
		if(str[k].contains(emptystring.get(j)))
			organizingCfg(cfg, emptystring, str, i, k, j);	
		
		return cfg;
	}
	public static ArrayList<String> eliminateUnitProductions(ArrayList<String> cfg) {
		int x;
		for(int i=1;i<cfg.size();i++) {
			String[] str = cfg.get(i).split("[|-]");
			int bound = str.length;
			for(int j=1;j<bound;j++) {
				if(str[j].length()==1) {
					if(str[j].equals(str[0])){//The replace command worked incorrectly due to the "|" statement, so "9" was put in place of "|" and the action was taken accordingly.
						cfg.set(i, cfg.get(i).replace("-", "|"));
						cfg.set(i, cfg.get(i)+"|");
						cfg.set(i, cfg.get(i).replace("|","9"));
						cfg.set(i, cfg.get(i).replaceFirst("9"+str[j]+"9","9"+""));
						cfg.set(i, cfg.get(i).replaceFirst("9","-"));    
						cfg.set(i, cfg.get(i).replace("9","|"));               	
					}
					x=findingTerminal(cfg,str[j]);//finds which terminal starts str[j]
					if(x!=0) {//if x equals zero couldn't find such a terminal
						String line_of_unit_production = cfg.get(x);
						line_of_unit_production = line_of_unit_production.substring(2);
						cfg.set(i, cfg.get(i).replace("-", "|"));
						cfg.set(i, cfg.get(i)+"|");
						cfg.set(i, cfg.get(i).replace("|","9"));
						cfg.set(i, cfg.get(i).replaceFirst("9"+str[j]+"9","9"+line_of_unit_production+"9"));
						cfg.set(i, cfg.get(i).replaceFirst("9","-"));    
						cfg.set(i, cfg.get(i).replace("9","|"));
						cfg.set(i, cfg.get(i).replace("||","|"));
						cfg.set(i, cfg.get(i).replace("||","|"));
						eliminateUnitProductions(cfg);//Checks if there are any other unit production processes
						
					}
					
				}
			}
		}
		removingRepetitiveStrings(cfg);//sometimes repeating expressions take them out of the string
		return cfg;
	}
	public static int findingTerminal(ArrayList<String> cfg,String s) {//This function finds at which index of cfg the terminal in the var string starts for unit production.
		int x=0;
		for(int i=1;i<cfg.size();i++) {
			if(cfg.get(i).startsWith(s)) {
				x=i;
				break;
			}				
		}
		return x;
	}
	
	public static ArrayList<String> removingRepetitiveStrings(ArrayList<String> cfg){//removes repetitive expressions from arraylist
		boolean flag=false;
		for(int i=0;i<cfg.size();i++) {
			String[] str = cfg.get(i).split("[|-]");
			for(int j=1;j<str.length;j++) {
				for(int k=1;k<str.length;k++) {
					if(j!=k && str[j].equals(str[k])) {
						cfg.set(i, cfg.get(i).replace("-", "|"));
						cfg.set(i, cfg.get(i).replace("|","9"));//replacefirst "|" It doesn't see the symbol so I changed it to 9.
						cfg.set(i, cfg.get(i).replaceFirst("9"+str[k]+"9","9"));
						cfg.set(i, cfg.get(i).replaceFirst("9","-"));
						cfg.set(i, cfg.get(i).replace("9","|"));
						i--;
						flag=true;
						break;
					}
				}
				if(flag) {
					flag=false;
					break;
				}
			}
		}
		return cfg;
	}
	public static void printcfg(ArrayList<String> cfg) {//prints cfg to console screen
		for(int i=1;i<cfg.size();i++) {
			System.out.println(cfg.get(i));
		}
	}
	
	public static ArrayList<String> eliminatingTerminals(ArrayList<String> cfg){//this function creates a new terminal with the expressions in the alphabet (for example E=0.1) and assigns them there.
		ArrayList<pairofTerminalandAlphabet> pair =identifyAlphabetAndTerminalPairs(cfg);
		for(int i=0;i<pair.size();i++) {
			cfg.add(String.valueOf(pair.get(i).getTerminal())+"-"+pair.get(i).getAlphabet());
		}
		for(int i=1;i<cfg.size();i++) {
			String[] str = cfg.get(i).split("[|-]");
			cfg.set(i, "");
			for(int j=0;j<str.length;j++) {
				for(int k=0;k<pair.size();k++) {
					if(str[j].length()!=1 && str[j].contains(pair.get(k).alphabet)) {//Inserts the terminal assigned as pairs instead of elements of the alphabet inside the string
						str[j] = str[j].replace(pair.get(k).getAlphabet(), String.valueOf(pair.get(k).getTerminal()));
					}
					
				}
				if(j==0) 
					cfg.set(i,str[0]+"-");
				else {
					cfg.set(i,cfg.get(i)+str[j]+"|");
				}
					
			
			}
			
		}
		return cfg;
	}
	public static ArrayList<pairofTerminalandAlphabet> identifyAlphabetAndTerminalPairs(ArrayList<String> cfg){
		ArrayList<pairofTerminalandAlphabet> pair = new ArrayList<>();//keeps new terminal and alphabet pairs
		ArrayList<Character> letters =new ArrayList<>();
    	for(char letter= 'A'; letter <= 'Z'; ++letter)
    		letters.add(letter);//adds all the letters of the alphabet to the letters arraylist then removes the terminals from this arraylist so new terminals are created differently from the previous ones
    	for(int i=0;i<letters.size();i++) {
			for(int j=0;j<cfg.size();j++) {
				if(letters.get(i)==cfg.get(j).charAt(0)) {
					letters.remove(i);
					i--;
					break;
				}
			}
		}
		String[] str = cfg.get(0).split("[|=,]");
		for(int i=1;i<str.length;i++) {
			pairofTerminalandAlphabet e = new pairofTerminalandAlphabet(str[i],letters.get(letters.size()-1));
			pair.add(e);
			letters.remove(letters.size()-1);
		}
		
		return pair;
	}
	
	public static ArrayList<String> breakVariableStrings(ArrayList<String> cfg){//Breaking variables with a length of more than 2
		ArrayList<Character> letters =new ArrayList<>();
		ArrayList<String> pairOfTerminalAndVariable =new ArrayList<>();//this arraylist keeps terminal in even index, variable in odd index
		for(char letter= 'A'; letter <= 'Z'; ++letter)
    		letters.add(letter);
		for(int i=0;i<letters.size();i++) {
			for(int j=0;j<cfg.size();j++) {
				if(letters.get(i)==cfg.get(j).charAt(0)) {
					letters.remove(i);//removing already existing terminals from arraylist
					i--;
					break;
				}
			}
		}
		cfg = reducingTheNumberOfVariables(cfg, letters, pairOfTerminalAndVariable);//abbreviation and new terminal creation process
		
		addingS0Terminal(cfg);
		return cfg;
	}
	public static int findingIndexOfTerminal(ArrayList<String> pairOfTerminalAndVariable,String str) {
		int index=-1;
		for(int k=0;k<pairOfTerminalAndVariable.size();k++) {//Check if there is a terminal containing this variable before
			if(str.contains(pairOfTerminalAndVariable.get(k)) && pairOfTerminalAndVariable.get(k).length()==2) {
				index=k;
				break;
			}
		}
		return index;
	}
	public static ArrayList<String> reducingTheNumberOfVariables(ArrayList<String> cfg,ArrayList<Character> letters,ArrayList<String> pairOfTerminalAndVariable){
		int count=0;//count is a control mechanism that checks for the presence of more than 1 expression in the string.
		for(int i=1;i<cfg.size();i++) {
			String[] str = cfg.get(i).split("[|-]");
			cfg.set(i, "");
			for(int j=0;j<str.length;j++) {
				if(str[j].length()>2) {//Checking and breaking strings longer than 2
					int index = findingIndexOfTerminal(pairOfTerminalAndVariable, str[j]);
					if(index!=-1) {//if index is -1, no such terminal has been created before	
						str[j]=str[j].substring(0,str[j].length()-2)+pairOfTerminalAndVariable.get(index-1);
						count++;
					}
					else {
						cfg.add(letters.get(letters.size()-1)+"-"+str[j].substring(str[j].length()-2));
						pairOfTerminalAndVariable.add(String.valueOf(letters.get(letters.size()-1)));
						pairOfTerminalAndVariable.add(str[j].substring(str[j].length()-2));
						str[j]=str[j].substring(0,str[j].length()-2)+letters.get(letters.size()-1);
						letters.remove(letters.size()-1);
						count++;
					}
					
				}
				//Editing the cfg line and adding it sequentially from the beginning
				if(j==0) 
					cfg.set(i,str[0]+"-");
				else {
					cfg.set(i,cfg.get(i)+str[j]+"|");
				}
				if(count>1 && j==str.length-1) {
					count=0;
					reducingTheNumberOfVariables(cfg, letters, pairOfTerminalAndVariable);
				}
			}
			
			
		}
		
		return cfg;
	}
	public static ArrayList<String> addingS0Terminal(ArrayList<String> cfg){//if S terminals contains S string this function add NEW S0 terminals 
		boolean flagS0=false;
		for(int i=0;i<cfg.size();i++) {
			String[] str = cfg.get(i).split("[|-]");
			if(str[0].equals("S")) {
				for(int j=1;j<str.length;j++) {
					if(str[j].contains("S"))
						flagS0=true;
				}
			}
		}
		if(flagS0)
			cfg.add(1,cfg.get(1).replaceFirst("S", "S0"));
		return cfg;
	}
	

	public static void main(String[] args) throws IOException {
		ArrayList<String> cfg=new ArrayList<>();
		cfg=fileOperations();
		if(cfg.get(0).startsWith("E=")) {
			System.out.println(cfg.get(0));
			printcfg(cfg);
			cfg=eliminating€(cfg);
			System.out.println("\n----Eliminate Unit Production----");
			cfg=eliminateUnitProductions(cfg);
			printcfg(cfg);
			System.out.println("\n----Eliminate Terminals----");
			cfg=eliminatingTerminals(cfg);
			printcfg(cfg);
			System.out.println("\n----Break Variable Strings Longer Than 2----");
			cfg=breakVariableStrings(cfg);
			printcfg(cfg);
		}
		else
			System.out.println("Please edit your file alphabet E=0,1 in the first line");
		
		
	}

}

class pairofTerminalandAlphabet{//This class is created because a terminal and a structure that holds the letter opposite is required.
	String alphabet;
	char terminal;
	
	public pairofTerminalandAlphabet() {
		
	}
	public pairofTerminalandAlphabet(String a, char t) {
		alphabet=a;
		terminal=t;
	}

	public String getAlphabet() {
		return alphabet;
	}

	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}

	public char getTerminal() {
		return terminal;
	}

	public void setTerminal(char terminal) {
		this.terminal = terminal;
	}
	
}
