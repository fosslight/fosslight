/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation;

import java.util.HashMap;

@SuppressWarnings("serial")
public class T2CoValidMap<K,V> extends HashMap<K, V> {
    
    @Override
	public V get(Object o){
        V v = super.get(o);
        
        if (v == null) {
        	throw new IllegalArgumentException("key " + o + " doesn't exist");
        }
        
        return v;
    }
    
    public V getEvenNull(Object o){
        return super.get(o);
    }
}