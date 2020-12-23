package krasa.albion.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemsCache {
	private static final Logger log = LoggerFactory.getLogger(ItemsCache.class);
	List<MarketItem> items = new ArrayList<>();
	Map<String, MarketItem> itemsByCode = new HashMap<>();
	Map<String, MarketItem> itemsByName = new HashMap<>();
	Set<String> allCodes = new HashSet<>();

	public ItemsCache() throws Exception {
		List<String> strings = IOUtils.readLines(new FileReader("ao-bin-dumps\\formatted\\items.txt"));
		for (String string : strings) {
			String[] split = string.split(":");
			if (split.length == 3) {
				String code = split[1].trim();
				if (code.contains("_NONTRADABLE")) {
					continue;
				}
				allCodes.add(code);

				if (code.contains("@") && itemsByCode.containsKey(StringUtils.substringBefore(code, "@"))) {
					continue;
				}


//				String s = StringUtils.substringBeforeLast(code, "@");
//				if (NumberUtils.isDigits(s.substring(s.length() - 1))) {
//					System.err.println(code);
//				}


				MarketItem e = new MarketItem(split);
				items.add(e);
				itemsByCode.put(e.getCode(), e);
				if (itemsByName.containsKey(e.getName())) {
					log.warn("duplicate name: " + e);
				}
				itemsByName.put(e.getName(), e);
			} else {
				//				log.warn(Arrays.toString(split));
			}
		}

	}

	public List<MarketItem> getItems() {
		return items;
	}

	public Set<String> names() {
		Set<String> objects = new TreeSet<>();
		for (MarketItem item : getItems()) {
			objects.add(item.getName());
		}
		return objects;
	}

	public String getName(String item_id) {
		MarketItem marketItem = itemsByCode.get(StringUtils.substringBefore(item_id, "@"));
		if (marketItem == null) {
			return item_id;
		}
		return marketItem.getName();
	}

	public List<MarketItem> getEligibleItems(String text) {
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
			throw new RuntimeException();
		}
		return marketItems;
	}

	public boolean containsCode(String str) {
		return allCodes.contains(str);
	}

	public MarketItem getItemByName(String newValue) {
		return itemsByName.get(newValue);
	}
}