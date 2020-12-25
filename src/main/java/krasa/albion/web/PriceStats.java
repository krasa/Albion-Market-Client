package krasa.albion.web;

import javafx.scene.control.ListView;
import krasa.albion.commons.MyException;
import krasa.albion.domain.Quality;
import krasa.albion.domain.Tier;
import krasa.albion.service.ItemsCache;
import krasa.albion.service.MarketItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;

public class PriceStats {
	private static final Logger log = LoggerFactory.getLogger(PriceStats.class);

	private final List<String> cities;
	private final List<String> tiers;
	private final ItemsCache itemsCache;
	private final List<String> qualities;

	public PriceStats(ListView cities, ListView qualities, ListView tiers, ItemsCache itemsCache) {
		this.cities = cities.getSelectionModel().getSelectedItems();
		this.qualities = qualities.getSelectionModel().getSelectedItems();
		this.tiers = tiers.getSelectionModel().getSelectedItems();
//		double min = ip.getMin();
//		double max = ip.getMax();


		this.itemsCache = itemsCache;
	}

	public String path(MarketItem item) {
		String code = item.getCode();

		StringBuilder sb = new StringBuilder();

		if (tiers.isEmpty() || tiers.contains("---")) {
			sb.append(code);
		} else {
			HashSet<String> codes = new HashSet<>();
			for (int i = 0; i < tiers.size(); i++) {
				String tier = tiers.get(i);
				String adjustedCode = new Tier(tier).generateCode(item);
				if (itemsCache.containsCode(adjustedCode)) {
					codes.add(adjustedCode);
				} else {
					log.error("invalid code=" + adjustedCode);
//					throw new RuntimeException("invalid code=" + adjustedCode);
				}
			}
			if (codes.size() == 0) {
				throw new MyException("No items at this tier");
			}
			for (String s : codes) {
				sb.append(s);
				sb.append(",");
			}
		}
		normalize(sb);
		sb.append("?");
		if (!cities.isEmpty() && !cities.contains("---")) {
			sb.append("locations=");
			for (String city : cities) {
				sb.append(city);
				sb.append(",");
			}
		}
		normalize(sb);

		if (!item.isMap() && !qualities.isEmpty() && !qualities.contains("---")) {
			sb.append("&qualities=");
			for (String s : qualities) {
				sb.append(Quality.asCode(s));
				sb.append(",");
			}
		}
		normalize(sb);

		return "https://www.albion-online-data.com/api/v2/stats/prices/" + sb.toString();
	}

	private void normalize(StringBuilder sb) {
		if (sb.length() > 0 && sb.codePointAt(sb.length() - 1) == ',') {
			sb.setLength(sb.length() - 1);
		}
	}

}
