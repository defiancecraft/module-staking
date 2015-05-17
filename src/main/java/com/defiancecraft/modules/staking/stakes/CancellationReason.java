package com.defiancecraft.modules.staking.stakes;

import com.defiancecraft.modules.staking.Staking;

public enum CancellationReason {

	ALPHA_OFFLINE(Staking.LANG_GONE_OFFLINE, 1),
	ALPHA_LEFT_AREA(Staking.LANG_LEFT_AREA_CANCEL, 1),
	ALPHA_CANCELLED(Staking.LANG_GENERIC_CANCEL, 1),
	BETA_OFFLINE(Staking.LANG_GONE_OFFLINE, 2),
	BETA_LEFT_AREA(Staking.LANG_LEFT_AREA_CANCEL, 2),
	BETA_CANCELLED(Staking.LANG_GENERIC_CANCEL, 2),
	SHUTDOWN(Staking.LANG_SHUTDOWN, 0);
	
	private String field;
	private int player;
	
	CancellationReason(String field, int player) {
		this.field = field;
		this.player = player;
	}
	
	public boolean isAlpha() {
		return player == 1;
	}
	
	public boolean isBeta() {
		return player == 2;
	}
	
	public String getMessageField() {
		return field;
	}
	
}
