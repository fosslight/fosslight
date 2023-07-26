package oss.fosslight.api.controller.v1;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oss.fosslight.domain.LicenseMaster;

@Getter
@RequiredArgsConstructor
public class LicenseCreateRequest {

    private final String licenseName;
    private final String licenseType;
    private final String restriction;
    private final String restrictions;
    private final String obligationNotificationYn;
    private final String obligationDisclosingSrcYn;
    private final String shortIdentifier;
    private final String[] licenseNicknames;
    private final String[] webpages;
    private final String description;
    private final String licenseText;
    private final String attribution;
    private final String comment;

    public LicenseMaster toLicenseMaster() {
        final LicenseMaster licenseMaster = inputRequest();

        setDefault(licenseMaster);

        return licenseMaster;
    }

    private LicenseMaster inputRequest() {
        final LicenseMaster licenseMaster = new LicenseMaster();
        licenseMaster.setLicenseName(licenseName);
        licenseMaster.setLicenseType(licenseType);
        licenseMaster.setRestriction(restriction);
        licenseMaster.setRestrictions(restrictions);
        licenseMaster.setObligationNotificationYn(obligationNotificationYn);
        licenseMaster.setObligationDisclosingSrcYn(obligationDisclosingSrcYn);
        licenseMaster.setShortIdentifier(shortIdentifier);
        licenseMaster.setLicenseNicknames(licenseNicknames);
        licenseMaster.setWebpages(webpages);
        licenseMaster.setDescription(description);
        licenseMaster.setLicenseText(licenseText);
        licenseMaster.setAttribution(attribution);
        licenseMaster.setComment(comment);

        return licenseMaster;
    }

    private void setDefault(final LicenseMaster licenseMaster) {
        licenseMaster.setObligationNeedsCheckYn("N");
        licenseMaster.setReqLicenseTextYn("N");
        licenseMaster.setUserUseYn("N");
        licenseMaster.setDeptUseYn("N");
        licenseMaster.setDelYn ("N");
    }
}
