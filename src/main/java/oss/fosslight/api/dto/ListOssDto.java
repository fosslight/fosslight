package oss.fosslight.api.dto;

import lombok.*;
import oss.fosslight.util.DateUtil;

import java.util.List;

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

    @Data
    @Builder(toBuilder = true)
    public static class Request extends Paging {
        private String ossName;
        private String ossId;
        private Boolean ossNameExact = false;
        private String licenseName;
        private Boolean licenseNameExact = false;
        private String homepageUrl;
        private Boolean homepageUrlExact = false;
        private String description;
        private String copyright;
        private Boolean deactivate;
        private String ossType;
        private String licenseType;
        private String creator;
        private String createdAtFrom;
        private String createdAtTo;
        private String modifier;
        private String modifiedAtFrom;
        private String modifiedAtTo;


        @Setter(AccessLevel.NONE)
        private Boolean searchFlag = true;
        @Setter(AccessLevel.NONE)
        private String cvssScore;
        @Setter(AccessLevel.NONE)
        private String versionCheck;

        public void setHomepageUrl(String url) {
            homepageUrl = url
                    .replaceFirst("^((http|https)://)?(www\\.)?", "")
                    .replaceFirst("/$", "");
        }

        public void setOssType(List<Integer> typeNumbers) {
            var typeStr = "";
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

        public String getLicenseNameQuery() {
            return "%" + licenseName.replaceAll("_", "\\_") + "%";
        }
    }

//    public static class Query extends Request {
//    }

    public static class Result {
        public List<OssDto> list;
    }
}
