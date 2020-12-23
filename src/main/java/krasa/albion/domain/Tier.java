package krasa.albion.domain;

import krasa.albion.service.MarketItem;
import org.apache.commons.lang3.StringUtils;

public class Tier {
	private final String tier;

	public Tier(String tier) {
		this.tier = tier;
	}

	public String generateCode(MarketItem item) {
		String code = item.getCode();
		code = code.substring(2);

		if (this.tier.equals("---")) {
			return code;
		}
		if (tier.contains(".")) {
			String[] tierSplit = tier.split("\\.");
			String enchant = tierSplit[1];
			String tier = tierSplit[0];

			if (item.isMap()) {
				code = StringUtils.substringBeforeLast(code, "_") + "_" + (Integer.parseInt(enchant) + 1);
			}


			if (enchant.equals("0")) {
				return "T" + tier + code;
			} else {
				return "T" + tier + code + "@" + enchant;
			}
		} else {
			return "T" + tier + code;
		}
	}
}
