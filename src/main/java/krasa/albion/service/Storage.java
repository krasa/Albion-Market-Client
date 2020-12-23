package krasa.albion.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.ListView;
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

	public void save(TextField name, ListView cities, ListView tier, ListView quality) {
		StorageData storageData = new StorageData();
		storageData.cities.addAll(cities.getSelectionModel().getSelectedItems());
		storageData.tier.addAll(tier.getSelectionModel().getSelectedItems());
		storageData.quality.addAll(quality.getSelectionModel().getSelectedItems());
		storageData.name = name.getText();

		try {
			String s = new ObjectMapper().writeValueAsString(storageData);


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


	public void load(TextField name, ListView cities, ListView tier, ListView quality) {
		try {
			StorageData storageData;
			if (Files.exists(STORAGE)) {

				String s1 = Files.readString(STORAGE);
				storageData = new ObjectMapper().readValue(s1, StorageData.class);
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StorageData {
		private List<String> tier = new ArrayList<>();
		private List<String> cities = new ArrayList<>();
		private List<String> quality = new ArrayList<>();
		private String name = "";

		public List<String> getTier() {
			return tier;
		}

		public void setTier(List<String> tier) {
			this.tier = tier;
		}

		public List<String> getCities() {
			return cities;
		}

		public void setCities(List<String> cities) {
			this.cities = cities;
		}

		public List<String> getQuality() {
			return quality;
		}

		public void setQuality(List<String> quality) {
			this.quality = quality;
		}


		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}
