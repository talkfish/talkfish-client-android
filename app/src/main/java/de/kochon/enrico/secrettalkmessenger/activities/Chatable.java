package de.kochon.enrico.secrettalkmessenger.activities;

public interface Chatable {

	public void addChatMessage(String message);
	
	public void addMetaInfo(String info);
	
	public void setNewMessageCount(Long count);
	
	public void endRefreshProcess();
	
}
