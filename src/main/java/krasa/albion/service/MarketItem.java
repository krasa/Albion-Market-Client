package krasa.albion.service;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class MarketItem {
	private String name;
	private String id;
	private String code;

	public MarketItem(String[] split) {
		id = split[0].trim();
		code = split[1].trim();
		name = split[2].trim();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public boolean isMap() {
		return getName().contains(" Map ");
	}
}
