/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile(value = {"dev"})
public class CustomCacheEventLogger implements CacheEventListener<Object, Object> {

	@Override
	public void onEvent(CacheEvent<?, ?> cacheEvent) {
		log.debug("Cache event = {}, Key = {},  Old value = {}, New value = {}", cacheEvent.getType(),
				cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
	}
}
