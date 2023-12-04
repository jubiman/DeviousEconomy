package com.jubiman.devious.forge.v1_20_1.deviouseconomy.enums;

public enum Transaction {
	// ADD
	ADD,
	BUY,
	PURCHASE,
	// REMOVE
	REMOVE,
	SELL,
	// SET
	SET;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
