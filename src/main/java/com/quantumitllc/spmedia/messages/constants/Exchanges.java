package com.quantumitllc.spmedia.messages.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Exchanges {

  public static final String DIRECT_EXCHANGE = "sp.exchanges.direct-exchange";
  public static final String TOPIC_EXCHANGE = "sp.exchanges.topic-exchange";
  public static final String FANOUT_EXCHANGE = "sp.exchanges.fanout-exchange";
}
