package krasa.albion.controller;

import krasa.albion.AlbionMarketClientApplication;

import java.io.FileNotFoundException;

public enum Sound {
	OK("Carcass_12divine.mp3"),
	FAIL("Carcass_4maps.mp3");

	public String path;

	Sound(String path) {
		this.path = path;
	}

	public String getUri() throws FileNotFoundException {
		return AlbionMarketClientApplication.class.getResource(path).toString();
	}
}
