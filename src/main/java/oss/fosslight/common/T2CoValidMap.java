/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.util.HashMap;

@SuppressWarnings("serial")
public class T2CoValidMap<K,V> extends HashMap<K, V> {
    
    public V get(Object o){
        V v = (V)super.get(o);
        
        if(v == null) {
        	throw new IllegalArgumentException("key " + o + " doesn't exist");
        }
        
        return v;
    }
    
    public V getEvenNull(Object o){
        return (V)super.get(o);
    }
}