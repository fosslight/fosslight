/*
 * Copyright (c) 2021 Taeseong Yu
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.util.List;

import com.nhncorp.lucy.security.xss.XssPreventer;
import com.nhncorp.lucy.security.xss.XssFilter;
import com.nhncorp.lucy.security.xss.XssSaxFilter;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.PartnerMaster;

@Slf4j
public class CustomXssFilter {

    public static void licenseMasterFilter(List<LicenseMaster> list) {
        for (LicenseMaster licenseMaster : list) {

            licenseMasterFilter(licenseMaster);
        }
    }

    public static void licenseMasterFilter(LicenseMaster licenseMaster) {
        String escapedLicenseName = XssPreventer.escape(licenseMaster.getLicenseName());
        String escapedLicenseType = XssPreventer.escape(licenseMaster.getLicenseType());
        String escapedShortIdentifier = XssPreventer.escape(licenseMaster.getShortIdentifier());
        String escapedDescription = XssPreventer.escape(licenseMaster.getDescription());
        String escapedWebpage = XssPreventer.escape(licenseMaster.getWebpage());

        //licenseMaster.setLicenseName(escapedLicenseName);
        licenseMaster.setLicenseType(escapedLicenseType);
        licenseMaster.setShortIdentifier(escapedShortIdentifier);
        licenseMaster.setDescription(escapedDescription);
        licenseMaster.setWebpage(escapedWebpage);
    }

    public static void ossMasterFilter(List<OssMaster> list) {
        for (OssMaster ossMaster : list) {

            ossMasterFilter(ossMaster);
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
        for (Project project : list) {

            projectFilter(project);
        }
    }

    public static void projectFilter(Project project) {
        String escapedName = XssPreventer.escape(project.getPrjName());

        project.setPrjName(escapedName);
    }

    public static void partnerFilter(List<PartnerMaster> list) {
        for (PartnerMaster partner : list) {

            partnerFilter(partner);
        }
    }

    public static void partnerFilter(PartnerMaster partner) {
        String escapedPartnerName = XssPreventer.escape(partner.getPartnerName());
        String escapedSoftwareVersion = XssPreventer.escape(partner.getSoftwareVersion());
        String escapedSoftwareName = XssPreventer.escape(partner.getSoftwareName());
        String escapedDescription = XssPreventer.escape(partner.getDescription());
        String escapedDivision = XssPreventer.escape(partner.getDivision());

        partner.setPartnerName(escapedPartnerName);
        partner.setSoftwareVersion(escapedSoftwareVersion);
        partner.setSoftwareName(escapedSoftwareName);
        partner.setDescription(escapedDescription);
        partner.setDivision(escapedDivision);
    }
}

