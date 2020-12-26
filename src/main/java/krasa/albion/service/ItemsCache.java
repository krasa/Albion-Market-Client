package krasa.albion.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;
import krasa.albion.Launcher;
import krasa.albion.commons.MyException;
import krasa.albion.domain.Categories;
import krasa.albion.domain.Quality;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemsCache {
	private static final Logger log = LoggerFactory.getLogger(ItemsCache.class);
	List<MarketItem> items = new ArrayList<>();
	Map<String, MarketItem> itemsByCode = new HashMap<>();
	Map<String, MarketItem> itemsByName = new HashMap<>();
	Set<String> allCodes = new HashSet<>();

	private Map<String, ItemsWrapper.Items.Item> itemPowerByCode;
	private ItemsWrapper.Items itemDetails;

	public ItemsCache() throws Exception {
		List<String> strings = IOUtils.readLines(Launcher.class.getResourceAsStream("/ao-bin-dumps/formatted/items.txt"), "UTF-8");
		for (String string : strings) {
			String[] split = string.split(":");
			if (split.length == 3) {
				String code = split[1].trim();
				if (code.contains("_NONTRADABLE")) {
					continue;
				}
				allCodes.add(code);
				MarketItem e = new MarketItem(split);
				if (itemsByCode.containsKey(e.getCode())) {
					throw new RuntimeException(e.toString());
				}
				itemsByCode.put(e.getCode(), e);


				if (itemsByName.containsKey(e.getName())) {
					if (!e.getCode().contains("@")) {
						log.warn("duplicate name: " + e);
					}
				} else {
					itemsByName.put(e.getName(), e);
				}


//				if (code.contains("@") 
//						&& itemsByName.containsKey(split[2])
////						&& itemsByCode.containsKey(StringUtils.substringBefore(code, "@"))
//				
//				) {
//					log.warn("ignoring "+string);
//					continue;
//				}


//				String s = StringUtils.substringBeforeLast(code, "@");
//				if (NumberUtils.isDigits(s.substring(s.length() - 1))) {
//					System.err.println(code);
//				}


				items.add(e);
			} else {
				//				log.warn(Arrays.toString(split));
			}
		}


		itemPowerByCode = new HashMap<>();
		ObjectMapper xmlMapper = getObjectMapper();
//		Map map = xmlMapper.readValue(file, Map.class);
		ItemsWrapper items = xmlMapper.readValue(Launcher.class.getResourceAsStream("/ao-bin-dumps/items.json"), ItemsWrapper.class);
		itemDetails = items.items;
		for (ItemsWrapper.Items.Item item : itemDetails.equipmentitem) {
			itemPowerByCode.put(item.uniquename, item);
		}
		for (ItemsWrapper.Items.Item item : itemDetails.weapon) {
			itemPowerByCode.put(item.uniquename, item);
		}
	}

	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		return objectMapper;
	}

	public List<MarketItem> getAllItems() {
		return items;
	}

	public Set<String> autocompleteNames(ObservableList<Categories> selectedItems) {
		long start = System.currentTimeMillis();
		Set<String> names = new TreeSet<>();
		Set<String> codes = new TreeSet<>();
		if (selectedItems.isEmpty() || selectedItems.contains(Categories.ALL)) {
			add(codes, itemDetails.weapon);
			add(codes, itemDetails.equipmentitem);
			add(codes, itemDetails.mount);
			add(codes, itemDetails.farmableitem);
			add(codes, itemDetails.simpleitem);
			add(codes, itemDetails.consumableitem);
			add(codes, itemDetails.consumablefrominventoryitem);
			add(codes, itemDetails.furnitureitem);
			add(codes, itemDetails.journalitem);
			add(codes, itemDetails.labourercontract);
			add(codes, itemDetails.mountskin);
		} else {
			for (Categories selectedItem : selectedItems) {
				switch (selectedItem) {
					case ALL:
						break;
					case WEAPON:
						add(codes, itemDetails.weapon);
						break;
					case EQUIPMENT_ITEM:
						add(codes, itemDetails.equipmentitem);
						break;
					case MOUNT:
						add(codes, itemDetails.mount);
						break;
					case FARMABLE_ITEM:
						add(codes, itemDetails.farmableitem);
						break;
					case SIMPLE_ITEM:
						add(codes, itemDetails.simpleitem);
						break;
					case CONSUMABLE_ITEM:
						add(codes, itemDetails.consumableitem);
						break;
					case CONSUMABLE_FROM_INVENTORY:
						add(codes, itemDetails.consumablefrominventoryitem);
						break;
					case FURNITURE_ITEM:
						add(codes, itemDetails.furnitureitem);
						break;
					case JOURNAL_ITEM:
						add(codes, itemDetails.journalitem);
						break;
					case LABOURER_CONTRACT:
						add(codes, itemDetails.labourercontract);
						break;
					case MOUNT_SKIN:
						add(codes, itemDetails.mountskin);
						break;
					default:
						throw new RuntimeException(selectedItem.toString());
				}
			}
		}


		for (MarketItem item : getAllItems()) {
			String code = item.getCode();
			if (codes.contains(code)) {
				names.add(item.getName());
			} else if (codes.contains(StringUtils.substringBeforeLast(code, "@"))) {
				names.add(item.getName());
			}
		}
		long end = System.currentTimeMillis();
		log.info("Autocomplete size: " + names.size() + " : " + (end - start) + "ms");
		return names;
	}

	private void add(Set<String> codes, List<ItemsWrapper.Items.Item> mount) {
		for (ItemsWrapper.Items.Item item : mount) {
			String code = item.uniquename;
			codes.add(code);
		}
	}

	public String getName(String item_id) {
		MarketItem marketItem = itemsByCode.get(item_id);
		if (marketItem == null) {
			return item_id;
		}
		return marketItem.getName();
	}

	public List<MarketItem> getEligibleItems(String text) {
		text = text.trim();
		ArrayList<MarketItem> marketItems = new ArrayList<>();
		for (MarketItem item : items) {
			String name = item.getName();
			if (name.equals(text)) {
				marketItems.add(item);
			}
		}
		if (marketItems.size() > 1) {
			ArrayList<MarketItem> collect = marketItems.stream().filter(marketItem -> {
				boolean b = !marketItem.getCode().contains("@");
				return b;
			}).collect(Collectors.toCollection(ArrayList::new));
			marketItems = collect;
		}
		if (marketItems.size() == 0) {
			throw new MyException("No item found for: " + text);
		}
		return marketItems;
	}

	public boolean containsCode(String str) {
		return allCodes.contains(str);
	}

	public MarketItem getItemByName(String newValue) {
		newValue = newValue.trim();
		MarketItem marketItem = itemsByName.get(newValue);
		if (marketItem == null) {
			marketItem = itemsByName.get(StringUtils.substringBefore(newValue, "@"));
		}
		return marketItem;
	}

	public String getIp(String item_id, Integer quality) {
		String baseIp = getBaseIp(item_id);
		if (baseIp != null) {
			return String.valueOf(Integer.parseInt(baseIp) + Quality.codeToIp(quality));
		}
		return null;
	}

	private String getBaseIp(String item_id) {
		ItemsWrapper.Items.Item item = itemPowerByCode.get(StringUtils.substringBefore(item_id, "@"));
		if (item != null) {
			String level = StringUtils.substringAfter(item_id, "@");
			if (StringUtils.isNotEmpty(level)) {
				ItemsWrapper.Items.Item.Enchantments enchantments = item.enchantments;
				if (enchantments != null) {
					List<ItemsWrapper.Items.Item.Enchantments.Enchantment> enchantment1 = enchantments.enchantment;
					for (ItemsWrapper.Items.Item.Enchantments.Enchantment enchantment : enchantment1) {
						if (enchantment.enchantmentlevel.equals(level)) {
							return enchantment.itempower;
						}
					}

				}
			} else {
				return item.itempower;
			}
		}
		return null;
	}
}