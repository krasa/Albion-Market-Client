package krasa.albion.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class Storage {
	public static final Path STORAGE = Paths.get("settings.json");
	public static final Path STORAGE_OLD = Paths.get("settings.json.old");
	public static final Path STORAGE_TMP = Paths.get("settings.json.tmp");

	public void save(TextField name, ListView<String> cities, ListView<String> tier, ListView<String> quality, Slider ipFrom, Slider ipTo) {
		StorageData storageData = new StorageData();
		storageData.cities.addAll(cities.getSelectionModel().getSelectedItems());
		storageData.tier.addAll(tier.getSelectionModel().getSelectedItems());
		storageData.quality.addAll(quality.getSelectionModel().getSelectedItems());
		storageData.name = name.getText();
		storageData.ipFrom = ipFrom.getValue();
		storageData.ipTo = ipTo.getValue();

		try {
			String s = getObjectMapper().writeValueAsString(storageData);


			if (Files.exists(STORAGE)) {
				if (s.equals(Files.readString(STORAGE))) {
					return;
				}
			}

			Files.deleteIfExists(STORAGE_TMP);
			Files.writeString(STORAGE_TMP, s.toString());
			if (Files.exists(STORAGE)) {
				Files.move(STORAGE, STORAGE_OLD, StandardCopyOption.REPLACE_EXISTING);
			}
			Files.move(STORAGE_TMP, STORAGE);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public void load(TextField name, ListView<String> cities, ListView<String> tier, ListView<String> quality, Slider ipFrom, Slider ipTo) {
		try {
			StorageData storageData;
			if (Files.exists(STORAGE)) {

				String s1 = Files.readString(STORAGE);
				storageData = getObjectMapper().readValue(s1, StorageData.class);
			} else {
				storageData = new StorageData();
			}


			for (String city : storageData.cities) {
				cities.getSelectionModel().select(city);
			}
			for (String city : storageData.quality) {
				quality.getSelectionModel().select(city);
			}
			for (String city : storageData.tier) {
				tier.getSelectionModel().select(city);
			}
			name.setText(storageData.name);
			ipFrom.setValue(storageData.ipFrom);
			ipTo.setValue(storageData.ipTo);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		return objectMapper;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StorageData {
		public double ipFrom = 1000;
		public double ipTo = 1400;
		private List<String> tier = new ArrayList<>();
		private List<String> cities = new ArrayList<>();
		private List<String> quality = new ArrayList<>();
		private String name = "";

	}

}
