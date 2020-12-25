package krasa.albion.utils;

import javafx.scene.image.Image;
import krasa.albion.Launcher;

import java.util.Objects;

public class MyUtils {

	public static Image getImage(String name) {
		return new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream(name)));
	}
}
