package com.jay.cloud_board.meeting_protocal;

/**
 * Created by Jay on 2019/3/3.
 */

public class MeetingProtocol {

	private String userId;
	private String userRole;
	private int protocolType;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
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
				"userId='" + userId + '\'' +
				", protocolType='" + protocolType + '\'' +
				'}';
	}
}
