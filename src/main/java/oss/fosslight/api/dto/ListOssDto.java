package oss.fosslight.api.dto;

import lombok.*;
import oss.fosslight.domain.OssMaster;

import java.util.List;
import java.util.Objects;

public class ListOssDto {
    public enum OssType {
        MULTI(1),
        DUAL(2),
        V_DIFF(3);

        public final int code;

        OssType(int code) {
            this.code = code;
        }
    }

    @Setter
    @Getter
    @Builder(toBuilder = true)
    public static class Request extends Paging {
        private String ossName;
        private String ossId;
        @Builder.Default
        private Boolean ossNameExact = false;
        private String licenseName;
        @Builder.Default
        private Boolean licenseNameExact = false;
        private String url;
        @Builder.Default
        private Boolean urlExact = false;
        private String description;
        private String copyright;
        private String deactivate;
        private String ossType;
        private String licenseType;
        private String creator;
        private String createdFrom;
        private String createdTo;
        private String modifier;
        private String modifiedFrom;
        private String modifiedTo;


        @Builder.Default
        @Setter(AccessLevel.NONE)
        private Boolean searchFlag = true;
        @Setter(AccessLevel.NONE)
        private String cvssScore;
        private Boolean versionCheck;

        public void setUrl(String url) {
            url = url
                    .replaceFirst("^((http|https)://)?(www\\.)?", "")
                    .replaceFirst("/$", "");
        }

        public void setDeactivate(List<String> flagList) {
            if (flagList.size() == 0 || (flagList.contains("1") && flagList.contains("0"))) {
                deactivate = null;
                return;
            }
            deactivate = Objects.equals(flagList.get(0), "1") ? "Y" : "N";
        }

        public void setOssType(List<Integer> typeNumbers) {
            var typeStr = "";

            if (typeNumbers.contains(0)) {
                typeStr += "N";
            }
            if (typeNumbers.contains(1)) {
                typeStr += "M";
            }
            if (typeNumbers.contains(2)) {
                typeStr += "D";
            }
            if (typeNumbers.contains(3)) {
                typeStr += "V";
            }
            this.ossType = typeStr;
        }

        public void setLicenseType(String type) {
            switch (type) {
                case "0":
                    licenseType = "PMS";
                    break;
                case "1":
                    licenseType = "WCP";
                    break;
                case "2":
                    licenseType = "CP";
                    break;
                case "3":
                    licenseType = "NA";
                    break;
                case "4":
                    licenseType = "PF";
                    break;
                default:
                    break;
            }
        }

        public String getLicenseNameQuery() {
            return "%" + licenseName.replaceAll("_", "\\_") + "%";
        }

        public OssMaster toOssMaster() {
            var ossMaster = new OssMaster();
            ossMaster.setOssName(ossName);
            ossMaster.setOssId(ossId);
            ossMaster.setOssNameAllSearchFlag((ossNameExact != null && ossNameExact) ? "Y" : "N");
            ossMaster.setLicenseName(licenseName);
            ossMaster.setLicenseNameAllSearchFlag((licenseNameExact != null && licenseNameExact) ? "Y" : "N");
            ossMaster.setHomepage(url);
            ossMaster.setHomepageAllSearchFlag((urlExact != null && urlExact) ? "Y" : "N");
            ossMaster.setSummaryDescription(description);
            if (deactivate != null) {
                ossMaster.setDeactivateFlag(deactivate);
            }
            ossMaster.setOssType(ossType);
            ossMaster.setcStartDate(createdFrom);
            ossMaster.setcEndDate(createdTo);
            ossMaster.setmStartDate(modifiedFrom);
            ossMaster.setmEndDate(modifiedTo);
            ossMaster.setCreator(creator);
            ossMaster.setModifier(modifier);

            return ossMaster;
        }
    }

    @Builder
    public static class Result {
        public List<OssDto> list;
        public int totalCount;
    }
}
