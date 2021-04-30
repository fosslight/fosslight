/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import oss.fosslight.domain.ComBean;

public class ComBeanSerializer implements JsonSerializer<ComBean> {

	@Override
	public JsonElement serialize(ComBean vo, Type typeOfVo, JsonSerializationContext context) {
		return context.serialize((Object) vo);
	}
}
