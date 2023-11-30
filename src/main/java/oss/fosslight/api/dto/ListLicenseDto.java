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
        String webpage;
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

        public void setRestrictions(List<String> list) {
            if (list.size() == 1 && list.get(0).equals("false")) {
                restrictions = null;
                return;
            }
            restrictions = list.stream()
                    .map(i -> Integer.toString(Integer.parseInt(i) + 1))
                    .collect(Collectors.toList());
        }

        public void setObligations(List<String> list) {
            if (list.isEmpty() || (list.size() == 1 && list.get(0).equals("false"))) {
//                obligationNone = true;
                return;
            }
            if (list.contains("0")) {
                obligationNotification = true;
            }
            if (list.contains("1")) {
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
