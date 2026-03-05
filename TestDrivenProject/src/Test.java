
public class Test {

	public static void main(String[] args) {

		int expected = 1;
		int actual = factorial(0);

		if (actual == expected) {
			System.out.println("Test passed");
		} else {
			System.out.println("Test failed: expected " + expected + " but got " + actual);
		}

	}

}
