package krasa.albion.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.scene.paint.Paint;


@JsonIgnoreProperties(ignoreUnknown = true)
public enum City {

	ALL("---", null),
	LYMHURST("Lymhurst", paint("5B9C10")),
	FORT_STERLING("Fort Sterling", paint("FBFDFF")),
	CAERLEON("Caerleon", paint("A02C1E")),
	MARTLOCK("Martlock", paint("068FA3")),
	THETFORD("Thetford", paint("9C19CD")),
	BRIDGEWATCH("Bridgewatch", paint("EB9026")),
	BLACK_MARKET("Black Market", null);

	//					"Merlyn",
	String name;
	Paint color;

	City(String name, Paint color) {
		this.name = name;
		this.color = color;
	}

	public static City from(String city) {
		for (City value : City.values()) {
			if (value.name.equals(city)) {
				return value;
			}
		}
		return null;
	}

	public Paint getColor() {
		return color;
	}

	@Override
	public String toString() {
		return name;
	}

	private static javafx.scene.paint.Paint paint(String fbfdff) {
		return javafx.scene.paint.Paint.valueOf(fbfdff);
	}

}
