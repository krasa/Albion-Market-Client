package krasa.albion.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(value = {"?xml"})
public class ItemsWrapper {
	Items items;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Items {
		public List<Item> equipmentitem;
		public List<Item> weapon;

//		public Item shopcategories;
//		public Item hideoutitem;
public List<Item> farmableitem;
		public List<Item> simpleitem;
		public List<Item> consumableitem;
		public List<Item> consumablefrominventoryitem;
		public List<Item> mount;
		public List<Item> furnitureitem;
		public List<Item> journalitem;
		public List<Item> labourercontract;
		public List<Item> mountskin;
//		public List<Item> crystalleagueitem;

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Item {
			@JsonProperty("@uniquename")
			String uniquename;
			@JsonProperty("@itempower")
			String itempower;
			Enchantments enchantments;

			@JsonIgnoreProperties(ignoreUnknown = true)
			public static class Enchantments {
				List<Enchantment> enchantment;

				@JsonIgnoreProperties(ignoreUnknown = true)
				public static class Enchantment {
					List<Enchantment> enchantment;

					@JsonProperty("@enchantmentlevel")
					String enchantmentlevel;
					@JsonProperty("@itempower")
					String itempower;

				}
			}

		}

	}
}

