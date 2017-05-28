package de.kochon.enrico.secrettalkmessenger.backend;

public interface ChannelCacheRefreshable {

   public void refreshConversationsFromCache(long idchannel);

   public void indicateRefresh();
   public void stopRefreshIndication();
   
}
