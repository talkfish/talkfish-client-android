package de.kochon.enrico.secrettalkmessenger.backend;

public interface ChannelCacheRefreshable {

   public void refreshConversationsFromCache(int idchannel);

   public void indicateRefresh();
   public void stopRefreshIndication();
   
}
