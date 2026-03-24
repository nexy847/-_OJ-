package com.oj.service;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oj.config.OjProperties;
import com.oj.entity.AnalysisEvent;
import com.oj.repository.AnalysisEventRepository;

@Service
public class AnalysisExportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AnalysisEventRepository analysisEventRepository;
    private final OjProperties properties;

    public AnalysisExportService(AnalysisEventRepository analysisEventRepository, OjProperties properties) {
        this.analysisEventRepository = analysisEventRepository;
        this.properties = properties;
    }

    @Scheduled(cron = "${oj.export.cron}")//判题好了的题目会被自动导出到hdfs
    public void scheduledExport() {
        exportPendingToHdfs();
    }

    @Transactional
    public int exportPendingToHdfs() {
        int batchSize = properties.getExport().getBatchSize();
        List<AnalysisEvent> events = analysisEventRepository.findPending(PageRequest.of(0, batchSize));
        if (events.isEmpty()) {
            return 0;
        }

        String hdfsUri = properties.getExport().getHdfsUri();
        String hdfsDir = properties.getExport().getHdfsDir();
        String datePartition = LocalDate.now(ZoneOffset.UTC).format(DATE_FORMAT);
        String fileName = "events-" + Instant.now().toEpochMilli() + ".csv";//数字表示距离unix纪元的毫秒间隔
        Path targetDir = new Path(hdfsDir + "/dt=" + datePartition);
        Path targetFile = new Path(targetDir, fileName);

        applyHadoopUser();
        Configuration conf = buildConfiguration(hdfsUri);

        try (FileSystem fs = FileSystem.get(java.net.URI.create(hdfsUri), conf)) {
            fs.mkdirs(targetDir);
            try (FSDataOutputStream out = fs.create(targetFile, true);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                writer.write("submission_id,user_id,problem_id,language,verdict,time_ms,memory_kb,created_at");
                writer.newLine();
                for (AnalysisEvent event : events) {
                    writer.write(toCsvLine(event));
                    writer.newLine();
                }
                writer.flush();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to export to HDFS: " + ex.getMessage(), ex);
        }

        Instant exportedAt = Instant.now();
        for (AnalysisEvent event : events) {
            event.setExported(true);
            event.setExportedAt(exportedAt);
        }
        analysisEventRepository.saveAll(events);
        return events.size();
    }

    private String toCsvLine(AnalysisEvent event) {
        return String.join(",",
                event.getSubmissionId().toString(),
                event.getUserId().toString(),
                event.getProblemId().toString(),
                event.getLanguage().name(),
                event.getVerdict().name(),
                Long.toString(event.getTimeMs()),
                Long.toString(event.getMemoryKb()),
                event.getCreatedAt().toString());
    }

    private void applyHadoopUser() {
        String hadoopUser = properties.getExport().getHadoopUser();
        if (hadoopUser != null && !hadoopUser.isBlank()) {
            System.setProperty("HADOOP_USER_NAME", hadoopUser);
        }
    }

    private Configuration buildConfiguration(String hdfsUri) {
        Configuration conf = new Configuration();
        String confDir = properties.getExport().getHadoopConfDir();
        if (confDir != null && !confDir.isBlank()) {
            java.nio.file.Path coreSite = Paths.get(confDir, "core-site.xml");
            if (Files.exists(coreSite)) {
                conf.addResource(new Path(coreSite.toUri()));
            }
            java.nio.file.Path hdfsSite = Paths.get(confDir, "hdfs-site.xml");
            if (Files.exists(hdfsSite)) {
                conf.addResource(new Path(hdfsSite.toUri()));
            }
        }
        conf.set("fs.defaultFS", hdfsUri);
        return conf;
    }
}
