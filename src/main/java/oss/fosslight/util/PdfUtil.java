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
        List<OssMaster> ossReview = new ArrayList<>();
        List<LicenseMaster> licenseReview = new ArrayList<>();
        List<Vulnerability> vulnerabilityReview = new ArrayList<>();
        Map<String,LicenseMaster> licenseMasterMap = new HashMap<>();
        Map<String,Vulnerability> vulnerabilityMap = new HashMap<>();
        Map<String,OssMaster> ossMasterMap = new HashMap<>();
        String type = "";


        Project project = projectMapper.selectProjectMaster2(prjId);
        if(project.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
            type = CoConstDef.CD_DTL_COMPONENT_ID_ANDROID;
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
        if(type.equals(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID))      {
            list = (List<ProjectIdentification>) map.get("mainData");
        } else{
            list = (List<ProjectIdentification>) map.get("rows");
        }
        for (ProjectIdentification projectIdentification : list) {
            if (projectIdentification.getExcludeYn().equals("N")) {
                //OssMasterReview
                OssMaster ossMaster = new OssMaster();
                ossMaster.setOssId(projectIdentification.getOssId());
                OssMaster oss = CoCodeManager.OSS_INFO_BY_ID.get(projectIdentification.getOssId());
                if (oss != null) {
                    if (!avoidNull(oss.getSummaryDescription()).equals("")) {
                        if(!ossMasterMap.containsKey(oss.getOssName().toUpperCase())) {
                            ossMasterMap.put(oss.getOssName().toUpperCase(), oss);
                        }
                    }

                    //VulnerabilityReview
                    projectIdentification.setOssName(oss.getOssName());
                    projectIdentification.setOssVersion(oss.getOssVersion());
                    ProjectIdentification prjOssMaster = projectMapper.getOssId(projectIdentification);
                    if (prjOssMaster != null && prjOssMaster.getCvssScore() != null) {
                        BigDecimal bdScore = new BigDecimal(Float.parseFloat(prjOssMaster.getCvssScore()));
                        BigDecimal mailingScore = new BigDecimal(CoCodeManager.getCodeExpString(CoConstDef.CD_VULNERABILITY_MAILING_SCORE, CoConstDef.CD_VULNERABILITY_MAILING_SCORE_STANDARD));

                        if (bdScore.compareTo(mailingScore) >= 0) {
                            Vulnerability vulnerability = new Vulnerability();
                            vulnerability.setOssName(oss.getOssName());
                            vulnerability.setVersion(oss.getOssVersion());
                            vulnerability.setCvssScore(prjOssMaster.getCvssScore());
                            String version = oss.getOssVersion();
                            if(oss.getOssVersion().equals("") || oss.getOssVersion() == null) {
                                version = "-";
                            }
                            vulnerability.setVulnerabilityLink(CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/vulnerability/vulnpopup?ossName=" + oss.getOssName() + "&ossVersion=" + version);
                            vulnerabilityMap.put(oss.getOssName().toUpperCase() + "_" + oss.getOssVersion().toUpperCase(), vulnerability);
                        }
                    }
                }
                List<String> licenseList = Arrays.asList(projectIdentification.getLicenseName().split(","));
                licenseList = licenseList.stream().distinct().collect(Collectors.toList());

                for(String license : licenseList) {
                    LicenseMaster lm = CoCodeManager.LICENSE_INFO_UPPER.get(license.toUpperCase());
                    if(lm != null) {
                        if (!isEmpty(avoidNull(lm.getRestrictionStr())) || (!isEmpty(avoidNull(lm.getDescription())) && !lm.getLicenseType().equals(CoConstDef.CD_LICENSE_TYPE_PMS))) {
                            if(!isEmpty(avoidNull(lm.getShortIdentifier()))) {
                                lm.setLicenseName(lm.getShortIdentifier());
                            }
                            licenseMasterMap.put(lm.getLicenseName(), lm);
                        }
                    }
                }
            }
        }

        for(OssMaster ossMaster : ossMasterMap.values()) {
            ossReview.add(ossMaster);
        }

        for (LicenseMaster licenseMaster : licenseMasterMap.values()) {
            licenseReview.add(licenseMaster);
        }

        for(Vulnerability vulnerability : vulnerabilityMap.values()){
            vulnerability.setVulnerabilityLink("<a href='" + vulnerability.getVulnerabilityLink() + "' target='_blank'>" + vulnerability.getVulnerabilityLink() + "</a>");
            vulnerabilityReview.add(vulnerability);
        }

        if(ossReview.size() > 0) {
            for(OssMaster om : ossReview) {
                om.setSummaryDescription(CommonFunction.lineReplaceToBR(om.getSummaryDescription()));
            }
        }

        if(licenseReview.size() > 0) {
            for(LicenseMaster lm : licenseReview) {
                lm.setDescription(CommonFunction.lineReplaceToBR(lm.getDescription()));
                lm.setRestrictionStr(CommonFunction.lineReplaceToBR(lm.getRestrictionStr()));
            }
        }

        if(ossReview.size() == 0 && licenseReview.size() == 0 && vulnerabilityReview.size() == 0 ) {
            return null;
        } else {
            convertData.put("OssReview", ossReview);
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
