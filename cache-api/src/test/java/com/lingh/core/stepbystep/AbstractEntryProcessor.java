package com.lingh.core.stepbystep;

import javax.cache.processor.EntryProcessor;
import java.io.Serializable;

public abstract class AbstractEntryProcessor<K, V, T> implements EntryProcessor<K, V, T>, Serializable {
}
