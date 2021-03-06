package org.domeos.api.model.deployment;

import java.util.List;

/**
 * Created by xxs on 16/1/19.
 */
public class DashboardQueryData {
    private String msg;
    private List<Double> cpudata;
    private List<Double> memdata;
    private boolean ok;

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setCpudata(List<Double> cpudata) {
        this.cpudata = cpudata;
    }

    public List<Double> getCpudata() {
        return this.cpudata;
    }

    public void setMemdata(List<Double> memdata) {
        this.memdata = memdata;
    }

    public List<Double> getMemdata() {
        return this.memdata;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public boolean getOk() {
        return this.ok;
    }
}
