package ui;

public class Solution {
	
	public static void main(String[] args) {
		if(args.length <2 || args.length>3){
			System.out.println("Krivi argumenti");
		}
		
		//bez hiperparametara
		if(args.length==2) {
			ID3 id3= new ID3(args[0], args[1]);
		}
		else if(args.length==3) {
			ID3 id3= new ID3(args[0], args[1], Integer.parseInt(args[2]));
		}
	}
}
