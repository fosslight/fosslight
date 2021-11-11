/*
 * Copyright (c) 2021 Taeseong Yu
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.util.List;

import com.nhncorp.lucy.security.xss.XssPreventer;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;

@Slf4j
public class XssFilter {
    public static void licenseMasterFilter(List<LicenseMaster> list) {
        for(LicenseMaster licenseMaster : list) {

            String escapedLicenseName = XssPreventer.escape(licenseMaster.getLicenseName());
            String escapedLicenseType = XssPreventer.escape(licenseMaster.getLicenseType());
            String escapedShortIdentifier = XssPreventer.escape(licenseMaster.getShortIdentifier());
            String escapedDescription = XssPreventer.escape(licenseMaster.getDescription());
            String escapedWebpage = XssPreventer.escape(licenseMaster.getWebpage());

            licenseMaster.setLicenseName(escapedLicenseName);
            licenseMaster.setLicenseType(escapedLicenseType);
            licenseMaster.setShortIdentifier(escapedShortIdentifier);
            licenseMaster.setDescription(escapedDescription);
            licenseMaster.setWebpage(escapedWebpage);
        }
    }

    public static void licenseMasterFilter(LicenseMaster licenseMaster) {
        String escapedLicenseName = XssPreventer.escape(licenseMaster.getLicenseName());
        String escapedLicenseType = XssPreventer.escape(licenseMaster.getLicenseType());
        String escapedShortIdentifier = XssPreventer.escape(licenseMaster.getShortIdentifier());
        String escapedDescription = XssPreventer.escape(licenseMaster.getDescription());
        String escapedWebpage = XssPreventer.escape(licenseMaster.getWebpage());

        licenseMaster.setLicenseName(escapedLicenseName);
        licenseMaster.setLicenseType(escapedLicenseType);
        licenseMaster.setShortIdentifier(escapedShortIdentifier);
        licenseMaster.setDescription(escapedDescription);
        licenseMaster.setWebpage(escapedWebpage);
    }

    public static void ossMasterFilter(List<OssMaster> list) {
        for(OssMaster ossMaster : list) {

            String escapedLicenseName = XssPreventer.escape(ossMaster.getLicenseName());
            String escapedLicenseType = XssPreventer.escape(ossMaster.getLicenseType());
            String escapedSummaryDescription = XssPreventer.escape(ossMaster.getSummaryDescription());
            String escapedDownloadLocation = XssPreventer.escape(ossMaster.getDownloadLocation());
            String escapedHomepage = XssPreventer.escape(ossMaster.getHomepage());

            ossMaster.setLicenseName(escapedLicenseName);
            ossMaster.setLicenseType(escapedLicenseType);
            ossMaster.setSummaryDescription(escapedSummaryDescription);
            ossMaster.setDownloadLocation(escapedDownloadLocation);
            ossMaster.setHomepage(escapedHomepage);
        }
    }

    public static void ossMasterFilter(OssMaster ossMaster) {
        String escapedLicenseName = XssPreventer.escape(ossMaster.getLicenseName());
        String escapedLicenseType = XssPreventer.escape(ossMaster.getLicenseType());
        String escapedSummaryDescription = XssPreventer.escape(ossMaster.getSummaryDescription());
        String escapedDownloadLocation = XssPreventer.escape(ossMaster.getDownloadLocation());
        String escapedHomepage = XssPreventer.escape(ossMaster.getHomepage());

        ossMaster.setLicenseName(escapedLicenseName);
        ossMaster.setLicenseType(escapedLicenseType);
        ossMaster.setSummaryDescription(escapedSummaryDescription);
        ossMaster.setDownloadLocation(escapedDownloadLocation);
        ossMaster.setHomepage(escapedHomepage);
    }

    public static void projectFilter(List<Project> list) {
        for(Project project : list) {

            String escapedPrjName = XssPreventer.escape(project.getPrjName());

            project.setPrjName(escapedPrjName);
        }
    }

    public static void projectFilter(Project project) {
        String escapedName = XssPreventer.escape(project.getPrjName());

        project.setPrjName(escapedName);
    }
}
