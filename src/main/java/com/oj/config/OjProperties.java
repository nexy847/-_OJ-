package com.oj.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oj")
public class OjProperties {
    private final Judge judge = new Judge();
    private final Export export = new Export();
    private final Security security = new Security();
    private final Limits limits = new Limits();

    public Judge getJudge() {
        return judge;
    }

    public Export getExport() {
        return export;
    }

    public Security getSecurity() {
        return security;
    }

    public Limits getLimits() {
        return limits;
    }

    public static class Judge {
        private String workDir = "./data/work";
        private String testcaseDir = "./data/testcases";
        private int batchSize = 2;
        private Duration pollInterval = Duration.ofSeconds(1);
        private int maxConcurrency = 2;
        private int maxRetries = 2;
        private int retryDelaySeconds = 5;
        private double cpus = 1.0;
        private int memoryMb = 256;
        private int pidsLimit = 64;
        private int maxOutputKb = 1024;
        private int maxCodeSizeKb = 64;
        private Map<String, String> images = new HashMap<>();

        public Judge() {
            images.put("C", "oj-gcc");
            images.put("CPP", "oj-gcc");
            images.put("JAVA", "oj-java");
            images.put("PYTHON", "oj-python");
        }

        public String getWorkDir() {
            return workDir;
        }

        public void setWorkDir(String workDir) {
            this.workDir = workDir;
        }

        public String getTestcaseDir() {
            return testcaseDir;
        }

        public void setTestcaseDir(String testcaseDir) {
            this.testcaseDir = testcaseDir;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public Duration getPollInterval() {
            return pollInterval;
        }

        public void setPollInterval(Duration pollInterval) {
            this.pollInterval = pollInterval;
        }

        public double getCpus() {
            return cpus;
        }

        public void setCpus(double cpus) {
            this.cpus = cpus;
        }

        public int getMaxConcurrency() {
            return maxConcurrency;
        }

        public void setMaxConcurrency(int maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public int getRetryDelaySeconds() {
            return retryDelaySeconds;
        }

        public void setRetryDelaySeconds(int retryDelaySeconds) {
            this.retryDelaySeconds = retryDelaySeconds;
        }

        public int getMemoryMb() {
            return memoryMb;
        }

        public void setMemoryMb(int memoryMb) {
            this.memoryMb = memoryMb;
        }

        public int getMaxOutputKb() {
            return maxOutputKb;
        }

        public void setMaxOutputKb(int maxOutputKb) {
            this.maxOutputKb = maxOutputKb;
        }

        public int getMaxCodeSizeKb() {
            return maxCodeSizeKb;
        }

        public void setMaxCodeSizeKb(int maxCodeSizeKb) {
            this.maxCodeSizeKb = maxCodeSizeKb;
        }

        public int getPidsLimit() {
            return pidsLimit;
        }

        public void setPidsLimit(int pidsLimit) {
            this.pidsLimit = pidsLimit;
        }

        public Map<String, String> getImages() {
            return images;
        }

        public void setImages(Map<String, String> images) {
            this.images = images;
        }
    }

    public static class Export {
        private String hdfsUri = "hdfs://localhost:9000";
        private String hdfsDir = "/oj/analysis";
        private int batchSize = 2000;
        private String cron = "0 0 * * * *";
        private String hadoopUser = "hadoop";
        private String hadoopConfDir;

        public String getHdfsUri() {
            return hdfsUri;
        }

        public void setHdfsUri(String hdfsUri) {
            this.hdfsUri = hdfsUri;
        }

        public String getHdfsDir() {
            return hdfsDir;
        }

        public void setHdfsDir(String hdfsDir) {
            this.hdfsDir = hdfsDir;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public String getHadoopUser() {
            return hadoopUser;
        }

        public void setHadoopUser(String hadoopUser) {
            this.hadoopUser = hadoopUser;
        }

        public String getHadoopConfDir() {
            return hadoopConfDir;
        }

        public void setHadoopConfDir(String hadoopConfDir) {
            this.hadoopConfDir = hadoopConfDir;
        }
    }

    public static class Security {
        private String jwtSecret = "change-me-please-change-me";
        private long jwtExpirationMinutes = 240;
        private java.util.List<String> adminUsers = new java.util.ArrayList<>();
        private java.util.List<String> allowedOrigins = new java.util.ArrayList<>(
                java.util.List.of(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:4173",
                        "http://127.0.0.1:4173"
                )
        );

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public long getJwtExpirationMinutes() {
            return jwtExpirationMinutes;
        }

        public void setJwtExpirationMinutes(long jwtExpirationMinutes) {
            this.jwtExpirationMinutes = jwtExpirationMinutes;
        }

        public java.util.List<String> getAdminUsers() {
            return adminUsers;
        }

        public void setAdminUsers(java.util.List<String> adminUsers) {
            this.adminUsers = adminUsers;
        }

        public java.util.List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(java.util.List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Limits {
        private int submitPerMinute = 30;
        private int loginPerMinute = 10;

        public int getSubmitPerMinute() {
            return submitPerMinute;
        }

        public void setSubmitPerMinute(int submitPerMinute) {
            this.submitPerMinute = submitPerMinute;
        }

        public int getLoginPerMinute() {
            return loginPerMinute;
        }

        public void setLoginPerMinute(int loginPerMinute) {
            this.loginPerMinute = loginPerMinute;
        }
    }

}
