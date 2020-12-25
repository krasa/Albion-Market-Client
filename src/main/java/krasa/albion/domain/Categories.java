package krasa.albion.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

	@Override
	public String toString() {
		return name;
	}
}
