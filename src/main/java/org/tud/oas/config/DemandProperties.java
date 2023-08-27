package org.tud.oas.config;

public class DemandProperties {
    private String populationFile;
    private Integer runInterval;
    private Integer deleteInterval;

    public String getPopulationFile() {
        return populationFile;
    }

    public void setPopulationFile(String populationFile) {
        this.populationFile = populationFile;
    }

    public Integer getRunInterval() {
        return runInterval;
    }

    public void setRunInterval(Integer runInterval) {
        this.runInterval = runInterval;
    }

    public Integer getDeleteInterval() {
        return deleteInterval;
    }

    public void setDeleteInterval(Integer deleteInterval) {
        this.deleteInterval = deleteInterval;
    }
}
