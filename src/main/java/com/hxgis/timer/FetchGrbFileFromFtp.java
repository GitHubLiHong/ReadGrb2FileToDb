package com.hxgis.timer;

import com.hxgis.util.FtpUtil;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

/**
 * Created by admin on 2018/4/19.
 */
@Component
public class FetchGrbFileFromFtp {
    private Logger logger = Logger.getLogger(getClass());

    @Value("${grb.extern-dir}")
    private String rootExtDir;

    @Value("${ftp.ip}")
    private String ip;
    @Value("${ftp.port}")
    private Integer port;
    @Value("${ftp.username}")
    private String username;
    @Value("${ftp.password}")
    private String password;
    @Value("${ftp.dir}")
    private String dir;


    private Thread t;

    @Autowired
    ParseGrbFileService service;

    //@Scheduled(cron = "0 0/30 * * * *")
    public void fetchFromFTP() throws Exception {
        logger.info("-----------------开始执行抓取GRB文件定时器，抓取目录：ftp://bcwh:BCWH111@10.1.72.215:21/SPCC/BCWH/-----------------");
        DateTime now = DateTime.now();
        String year = now.toString("yyyy");
        String ym = now.toString("yyyyMM");
        String ymd = now.toString("yyyyMMdd");
        String hour = now.getHourOfDay() >= 16 ? "20" : "08";
        FtpUtil ftpUtil = new FtpUtil();
        boolean connect = ftpUtil.connect(ip, port, username, password);
        if (connect) {
            String file24003Suffix = ymd + hour + "00_24003.GRB2";
            String file24024Suffix = ymd + hour + "00_24024.GRB2";
            String file07203Suffix = ymd + hour + "00_07203.GRB2";
            String file07224Suffix = ymd + hour + "00_07224.GRB2";
            String remoteCd = dir + year + "/" + ymd + "/";
            logger.info("连接成功");
            FTPFile[] fileList = ftpUtil.getFileList(remoteCd);
            String tmpPath = rootExtDir + "/tmp";
            File downloadDir = new File(tmpPath);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            Map<String, List<FTPFile>> fileMap = new HashMap<>();
            fileMap.put("ER03", new ArrayList<>());//逐3小时降水
            fileMap.put("TMP", new ArrayList<>());//气温
            fileMap.put("TMAX", new ArrayList<>());//最高温
            fileMap.put("TMIN", new ArrayList<>());//最低温
            fileMap.put("ERH", new ArrayList<>());//相对湿度
            fileMap.put("ECT", new ArrayList<>());//云量
            fileMap.put("VIS", new ArrayList<>());//能见度
            fileMap.put("EDA10", new ArrayList<>());//风UV分量
            fileMap.put("PPH", new ArrayList<>());//降水相态
            fileMap.put("FOG", new ArrayList<>());//雾
            fileMap.put("HZ", new ArrayList<>());//霾
            fileMap.put("SAND", new ArrayList<>());//沙尘暴
            fileMap.put("SSM", new ArrayList<>());//雷暴
            fileMap.put("HAIL", new ArrayList<>());//冰雹
            fileMap.put("WP3", new ArrayList<>());//三小时天气现象
            for (FTPFile file : fileList) {
                String filename = file.getName();
                if (filename.endsWith(file24024Suffix) || filename.endsWith(file24003Suffix) || filename.endsWith(file07203Suffix)) {
                    if (filename.contains("ER03")) {
                        fileMap.get("ER03").add(file);
                    } else if (filename.contains("TMP")) {
                        fileMap.get("TMP").add(file);
                    } else if (filename.contains("TMAX")) {
                        fileMap.get("TMAX").add(file);
                    } else if (filename.contains("TMIN")) {
                        fileMap.get("TMIN").add(file);
                    } else if (filename.contains("ERH")) {
                        fileMap.get("ERH").add(file);
                    } else if (filename.contains("ECT")) {
                        fileMap.get("ECT").add(file);
                    } else if (filename.contains("VIS")) {
                        fileMap.get("VIS").add(file);
                    } else if (filename.contains("EDA10")) {
                        fileMap.get("EDA10").add(file);
                    } else if (filename.contains("PPH")) {
                        fileMap.get("PPH").add(file);
                    } else if (filename.contains("FOG")) {
                        fileMap.get("FOG").add(file);
                    } else if (filename.contains("HZ")) {
                        fileMap.get("HZ").add(file);
                    } else if (filename.contains("SAND")) {
                        fileMap.get("SAND").add(file);
                    } else if (filename.contains("SSM")) {
                        fileMap.get("SSM").add(file);
                    } else if (filename.contains("HAIL")) {
                        fileMap.get("HAIL").add(file);
                    } else if (filename.contains("WP3")) {
                        fileMap.get("WP3").add(file);
                    }
                }
            }
            boolean parseFile = false;
            Map<String, String> changedFileMap = new HashMap<>();
            //FTP下载文件
            for (Map.Entry<String, List<FTPFile>> fileNamesEntry : fileMap.entrySet()) {
                //下载文件名称最新的文件
                String fileType = fileNamesEntry.getKey();
                List<FTPFile> files = fileNamesEntry.getValue();
                if (files.size() > 0) {
                    files.sort((a, b) -> (b.getName().split("_")[4].compareTo(a.getName().split("_")[4])));
                    //ftp服务器上最新的文件
                    FTPFile newestRemoteFile = files.get(0);
                    String newestRemoteFileName = newestRemoteFile.getName();
                    Long newestRemoteFileSize = newestRemoteFile.getSize();
                    if (newestRemoteFileSize > (10 * 1 << 10)) {
                        //本地文件
                        String[] localFileNames = downloadDir.list((dir, name) -> (name.contains(fileType) && name.endsWith("GRB2") && (name.contains(file07203Suffix) || name.contains(file24024Suffix) || name.contains(file24003Suffix))));
                        Arrays.sort(localFileNames, (a, b) -> (b.split("_")[4].compareTo(a.split("_")[4])));
                        if (localFileNames.length == 0 || !newestRemoteFileName.equals(localFileNames[0]) || (Long.compare(newestRemoteFileSize, new File(tmpPath + localFileNames[0]).length()) != 0)) {
                            if (!parseFile) {
                                parseFile = true;
                            }
                            if (t != null) {
                                if (!service.getTerminate()) {
                                    Map<String, String> unParingMap = service.setTerminate(true);
                                    if (unParingMap != null) {
                                        for (Map.Entry<String, String> entry : unParingMap.entrySet()) {
                                            changedFileMap.put(entry.getKey(), entry.getValue());
                                        }
                                    }
                                    if (!service.isExit()) {
                                        logger.info("解析线程还在运行,下载线程让出CPU,等待解析进程退出");
                                        t.join();
                                    }
                                }
                            }
                            changedFileMap.put(fileType, newestRemoteFileName);
                            logger.info("正在下载" + fileType + ".GRB2文件" + newestRemoteFileName);
                            File localFile = new File(tmpPath + "/" + newestRemoteFileName);
                            ftpUtil.downSingleFile(remoteCd, newestRemoteFileName, localFile);
                            logger.info("下载完成" + newestRemoteFileName);
                        } else {
                            logger.info("当前" + downloadDir + "目录下的" + fileType + " GRB2文件已经是最新的：" + localFileNames[0]);
                        }
                    }
                } else {
                    logger.info("未找到" + ymd + hour + "时次对应的" + fileType + " GRB2文件");
                }
            }
            if (parseFile) {
                Map<String, String> finalChangedFileMap = changedFileMap;
                t = new Thread(() -> {
                    try {
                        service.setTerminate(false);
                        service.setExit(false);
                        service.parse(finalChangedFileMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                t.start();
            }
            logger.info("处理" + ymd + hour + "时次的GRB文件完成");
        }

    }
}
