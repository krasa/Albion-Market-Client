package krasa.albion.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(value = {"?xml"})
public class ItemsWrapper {
	Items items;

	public Items getItems() {
		return items;
	}

	public void setItems(Items items) {
		this.items = items;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Items {
		List<Item> equipmentitem;
		List<Item> weapon;

		public List<Item> getEquipmentitem() {
			return equipmentitem;
		}

		public void setEquipmentitem(List<Item> equipmentitem) {
			this.equipmentitem = equipmentitem;
		}

		public List<Item> getWeapon() {
			return weapon;
		}

		public void setWeapon(List<Item> weapon) {
			this.weapon = weapon;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Item {
			@JsonProperty("@uniquename")
			String uniquename;
			@JsonProperty("@itempower")
			String itempower;
			Enchantments enchantments;

			public Enchantments getEnchantments() {
				return enchantments;
			}

			public void setEnchantments(Enchantments enchantments) {
				this.enchantments = enchantments;
			}

			public String getUniquename() {
				return uniquename;
			}

			public void setUniquename(String uniquename) {
				this.uniquename = uniquename;
			}

			public String getItempower() {
				return itempower;
			}

			public void setItempower(String itempower) {
				this.itempower = itempower;
			}

		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Enchantments {
			List<Enchantment> enchantment;

			public List<Enchantment> getEnchantment() {
				return enchantment;
			}

			public void setEnchantment(List<Enchantment> enchantment) {
				this.enchantment = enchantment;
			}
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Enchantment {
			@JsonProperty("@enchantmentlevel")
			String enchantmentlevel;
			@JsonProperty("@itempower")
			String itempower;

			public String getEnchantmentlevel() {
				return enchantmentlevel;
			}

			public void setEnchantmentlevel(String enchantmentlevel) {
				this.enchantmentlevel = enchantmentlevel;
			}

			public String getItempower() {
				return itempower;
			}

			public void setItempower(String itempower) {
				this.itempower = itempower;
			}
		}

	}
}

