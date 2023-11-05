/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.api.dto.ListOssDto;
import oss.fosslight.api.dto.OssDto;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.repository.ApiOssMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.service.ApiOssService;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.StringUtil;

import static oss.fosslight.CoTopComponent.isEmpty;

@Service
public class ApiOssServiceImpl implements ApiOssService {
    /**
     * The api oss mapper.
     */
    @Autowired
    ApiOssMapper apiOssMapper;

    @Autowired
    OssMapper ossMapper;

    @Override
    public List<Map<String, Object>> getOssInfo(Map<String, Object> paramMap) {
        String rtnOssName = apiOssMapper.getOssName((String) paramMap.get("ossName"));

        if (!StringUtil.isEmpty(rtnOssName)) {
            paramMap.replace("ossName", rtnOssName);
        }

        return apiOssMapper.getOssInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getOssInfoByDownloadLocation(String downloadLocation) {
        return apiOssMapper.getOssInfoByDownloadLocation(downloadLocation);
    }

    @Override
    public List<Map<String, Object>> getLicenseInfo(String licenseName) {
        return apiOssMapper.getLicenseInfo(licenseName);
    }


    public String[] getOssNickNameListByOssName(String ossName) {
        List<String> nickList = null;
        if (!StringUtil.isEmpty(ossName)) {
            nickList = apiOssMapper.selectOssNicknameList(ossName);
            if (nickList != null) {
                nickList = nickList.stream()
                        .filter(CommonFunction.distinctByKey(nick -> nick.trim().toUpperCase()))
                        .collect(Collectors.toList());
            }
        }

        nickList = (nickList != null ? nickList : Collections.emptyList());
        return nickList.toArray(new String[nickList.size()]);
    }

    @Override
    public ListOssDto.Result listOss(ListOssDto.Request request) {
        var ossMaster = new OssMaster();

        HashMap<String, Object> map = new HashMap<>();

        ArrayList<OssDto> newList = new ArrayList<>();

        ListOssDto.Result result = new ListOssDto.Result();
        var list = apiOssMapper.selectOssList(request);
        result.list = list;

        String orgOssName = request.getOssName();
        List<String> multiOssList = ossMapper.selectMultiOssList(ossMaster);
        multiOssList.replaceAll(String::toUpperCase);

        // TODO:
//        for (OssMaster oss : list) {
//            if (multiOssList.contains(oss.getOssName().toUpperCase())) {
//                var query = request.toBuilder()
//                        .ossName(oss.getOssName())
//                        .ossId(oss.getOssId())
//                        .build();
//                List<OssMaster> subList = apiOssMapper.selectOssSubList(ossMaster);
//
//                newList.addAll(subList);
//            } else {
//                newList.add(oss);
//            }
//        }
//
//        ossMaster.setOssName(orgOssName);
//
//        // license name 처리
//        if (newList != null && !newList.isEmpty()) {
//            OssMaster param = new OssMaster();
//
//            for (OssMaster bean : newList) {
//                param.addOssIdList(bean.getOssId());
//            }
//
//            List<OssLicense> licenseList = apiOssMapper.selectOssLicenseList(param);
//
//            for (OssLicense licenseBean : licenseList) {
//                for (OssMaster bean : newList) {
//                    if (licenseBean.getOssId().equals(bean.getOssId())) {
//                        bean.addOssLicense(licenseBean);
//                        break;
//                    }
//                }
//            }
//
//            for (OssMaster bean : newList) {
//                if (bean.getOssLicenses() != null && !bean.getOssLicenses().isEmpty()) {
//                    bean.setLicenseName(CommonFunction.makeLicenseExpression(bean.getOssLicenses()));
//                }
//
//                // group by key 설정 grid 상에서 대소문자 구분되어 대문자로 모두 치화하여 그룹핑
//                bean.setGroupKey(bean.getOssName().toUpperCase());
//
//                // NICK NAME ICON 표시
//                if (CoConstDef.FLAG_YES.equals(ossMaster.getSearchFlag())) {
//                    bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
//
//                    if (!isEmpty(bean.getOssNickname())) {
//                        bean.setOssName("<span class='iconSet nick'>Nick</span>&nbsp;" + bean.getOssName());
//                    } else {
//                        bean.setOssName("<span class='iconSet nick dummy'></span>&nbsp;" + bean.getOssName());
//                    }
//                }
//            }
//        }
//
//        map.put("page", ossMaster.getCurPage());
//        map.put("total", ossMaster.getTotBlockSize());
//        map.put("records", records);
//        map.put("rows", newList);

        return result;
    }
}