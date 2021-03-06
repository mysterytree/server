package org.domeos.api.service.deployment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.domeos.api.mapper.cluster.ClusterBasicMapper;
import org.domeos.api.mapper.deployment.VersionMapper;
import org.domeos.api.model.cluster.ClusterBasic;
import org.domeos.api.model.deployment.*;
import org.domeos.api.service.cluster.ClusterLogService;
import org.domeos.api.service.deployment.DeploymentBiz;
import org.domeos.api.service.deployment.VersionBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 */
@Service("versionBiz")
public class VersionBizImpl implements VersionBiz{

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    VersionMapper versionMapper;

    @Autowired
    ClusterBasicMapper clusterBasicMapper;

    @Autowired
    ClusterLogService clusterLogService;

    @Autowired
    DeploymentBiz deploymentBiz;


    private void setLogDraft(Version version, long deployId) throws IOException {
        if (version.getLogDraft() != null && version.getLogDraft().needFlumeContainer()) {
            Deployment deployment = deploymentBiz.getDeployment(deployId);
            if (deployment != null) {
                String clusterName = deployment.getClusterName();
                ClusterBasic clusterBasic = clusterBasicMapper.getClusterBasicByName(clusterName);
                long clusterId = clusterBasic.getId();

                // set log flume image related
                clusterLogService.setLogDraft(version, clusterId);
                String logDraftCheckLegality = version.getLogDraft().checkContainerLegality();
                if (!StringUtils.isBlank(logDraftCheckLegality)) {
                    throw new IOException(logDraftCheckLegality);
                }
            }
        }
    }

    @Override
    public long createVersion(Version version) throws Exception {
        VersionDBProto proto = buildVersionDBProto(version);

        Long verNow = versionMapper.getMaxVersion(proto.getDeployId());
        if (verNow == null) {
            verNow = 0L;
        }

        verNow++;
        proto.setVersion(verNow);
        try {
            versionMapper.createVersion(proto);
        } catch (Exception e) {
            throw new Exception("create new version failed, please retry.", e);
        }
        return verNow;
    }

    @Override
    public Version getVersion(long deployId, long version) throws IOException {
        VersionDBProto proto = versionMapper.getVersion(deployId, version);
        return buildVersion(proto);
    }

    @Override
    public Version getNewestVersion(long deployId) throws IOException {
        VersionDBProto currentVersionDBProto = versionMapper.getNewestVersion(deployId);
        return buildVersion(currentVersionDBProto);
    }

    @Override
    public void deleteAllVersion(long deployId) {
        List<VersionDBProto> versionDBProtos = versionMapper.listVersionByDeployId(deployId);
        for (VersionDBProto proto : versionDBProtos) {
            versionMapper.deleteVersionById(proto.getVid());
        }
    }

    @Override
    public List<Version> listVersions(long deployId) throws IOException {
        List<VersionDBProto> protos = versionMapper.listVersionByDeployId(deployId);
        if (protos == null || protos.size() == 0) {
            return null;
        }
        List<Version> versions = new ArrayList<>(protos.size());
        for (VersionDBProto versionDBProto : protos) {
            Version version = buildVersion(versionDBProto);
            versions.add(version);
        }
        return versions;
    }

    @Override
    public VersionDetail buildVersionDetail(long deployId, long versionId) throws IOException {
        VersionDetail versionDetail = new VersionDetail();
        versionDetail.setVersion(versionId);
        versionDetail.setDeployId(deployId);

        Deployment deployment = deploymentBiz.getDeployment(deployId);
        if (deployment == null) {
            return null;
        }
        versionDetail.setClusterName(deployment.getClusterName());
        versionDetail.setDeployName(deployment.getDeployName());
        versionDetail.setHostEnv(deployment.getHostEnv());

        Version version = this.getVersion(deployId, versionId);
        if (version == null) {
            return null;
        }
        versionDetail.setCreateTime(version.getCreateTime());
        versionDetail.setContainerDrafts(version.getContainerDrafts());
        versionDetail.setLogDraft(version.getLogDraft());
        versionDetail.setLabelSelectors(version.getLabelSelectors());
        versionDetail.setNetworkMode(version.getNetworkMode());
        versionDetail.setVolumes(version.getVolumes());
        versionDetail.setHostList(version.getHostList());

        return versionDetail;
    }

    public final Version buildVersion(VersionDBProto versionDBProto) throws IOException {
        Version version = objectMapper.readValue(versionDBProto.getContents(), Version.class);
        version.setDeployId(versionDBProto.getDeployId());
        version.setVid(versionDBProto.getVid());
        version.setVersion(versionDBProto.getVersion());
        setLogDraft(version, versionDBProto.getDeployId());
        return version;
    }

    public final VersionDBProto buildVersionDBProto(Version version) throws JsonProcessingException {
        VersionDBProto proto = new VersionDBProto();
        proto.setDeployId(version.getDeployId());
        proto.setVersion(version.getVersion());
        proto.setContents(objectMapper.writeValueAsString(version));
        return proto;
    }

    public final Version buildVersion(DeploymentDraft deploymentDraft, long deployId) {
        Version version = new Version();
        version.setCreateTime(System.currentTimeMillis() / 1000);
        version.setDeployId(deployId);
        version.setContainerDrafts(deploymentDraft.getContainerDrafts());
        version.setLabelSelectors(deploymentDraft.getLabelSelectors());
        version.setLogDraft(deploymentDraft.getLogDraft());
        version.setVolumes(deploymentDraft.getVolumes());
        version.setHostList(deploymentDraft.getHostList());
        version.setNetworkMode(deploymentDraft.getNetworkMode());
        return version;
    }

    public final boolean versionExist(long deployId, long versionId) {
        List<VersionDBProto> versionDBProtos = versionMapper.listVersionByDeployId(deployId);
        for (VersionDBProto versionDBProto : versionDBProtos) {
            if (versionDBProto.getVersion() == versionId) {
                return true;
            }
        }
        return false;
    }

}
