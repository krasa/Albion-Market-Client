package krasa.albion.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import krasa.albion.controller.MainController;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryItem {

	private List<String> tier = new ArrayList<>();
	private List<City> cities = new ArrayList<>();
	private List<String> quality = new ArrayList<>();
	private String name = "";
	private String path = "";
	;

	public HistoryItem() {
	}

	public HistoryItem(String path, MainController mainController) {
		this.path = path;
		cities.addAll(mainController.cities.getSelectionModel().getSelectedItems());
		tier.addAll(mainController.tier.getSelectionModel().getSelectedItems());
		quality.addAll(mainController.quality.getSelectionModel().getSelectedItems());
		name = mainController.name.getText();
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HistoryItem historyItem = (HistoryItem) o;

		if (tier != null ? !tier.equals(historyItem.tier) : historyItem.tier != null) return false;
		if (cities != null ? !cities.equals(historyItem.cities) : historyItem.cities != null) return false;
		if (quality != null ? !quality.equals(historyItem.quality) : historyItem.quality != null) return false;
		if (name != null ? !name.equals(historyItem.name) : historyItem.name != null) return false;
		return path != null ? path.equals(historyItem.path) : historyItem.path == null;
	}

	@Override
	public int hashCode() {
		int result = tier != null ? tier.hashCode() : 0;
		result = 31 * result + (cities != null ? cities.hashCode() : 0);
		result = 31 * result + (quality != null ? quality.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (path != null ? path.hashCode() : 0);
		return result;
	}

	public List<String> getTier() {
		return tier;
	}

	public void setTier(List<String> tier) {
		this.tier = tier;
	}

	public List<City> getCities() {
		return cities;
	}

	public void setCities(List<City> cities) {
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return name;
	}
}
