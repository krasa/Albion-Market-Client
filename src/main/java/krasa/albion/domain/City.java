package krasa.albion.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import javafx.scene.paint.Paint;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@JsonIgnoreProperties(ignoreUnknown = true)
public enum City {

	ALL("---", null),
	LYMHURST("Lymhurst", paint("5B9C10")),
	FORT_STERLING("Fort Sterling", paint("FBFDFF")),
	CAERLEON("Caerleon", paint("F00026")),
	MARTLOCK("Martlock", paint("068FA3")),
	THETFORD("Thetford", paint("9C19CD")),
	BRIDGEWATCH("Bridgewatch", paint("EB9026")),
	BLACK_MARKET("Black Market", paint("FFFFFF"));

	//					"Merlyn",
	String name;
	Paint color;

	@JsonCreator
	public static City forValue(String name) {
		return from(name);
	}

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

	public static List<String> getListViewItems() {
		return Arrays.stream(values()).map(city -> city.name).collect(Collectors.toList());
	}

	public Paint getColor() {
		return color;
	}

	@JsonValue
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	private static javafx.scene.paint.Paint paint(String fbfdff) {
		return javafx.scene.paint.Paint.valueOf(fbfdff);
	}

}
