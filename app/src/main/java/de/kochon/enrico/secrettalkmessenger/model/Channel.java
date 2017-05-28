package de.kochon.enrico.secrettalkmessenger.model;

public class Channel {

   public long id;
   public final String name;
   public final String protocol;
   public String endpoint;
   public final boolean isforreceiving;

   public final String DEFAULT_PROTOCOL = "secrettalk";


   public Channel(String name, String endpoint, boolean isforreceiving) {
      this.id = -1;
      this.name = name;
      this.endpoint = endpoint;
      this.protocol = DEFAULT_PROTOCOL;
      this.isforreceiving = isforreceiving;
   }

   public Channel(long id, String name, String protocol, String endpoint, boolean isforreceiving) {
      this.id = id;
      this.name = name;
      this.protocol = protocol;
      this.endpoint = endpoint;
      this.isforreceiving = isforreceiving;
   }

   @Override
   public String toString() {
      return String.format("%s [%s:%s(%s) idchannel=%d]", name, protocol, endpoint, (isforreceiving?"in":"out"), id);
   }

	
	public int compareTo(final Channel otherChannel) {

      int result = 0;

		if (0 == result) result = name.compareTo(otherChannel.name);
		if (0 == result) result = protocol.compareTo(otherChannel.protocol);
		if (0 == result) result = endpoint.compareTo(otherChannel.endpoint);
      if (0 == result) {
         if (isforreceiving && !otherChannel.isforreceiving) result = 1;
         if (!isforreceiving && otherChannel.isforreceiving) result = -1;
      }

      return result;
	}
	

	@Override
	/**
	 * standardimplementation based on compareTo 
	 * taken from 
	 * Michael Inden, "Der Weg zum Java Profi", 2.aktualisierte und erweiterte Auflage, Seite 291
	 **/
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Channel otherChannel = (Channel) obj;
		return (0 == compareTo(otherChannel));
	}

	
	@Override
	/**
	 * ideas taken from
	 * Michael Inden, "Der Weg zum Java Profi", 2.aktualisierte und erweiterte Auflage, Seite 283ff
	 */
	public int hashCode() {
		final int basePrime = 17;

		return basePrime * basePrime * basePrime * (isforreceiving ? 1: 2)
				+ basePrime * basePrime * name.hashCode()
				+ basePrime * protocol.hashCode()
				+ endpoint.hashCode();
	}
}
