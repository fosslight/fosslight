package oss.fosslight.api.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListLicenseDto {
    @Setter
    @Getter
    @Builder(toBuilder = true)
    public static class Request extends Paging {
        String licenseName;
        Boolean licenseNameExact;
        String licenseType;
        String licenseText;
        String description;
        String homepageUrl;
        String creator;
        String modifier;
        String createdFrom;
        String createdTo;
        String modifiedFrom;
        String modifiedTo;

        @Setter(AccessLevel.NONE)
        List<String> restrictions;

        @Setter(AccessLevel.NONE)
        Boolean obligationNeedsCheck;
        @Setter(AccessLevel.NONE)
        Boolean obligationDisclosingSrc;
        @Setter(AccessLevel.NONE)
        Boolean obligationNotification;

        // TODO: used for obligation type 'None' only
        @Setter(AccessLevel.NONE)
        Boolean obligationNone;

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

        public void setRestrictions(List<String> list) {
            if (list.size() == 1 && list.get(0).equals("false")) {
                restrictions = null;
                return;
            }
            restrictions = list;
        }

        public void setObligations(String obligationChoice) {
            if (obligationChoice.equals("0")) {
                obligationNone = true;
            }
            if (obligationChoice.equals("1")) {
                obligationNotification = true;
                obligationDisclosingSrc = false;
            }
            if (obligationChoice.equals("2")) {
                obligationNotification = true;
                obligationDisclosingSrc = true;
            }
        }
    }

    @Builder
    public static class Result {
        public List<LicenseDto> list;
        public int totalCount;
    }
}
