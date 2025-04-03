
// v001 11/10/2024

public class Main {
	public static void main(String[] args) {
		Tests tests =  new Tests();
		
		// USED THE EXAMPLE TEST CASES FOR 2 AND 6
		// COMMENTED OUT MY OWN TEST CASES
		// RIGHT BELOW THE EXAMPLE TEST CASES
		
		System.out.println("\n\nuserRequirement1:");
		tests.userRequirement1();	
		System.out.println("\n\nUR2 EXAMPLE TEST:");
		tests.exampleUR2Test();	
//		System.out.println("\n\nuserRequirement2:"); 		
//		tests.userRequirement2();                           
		System.out.println("\n\nuserRequirement3:");
		tests.userRequirement3();
		System.out.println("\n\nuserRequirement4:");
		tests.userRequirement4();
		System.out.println("\n\nuserRequirement5:");
		tests.userRequirement5();
		System.out.println("\n\nUR6 EXAMPLE TEST:");
		tests.exampleUR6test();		
//		System.out.println("\n\nuserRequirement6:");        
//		tests.userRequirement6();                          

	}
}
