package oss.fosslight.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.font.FontProvider;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.VerificationMapper;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.VulnerabilityService;


@Slf4j
public final class PdfUtil extends CoTopComponent {

    private static PdfUtil instance;
    private static VerificationMapper verificationMapper;
    private static LicenseMapper licenseMapper;
    private static OssMapper ossMapper;
    private static VulnerabilityService vulnerabilityService;
    private static ProjectService projectService;
    private static CodeMapper codeMapper;
    private static ProjectMapper projectMapper;

    public static PdfUtil getInstance() {
        if (instance == null) {
            instance = new PdfUtil();
            licenseMapper = (LicenseMapper) getWebappContext().getBean(LicenseMapper.class);
            ossMapper = (OssMapper) getWebappContext().getBean(OssMapper.class);
            projectService = (ProjectService) getWebappContext().getBean(ProjectService.class);
            vulnerabilityService = (VulnerabilityService) getWebappContext().getBean(VulnerabilityService.class);
            codeMapper = (CodeMapper) getWebappContext().getBean(CodeMapper.class);
            projectMapper = (ProjectMapper) getWebappContext().getBean(ProjectMapper.class);
            verificationMapper = (VerificationMapper) getWebappContext().getBean(VerificationMapper.class);
        }
        return instance;
    }

    public static boolean html2pdf(String contents, String file) throws IOException {
        ConverterProperties properties = new ConverterProperties();
        FontProvider fontProvider = new DefaultFontProvider(false, false, false);

        FontProgram fontProgram = FontProgramFactory.createFont(new ClassPathResource("/static/font/NanumBarunGothic.ttf").getURL().toString());
        fontProvider.addFont(fontProgram);
        properties.setFontProvider(fontProvider);

        List<IElement> elements = HtmlConverter.convertToElements(contents, properties);
        PdfDocument pdf = new PdfDocument(new PdfWriter(file));
        Document document = new Document(pdf);

        document.setMargins(50, 0, 50, 0);
        for (IElement element : elements) {
            document.add((IBlockElement) element);
        }
        document.close();

        return true;
    }

    public String getReviewReportHtml(String prjId) throws Exception
    {
        Map<String,Object> convertData = new HashMap<>();
        List<OssMaster> ossReviewSummary = new ArrayList<>();
        List<OssMaster> ossReviewImportantNotes = new ArrayList<>();
        List<LicenseMaster> licenseReview = new ArrayList<>();
        List<Vulnerability> vulnerabilityReview = new ArrayList<>();
        Map<String,LicenseMaster> licenseMasterMap = new HashMap<>();
        Map<String,Vulnerability> vulnerabilityMap = new HashMap<>();
        Map<String,OssMaster> ossMasterMapSummary = new HashMap<>();
        Map<String,OssMaster> ossMasterMapImportantNotes = new HashMap<>();
        String type = "";


        Project project = projectMapper.selectProjectMaster2(prjId);
        if (project.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
            type = CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM;
        } else {
            type = CoConstDef.CD_DTL_COMPONENT_ID_BOM;
        }

        project = projectMapper.selectProjectMaster(prjId);
        String url = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/project/shareUrl/" + prjId;
        String _s = "<a href='"+url+"' target='_blank'>" + project.getPrjName();
        if(!isEmpty(project.getPrjVersion())) {
            _s += "(" + project.getPrjVersion()+")";
        }
        _s += "</a>";
        project.setPrjName(_s);

        convertData.put("project", project);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        String strToday = sdf.format(c1.getTime());
        convertData.put("date", strToday);

        ProjectIdentification _param = new ProjectIdentification();
        _param.setReferenceDiv(type);
        _param.setReferenceId(prjId);
        _param.setMerge(CoConstDef.FLAG_NO);

        Map<String, Object> map = projectService.getIdentificationGridList(_param, true);
        List<ProjectIdentification> list = new ArrayList<ProjectIdentification>();
        list = (List<ProjectIdentification>) map.get("rows");
        for (ProjectIdentification projectIdentification : list) {
        	if (CoConstDef.FLAG_NO.equals(avoidNull(projectIdentification.getExcludeYn(), CoConstDef.FLAG_NO))) {
                //OssMasterReview
                OssMaster oss = CoCodeManager.OSS_INFO_BY_ID.get(projectIdentification.getOssId());
                if (oss != null && !isEmpty(oss.getOssName())) {
                    if (!avoidNull(oss.getSummaryDescription()).equals("")) {
                        if(!ossMasterMapSummary.containsKey(oss.getOssName().toUpperCase())) {
                            ossMasterMapSummary.put(oss.getOssName().toUpperCase(), oss);
                        }
                    }

                    if (!avoidNull(oss.getImportantNotes()).equals("")) {
                        if(!ossMasterMapImportantNotes.containsKey(oss.getOssName().toUpperCase())) {
                            ossMasterMapImportantNotes.put(oss.getOssName().toUpperCase(), oss);
                        }
                    }
                }
                
                //VulnerabilityReview
                if (!isEmpty(projectIdentification.getCvssScore())) {
            		BigDecimal bdScore = new BigDecimal(Float.parseFloat(projectIdentification.getCvssScore()));
            		BigDecimal mailingScore = new BigDecimal(CoCodeManager.getCodeExpString(CoConstDef.CD_VULNERABILITY_MAILING_SCORE, CoConstDef.CD_VULNERABILITY_MAILING_SCORE_STANDARD));
            		
            		if (bdScore.compareTo(mailingScore) >= 0) {
                        Vulnerability vulnerability = new Vulnerability();
                        vulnerability.setOssName(projectIdentification.getOssName());
                        vulnerability.setVersion(projectIdentification.getOssVersion());
                        vulnerability.setCvssScore(projectIdentification.getCvssScore());
                        String version = projectIdentification.getOssVersion();
                        if (isEmpty(projectIdentification.getOssVersion())) {
                            version = "-";
                        }
                        vulnerability.setVulnerabilityLink(CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/vulnerability/vulnpopup?ossName=" + projectIdentification.getOssName() + "&ossVersion=" + version);
                        vulnerabilityMap.put((projectIdentification.getOssName()+ "_" + projectIdentification.getOssVersion()).toUpperCase(), vulnerability);
                    }
            	}
                
                List<String> licenseList = Arrays.asList(projectIdentification.getLicenseName().split(","));
                licenseList = licenseList.stream().distinct().collect(Collectors.toList());

                for(String license : licenseList) {
                    LicenseMaster lm = CoCodeManager.LICENSE_INFO_UPPER.get(license.toUpperCase());
                    if(lm != null) {
                        String disclosingSrc = CoCodeManager.getCodeString(CoConstDef.CD_SOURCE_CODE_DISCLOSURE_SCOPE, lm.getDisclosingSrc());
                        if (!isEmpty(avoidNull(lm.getRestrictionStr())) || (!isEmpty(avoidNull(lm.getDescription())) && !disclosingSrc.equals("NONE") && project.getNetworkServerType().equals("N"))) {
                            if(!isEmpty(avoidNull(lm.getShortIdentifier()))) {
                                lm.setLicenseName(lm.getShortIdentifier());
                            }
                            licenseMasterMap.put(lm.getLicenseName(), lm);
                        }
                    }
                }
            }
        }
        
        for(OssMaster ossMaster : ossMasterMapSummary.values()) {
            OssMaster oss = new OssMaster();
            oss.setOssName("<a href='" + CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") +"/oss/list/" + ossMaster.getOssName() + "' target='_blank'>" + ossMaster.getOssName() + "</a>");
            oss.setSummaryDescription(ossMaster.getSummaryDescription());
            ossReviewSummary.add(oss);
        }

        for(OssMaster ossMaster : ossMasterMapImportantNotes.values()) {
            OssMaster oss = new OssMaster();
            oss.setOssName("<a href='" + CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") +"/oss/list/" + ossMaster.getOssName() + "' target='_blank'>" + ossMaster.getOssName() + "</a>");
            oss.setImportantNotes(ossMaster.getImportantNotes());
            ossReviewImportantNotes.add(oss);
        }

        for (LicenseMaster licenseMaster : licenseMasterMap.values()) {
            LicenseMaster license = new LicenseMaster();
            license.setLicenseName("<a href='" + CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/license/edit/" + licenseMaster.getLicenseId() + "' target='_blank'>" + licenseMaster.getLicenseName() + "</a>");
            license.setDescriptionHtml(licenseMaster.getDescriptionHtml());
            license.setRestrictionStr(licenseMaster.getRestrictionStr());
            licenseReview.add(license);
        }
        
        for(Vulnerability vulnerability : vulnerabilityMap.values()){
            vulnerability.setVulnerabilityLink("<a href='" + vulnerability.getVulnerabilityLink() + "' target='_blank'>" + vulnerability.getVulnerabilityLink() + "</a>");
            vulnerabilityReview.add(vulnerability);
        }

        if(ossReviewSummary.size() > 0) {
            for(OssMaster om : ossReviewSummary) {
                om.setSummaryDescription(CommonFunction.lineReplaceToBR(om.getSummaryDescription()));
            }
        }

        if(ossReviewImportantNotes.size() > 0) {
            for(OssMaster om : ossReviewImportantNotes) {
                om.setImportantNotes(CommonFunction.lineReplaceToBR(om.getImportantNotes()));
            }
        }

        if(licenseReview.size() > 0) {
            for(LicenseMaster lm : licenseReview) {
                lm.setDescription(CommonFunction.lineReplaceToBR(lm.getDescription()));
                lm.setRestrictionStr(CommonFunction.lineReplaceToBR(lm.getRestrictionStr()));
            }
        }

        if(ossReviewSummary.size() == 0 && licenseReview.size() == 0 && vulnerabilityReview.size() == 0 && ossReviewImportantNotes.size() == 0) {
            return null;
        } else {
            convertData.put("OssReviewSummary", ossReviewSummary);
            convertData.put("OssReviewImportantNotes", ossReviewImportantNotes);
            convertData.put("LicenseReview", licenseReview);
            convertData.put("VulnerabilityReview", vulnerabilityReview);
            convertData.put("templateURL", "report/reviewReport.html");
            return CommonFunction.VelocityTemplateToString(convertData);
        }
    }

    /**
     * * Get velocity template content.
     *
     * @param path the path
     * @param model the model
     * @return the string
     * */

//    private String getVelocityTemplateContent(String path, Map<String, Object> model) {
//        VelocityContext context = new VelocityContext();
//        Writer writer = new StringWriter();
//        VelocityEngine vf = new VelocityEngine();
//        Properties props = new Properties();
//
//        for (String key : model.keySet()) {
//            if (!"templateUrl".equals(key)) {
//                context.put(key, model.get(key));
//            }
//        }
//
//        context.put("domain", CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org"));
//        context.put("commonFunction", CommonFunction.class);
//
//        props.put("resource.loader", "class");
//        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
//        props.put("input.encoding", "UTF-8");
//
//        vf.init(props);
//
//        try {
//            Template template = vf.getTemplate(path); // file name
//            template.merge(context, writer);
//
//            return writer.toString();
//        } catch (Exception e) {
//            log.error("Exception occured while processing velocity template:" + e.getMessage());
//        }
//        return "";
//    }

    public Map<String,Object> getPdfFilePath(String prjId) throws Exception{
        Map<String,Object> fileInfo = new HashMap<>();
        String fileName = "";
        String filePath = CommonFunction.emptyCheckProperty("reviewReport.path", "/reviewReport");

        Project project = projectMapper.selectProjectMaster(prjId);

        oss.fosslight.domain.File pdfFile = null;

        if(!isEmpty(project.getZipFileId())) {
            pdfFile = verificationMapper.selectVerificationFile(project.getZipFileId());
            fileName =  pdfFile.getOrigNm();
            filePath += java.io.File.separator + fileName;
        } else {
            pdfFile = verificationMapper.selectVerificationFile(project.getReviewReportFileId());
            fileName =  pdfFile.getOrigNm();
            filePath += java.io.File.separator + prjId + java.io.File.separator + fileName;
        }

        // Check File Exist
        java.io.File file = new java.io.File(filePath);
        if(!file.exists()){
            throw new Exception("Don't Exist PDF");
        }
        fileInfo.put("fileName",fileName);
        fileInfo.put("filePath",filePath);
        return  fileInfo;
    }
}
