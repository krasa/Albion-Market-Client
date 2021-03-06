package krasa.albion.domain;

import krasa.albion.service.MarketItem;
import org.apache.commons.lang3.StringUtils;

public class Tier {

	private int tier;
	private int enchant;

	public Tier(String code) {
		if (code.equals("---")) {
			this.tier = -1;
		} else {
			String[] tierSplit = code.split("\\.");
			if (tierSplit.length == 2) {
				enchant = Integer.parseInt(tierSplit[1]);
				this.tier = Integer.parseInt(tierSplit[0]);
			} else {
				tier = Integer.parseInt(tierSplit[0]);
				enchant = 0;
			}
		}
	}

	public String generateCode(MarketItem item) {
		String code = item.getCode();
		if (!code.matches("^T[0-9]_.*")) {
			return code;
		}
		code = code.substring(2);
		if (code.contains("@")) {
			code = StringUtils.substringBeforeLast(code, "@");
		}

		if (tier == -1) {
			return code;
		}
		if (item.isMap()) {
			code = StringUtils.substringBeforeLast(code, "_") + "_" + (enchant + 1);
		}
		if (enchant == 0) {
			return "T" + tier + code;
		} else {
			return "T" + tier + code + "@" + enchant;
		}
	}

	public int getIp() {
		if (tier == -1) {
			throw new RuntimeException("invalid tier");
		}
		return 700 + (tier - 4) * 100 + enchant * 100;
	}
}
