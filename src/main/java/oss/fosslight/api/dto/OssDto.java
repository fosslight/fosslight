package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.OssComponents;

import java.util.ArrayList;
import java.util.List;

@Data
public class OssDto implements ExcelData {
    String ossId;
    String ossType;
    String ossName;
    String ossVersion;
    String licenseName;
    String licenseType;
    String downloadUrl;
    String homepageUrl;
    String description;
    String cveId;
    String cvssScore;
    String creator;
    String created;
    String modifier;
    String modified;
    List<Character> obligations;

    String copyright;
    String nicknames;
    String attribution;

    Boolean exclude = false;

    public void setObligations(String obligationType) {
        var typeArr = obligationType.toCharArray();
        obligations = new ArrayList<>();
        obligations.add(typeArr[0] == '0' ? 'N' : 'Y');
        obligations.add(typeArr[1] == '0' ? 'N' : 'Y');
    }

    @Override
    public String[] toRow() {
        var notice = 'Y' == obligations.get(0);
        var source = 'Y' == obligations.get(1);
        var obligationString = "";
        if (notice && source) obligationString = "Notice & Distribute";
        else if (notice) obligationString = "Notice";
        var nicknameString = "";
        if (nicknames != null) {
            nicknameString = nicknames.replaceAll("\\|", "\r\n");
        }
        return new String[]{
                ossId,
                ossName,
                nicknameString,
                ossVersion,
                getOssTypeString(),
                licenseName,
                licenseType,
                obligationString,
                homepageUrl,
                downloadUrl,
                copyright,
                attribution,
                cvssScore
        };
    }

    private String getOssTypeString() {
        var rtn = new ArrayList<String>();
        if (CommonFunction.isEmpty(ossType)) {
            return "";
        }
        var ossTypeFlags = ossType.toCharArray();
        if (ossType.toCharArray()[0] == '1') {
            rtn.add("Multi");
        }

        if (ossType.toCharArray()[1] == '1') {
            rtn.add("Dual");
        }

        if (ossType.toCharArray()[2] == '1') {
            rtn.add("v-Diff");
        }
        return String.join(", ", rtn);
    }
}
