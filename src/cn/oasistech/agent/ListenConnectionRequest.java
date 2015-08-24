package cn.oasistech.agent;

import mjoys.util.Formater;

public class ListenConnectionRequest {
	private IdTag idTag;

	public IdTag getIdTag() {
		return idTag;
	}

	public void setIdTag(IdTag idTag) {
		this.idTag = idTag;
	}
	
	@Override
	public String toString() {
		return Formater.formatEntry("idkey", idTag);
	}
}