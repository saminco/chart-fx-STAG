package de.gsi.financial.samples.service;

import java.io.IOException;

import de.gsi.financial.samples.dos.OHLCVItem;

/**
 * Provides actual tick data 
 */
public interface TickOhlcvDataProvider {
    /**
   * Every get() returns tick ohlcv item. If it is replay mode - the boundary is reached the TickDataFinishedException is thrown. 
   * If the realtime mode is used - never-end loop is used. The thread waits to next data.
   * @return provides tick ohlcv data 
   * @throws TickDataFinishedException if the data are reached the boundary
   * @throws IOException - the data are not reachable
   */
    OHLCVItem get() throws TickDataFinishedException, IOException;
}
