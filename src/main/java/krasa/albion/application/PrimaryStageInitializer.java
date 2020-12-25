package krasa.albion.application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import krasa.albion.Launcher;
import krasa.albion.commons.MyUtils;
import krasa.albion.controller.MainController;
import net.rgielen.fxweaver.core.FxWeaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {
	private static final Logger log = LoggerFactory.getLogger(PrimaryStageInitializer.class);
	private final FxWeaver fxWeaver;
	private static Stage stage;

	public static Stage getStage() {
		return stage;
	}

	@Autowired
	public PrimaryStageInitializer(FxWeaver fxWeaver) {
		this.fxWeaver = fxWeaver;
	}

	@Override
	public void onApplicationEvent(StageReadyEvent event) {
		stage = event.stage;
		Parent root = fxWeaver.loadView(MainController.class);
		stage.getIcons().add(MyUtils.getImage("Client.png"));
		Scene scene = new Scene(root, 1800, 800);

		String styleSheetURL = Launcher.class.getResource("dark.css").toString();

		// enable style
		scene.getStylesheets().add(styleSheetURL);
//		
//		// disable style
//		scene.getStylesheets().remove(styleSheetURL);;

		stage.setTitle("Albion Market Client v" + getCurrentVersion());
		stage.setScene(scene);
		stage.show();
	}

	public static String getCurrentVersion() {
		try {
			String[] cfg = Path.of("app").toFile().list((dir, name) -> name.endsWith(".cfg"));
			if (cfg == null || cfg.length != 1) {
				log.error("getCurrentVersion cfg=" + Arrays.toString(cfg));
			}
			String currentVersion = "0";
			if (cfg != null) {
				for (String app : cfg) {
					Path app1 = Path.of("app", app);
					Properties properties = new Properties();
					properties.load(new FileInputStream(app1.toFile()));
					currentVersion = properties.getProperty("app.version");
					break;
				}
			}
			return currentVersion;
		} catch (Throwable e) {
			log.error("", e);
			return "0";
		}
	}
}
