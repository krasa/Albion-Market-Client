package krasa.albion.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum Categories {
	ALL("---"),
	WEAPON("Weapon"),
	EQUIPMENT_ITEM("Equipment Item"),
	MOUNT("Mount"),
	CONSUMABLE_ITEM("Consumable Item"),
	CONSUMABLE_FROM_INVENTORY("Consumable from Inventory"),
	FARMABLE_ITEM("Farmable Item"),
	SIMPLE_ITEM("Simple Item"),
	FURNITURE_ITEM("Furniture Item"),
	JOURNAL_ITEM("Journal Item"),
	LABOURER_CONTRACT("Labourer Contract"),
	MOUNT_SKIN("Mount Skin"),
//	CRYSTAL_LEAGUE_ITEM("Crystal League Item");
	;

	Categories(String name) {
		this.name = name;
	}

	String name;

	@JsonValue
	public String getName() {
		return name;
	}

	@JsonCreator
	public static Categories forValue(String name) {
		return from(name);
	}

	public static Categories from(String s) {
		for (Categories value : Categories.values()) {
			if (value.name.equals(s)) {
				return value;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}
}
