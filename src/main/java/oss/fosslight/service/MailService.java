/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import oss.fosslight.domain.History;

public interface MailService {
	public void sendMailRunTimeout();

	public void sendMail(History h, String[] receivers /*수신자*/, String[] ccIds/*참조*/, String[] bccIds/*숨은참조*/);
	
	public String[] selectMailAddrFromIds(String[] toIds);
	
	public void sendTempMail();
}
