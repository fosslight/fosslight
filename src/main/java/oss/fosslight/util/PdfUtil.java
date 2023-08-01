package oss.fosslight.util;

import com.itextpdf.html2pdf.HtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.*;
import oss.fosslight.repository.*;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.VulnerabilityService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static oss.fosslight.common.CoConstDef.CD_LICENSE_RESTRICTION;

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
        if(instance == null) {
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
    public static ByteArrayInputStream html2pdf(String html) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return inputStream;
    }
    public String getReviewReportHtml(String prjId){
        Map<String,Object> convertData = new HashMap<>();
        List<OssMaster> ossReview = new ArrayList<>();
        List<LicenseMaster> licenseReview = new ArrayList<>();
        List<Vulnerability> vulnerabilityReview = new ArrayList<>();
        Map<String,LicenseMaster> licenseMasterMap = new HashMap<>();
        String type = CoConstDef.CD_DTL_COMPONENT_ID_BOM;

        Project projectMaster = new Project();
        projectMaster.setPrjId(prjId);

        Project project = projectMapper.selectProjectMaster(projectMaster);
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
        List<ProjectIdentification> list = (List<ProjectIdentification>) map.get("rows");
        for(ProjectIdentification projectIdentification : list) {
            //OssMasterReview
            OssMaster ossMaster = new OssMaster();
            ossMaster.setOssId(projectIdentification.getOssId());
            OssMaster oss = ossMapper.selectOssOne(ossMaster);
            if (!oss.getSummaryDescription().equals("")) {
                ossReview.add(oss);
            }

            //VulnerabilityReview
            List<Map<String, Object>> _list = vulnerabilityService.selectMaxScoreNvdInfo(oss.getOssName(), oss.getOssVersion());
            for (Map<String, Object> m : _list) {
                BigDecimal bdScore = new BigDecimal(Float.toString((Float) m.get("cvssScore")));

                if (bdScore.compareTo(new BigDecimal("8.0")) < 0) {
                    continue;
                }
                Vulnerability vulnerability = new Vulnerability();
                vulnerability.setOssName(oss.getOssName());
                vulnerability.setVersion(oss.getOssVersion());
                vulnerability.setCvssScore(Float.toString((Float) m.get("cvssScore")));
                vulnerability.setVulnerabilityLink((String) m.get("vulnerabilityLink"));
                vulnerabilityReview.add(vulnerability);
            }

            //LisenseReview
            LicenseMaster licenseMaster = new LicenseMaster();
            licenseMaster.setLicenseId(projectIdentification.getLicenseId());

            LicenseMaster license = licenseMapper.selectLicenseOne(licenseMaster);
            if (!isEmpty(license.getRestriction())) {
                String[] arrRestrictions = license.getRestriction().split(",");
                String str = "";
                for (int i = 0; i < arrRestrictions.length - 2; i++) {
                    str += codeMapper.getCodeDtlNm(CD_LICENSE_RESTRICTION, arrRestrictions[i]) + ", ";
                }

                if (arrRestrictions.length > 0) {
                    str += codeMapper.getCodeDtlNm(CD_LICENSE_RESTRICTION, arrRestrictions[arrRestrictions.length - 1]);
                }
                license.setRestriction(str);
            }

            if (license.getRestriction() != null || license.getDescription() != null) {
                licenseMasterMap.put(license.getLicenseName(), license);
            }
        }
        for(LicenseMaster licenseMaster : licenseMasterMap.values()) {
            licenseReview.add(licenseMaster);
        }

        convertData.put("OssReview", ossReview);
        convertData.put("LicenseReview", licenseReview);
        convertData.put("VulnerabilityReview", vulnerabilityReview);
        String pdfContent = getVelocityTemplateContent("/template/report/reviewReport.html", convertData);
        return pdfContent;
    }

    /**
     * * Get velocity template content.
     *
     * @param path the path
     * @param model the model
     * @return the string
     * */
    private String getVelocityTemplateContent(String path, Map<String, Object> model) {
        VelocityContext context = new VelocityContext();
        Writer writer = new StringWriter();
        VelocityEngine vf = new VelocityEngine();
        Properties props = new Properties();

        for(String key : model.keySet()) {
            if(!"templateUrl".equals(key)) {
                context.put(key, model.get(key));
            }
        }

        context.put("domain", CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org"));
        context.put("commonFunction", CommonFunction.class);

        props.put("resource.loader", "class");
        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.put("input.encoding", "UTF-8");

        vf.init(props);

        try {
            Template template = vf.getTemplate(path); // file name
            template.merge(context, writer);

            return writer.toString();
        } catch (Exception e) {
            log.error("Exception occured while processing velocity template:" + e.getMessage());
        }
        return "";
    }

    public Map<String,Object> getPdfFilePath(String prjId) throws Exception{
        Map<String,Object> fileInfo = new HashMap<>();
        String fileName = "";
        String filePath = CommonFunction.emptyCheckProperty("notice.path", "/notice");

        Project project = new Project();
        project.setPrjId(prjId);
        project = projectMapper.selectProjectMaster(project);

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
