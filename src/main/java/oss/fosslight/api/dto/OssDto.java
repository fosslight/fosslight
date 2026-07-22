package oss.fosslight.api.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import oss.fosslight.common.CommonFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class OssDto implements ExcelData {
    String ossId;
    String ossType = "";
    @Setter(AccessLevel.NONE)
    Map<String, String> ossTypeMap = new LinkedHashMap<>();
    String ossName;
    String ossVersion;
    String licenseName;
    String licenseType;
    String downloadUrl = "";
    List<String> downloadUrls = new ArrayList<>();
    String homepageUrl = "";
    String description = "";
    String cveId = "";
    String cvssScore = "";
    String creator;
    String created;
    String modifier;
    String modified;
    List<Character> obligations;
    @Setter(AccessLevel.NONE)
    Map<String, String> obligationTypeMap = new LinkedHashMap<>();

    String copyright = "";
    String nicknames = "";
    @Setter(AccessLevel.NONE)
    List<String> nicknameList = new ArrayList<>();
    String attribution = "";

    Boolean exclude = false;

    public void setObligations(String obligationType) {
        var typeArr = obligationType.toCharArray();
        obligations = new ArrayList<>();
        if (typeArr.length == 0) {
            obligations.add('N');
            obligations.add('N');
        } else {
            obligations.add(typeArr[0] == '0' ? 'N' : 'Y');
            obligations.add(typeArr[1] == '0' ? 'N' : 'Y');
        }
        this.obligationTypeMap = buildObligationTypeMap(obligationType);
    }

    private Map<String, String> buildObligationTypeMap(String obligationType) {
        Map<String, String> rtn = new LinkedHashMap<>();
        var typeArr = obligationType.toCharArray();
        if (typeArr.length == 0) {
            rtn.put("Notice", "N");
            rtn.put("Source", "N");
        } else {
            rtn.put("Notice", typeArr[0] == '0' ? "N" : "Y");
            rtn.put("Source", typeArr[1] == '0' ? "N" : "Y");
        }
        return rtn;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        this.downloadUrls = splitDownloadUrls(downloadUrl);
    }

    public void setOssType(String ossTypeCode) {
        this.ossType = ossTypeCode;
        this.ossTypeMap = buildOssTypeMap(ossTypeCode);
    }

    public void setNicknames(String nicknames) {
        this.nicknames = nicknames;
        this.nicknameList = splitNicknames(nicknames);
    }

    @Override
    public String[] toRow() {
        var notice = 'Y' == obligations.get(0);
        var source = 'Y' == obligations.get(1);
        var obligationString = "";
        var downloadUrlsString = String.join(",", getDownloadUrls());
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
                downloadUrlsString,
                copyright,
                attribution,
                cvssScore
        };
    }

    private String getOssTypeString() {
        var rtn = new ArrayList<String>();
        if (CommonFunction.isEmpty(ossType) || ossType.length() < 3) {
            return "";
        }
        if (ossType.charAt(0) == '1') {
            rtn.add("Multi");
        }

        if (ossType.charAt(1) == '1') {
            rtn.add("Dual");
        }

        if (ossType.charAt(2) == '1') {
            rtn.add("v-Diff");
        }
        return String.join(", ", rtn);
    }

    private Map<String, String> buildOssTypeMap(String code) {
        Map<String, String> rtn = new LinkedHashMap<>();
        rtn.put("Multi", "N");
        rtn.put("Dual", "N");
        rtn.put("V-Diff", "N");

        if (CommonFunction.isEmpty(code) || code.length() < 3) {
            return rtn;
        }

        rtn.put("Multi", code.charAt(0) == '1' ? "Y" : "N");
        rtn.put("Dual", code.charAt(1) == '1' ? "Y" : "N");
        rtn.put("V-Diff", code.charAt(2) == '1' ? "Y" : "N");
        return rtn;
    }

    public List<String> getDownloadUrls() {
        if ((downloadUrls == null || downloadUrls.isEmpty()) && !CommonFunction.isEmpty(downloadUrl)) {
            downloadUrls = splitDownloadUrls(downloadUrl);
        }
        return downloadUrls;
    }

    private List<String> splitDownloadUrls(String source) {
        if (CommonFunction.isEmpty(source)) {
            return new ArrayList<>();
        }
        return Stream.of(source.split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> splitNicknames(String source) {
        if (CommonFunction.isEmpty(source)) {
            return new ArrayList<>();
        }
        return Stream.of(source.split("\\|"))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }
}
