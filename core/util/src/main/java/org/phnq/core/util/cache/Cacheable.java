package org.phnq.core.util.cache;

import java.io.Serializable;

/**
 *
 * @author pgostovic
 */
public interface Cacheable extends Serializable {

    String getKey();

    long getCacheTime();
}
