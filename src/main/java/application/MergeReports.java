package application;

import models.Report;
import services.DomHTMLParser;
import services.ReportDomainService;
import services.ReportFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MergeReports {
    private static final String REPORT_NAME_TO_FIND = "index.html";
    private static final String FOLDER_TO_LOOKUP = "src\\Reports\\ReportsLookUp";
    private static final String TEMPLATE_REPORT = "src\\Reports\\Template\\index.html";
    private static final String OUTPUT_REPORT_FILE = "src\\Reports\\Output\\Report_";
    private static final String OUTPUT_LOG_FILE = "src\\Reports\\Output\\merge.log";

    public static void main(String[] args) throws IOException {
        //1-Find reports in directory (*.index.html)
        //2-build model in memory from Html
        //3-Process all reports
        //4-build final report using Template.html

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss"));
        ReportFactory reportFactory = new ReportFactory();

        List<Report> reportsInMemory = findFiles(REPORT_NAME_TO_FIND, Paths.get(FOLDER_TO_LOOKUP)).map(DomHTMLParser::domToReport).collect(Collectors.toList());

        String reportName = OUTPUT_REPORT_FILE + currentDate + ".html";

        Path inputPath = Paths.get(TEMPLATE_REPORT);
        File outputFile = new File(reportName);
        try {
            outputFile.createNewFile();

            Path outputpath = Paths.get(reportName);

            Report finalReport = (new ReportDomainService()).merge(reportsInMemory);
            reportFactory.buildReportFile(finalReport, inputPath, outputpath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logReportsProcessed(reportsInMemory.size());
    }

    private static Stream<Path> findFiles(String fileNameToMatch, Path baseDir) throws IOException {
        return Files.find(baseDir, 5, (path, attributes) -> attributes.isRegularFile() && path.endsWith(fileNameToMatch));
    }

    public static void logReportsProcessed(int reportsCount) {
        Logger logger = Logger.getLogger("MergeLog");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler(OUTPUT_LOG_FILE, true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            String currentDateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    .format(Calendar.getInstance().getTime());
            // the following statement is used to log any messages
            logger.info("Run at: " + currentDateString + " - " + reportsCount + " Reports Processed");

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
