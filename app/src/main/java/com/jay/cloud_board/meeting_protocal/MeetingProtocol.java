package com.jay.cloud_board.meeting_protocal;

/**
 * Created by Jay on 2019/3/3.
 */

public class MeetingProtocol {

	private String userId;
	private int protocolType;


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	@Override
	public String toString() {
		return "MeetingProtocol{" +
				"userId='" + userId + '\'' +
				", protocolType='" + protocolType + '\'' +
				'}';
	}
}
