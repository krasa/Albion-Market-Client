package krasa.albion.domain;

public class Quality {
	public static int asCode(String s) {
		switch (s) {
			case ("Normal"):
				return 1;
			case ("Good"):
				return 2;
			case ("Outstanding"):
				return 3;
			case ("Excellent"):
				return 4;
			case ("Masterpiece"):
				return 5;
			default:
				throw new RuntimeException(s);
		}
	}

	public static String asName(Integer s) {
		if (s == null) {
			return "null";
		}
		switch (s) {
			case (1):
				return "Normal";
			case (2):
				return "Good";
			case (3):
				return "Outstanding";
			case (4):
				return "Excellent";
			case (5):
				return "Masterpiece";
			default:
				throw new RuntimeException("quality=" + s);
		}
	}

	public static Integer codeToIp(Integer s) {
		if (s == null) {
			return 0;
		}

		switch (s) {
			case (1):
				return 0;
			case (2):
				return 10;
			case (3):
				return 20;
			case (4):
				return 50;
			case (5):
				return 100;
			default:
				throw new RuntimeException("quality=" + s);
		}
	}


}
