package imc.market;

import imc.market.gridgain.IMCStorage;
import imc.market.streams.MarketOrdersStream;

/**
 *
 */
public class Bootstrapper {
    /**
     *
     * @param args
     */
    public static void main (String [] args) {
        //Initializing GridGain
        IMCStorage storage = new IMCStorage();
        storage.init();

        //Initializing marker orders stream
        MarketOrdersStream marketStream = new MarketOrdersStream(storage);
        marketStream.stream();

    }
}
