package com.hxgis.timer;


import com.hxgis.util.SmbUtil;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

/**
 * Created by admin on 2018/1/4.
 */
@Component
public class FetchGrbFileFromSmb {
    private Logger logger = Logger.getLogger(getClass());

    @Value("${grb.extern-dir}")
    private String rootExtDir;

    private Thread t;

    @Autowired
    ParseGrbFileService service;

//    @Scheduled(cron = "0 0/30 * * * *")
    public void fetchFromSmb() {
        logger.info("-----------------开始执行抓取GRB文件定时器，抓取目录：smb://qxt:qxt123@10.104.129.38-----------------");
        DateTime now = DateTime.now();
        String ym = now.toString("yyyyMM");
        String ymd = now.toString("yyyyMMdd");
        String hour = now.getHourOfDay() >= 17 ? "20" : "08";
        String file24003Suffix = ymd + hour + "00_24003.GRB2";
        String file24024Suffix = ymd + hour + "00_24024.GRB2";
        String file07203Suffix = ymd + hour + "00_07203.GRB2";
        String file07224Suffix = ymd + hour + "00_07224.GRB2";
        String tmpPath = rootExtDir + "/tmp/";
        File downloadDir = new File(tmpPath);
        SmbFile remoteFile = null;
        try {
            remoteFile = new SmbFile("smb://qxt:qxt123@10.104.129.38/data/" + ym + "/release/");
            logger.info("-----------------成功连接到10.104.129.38共享目录-----------------");
            SmbFile[] smbFiles = new SmbFile[0];
            smbFiles = remoteFile.listFiles();
            Map<String, List<SmbFile>> fileMap = new HashMap<>();
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
            for (SmbFile s : smbFiles) {
                String filename = s.getName();
                if (filename.endsWith(file24024Suffix) || filename.endsWith(file24003Suffix) || filename.endsWith(file07203Suffix) || filename.endsWith(file07224Suffix)) {
                    if (filename.contains("ER03")) {
                        fileMap.get("ER03").add(s);
                    } else if (filename.contains("TMP")) {
                        fileMap.get("TMP").add(s);
                    } else if (filename.contains("TMAX")) {
                        fileMap.get("TMAX").add(s);
                    } else if (filename.contains("TMIN")) {
                        fileMap.get("TMIN").add(s);
                    } else if (filename.contains("ERH")) {
                        fileMap.get("ERH").add(s);
                    } else if (filename.contains("ECT")) {
                        fileMap.get("ECT").add(s);
                    } else if (filename.contains("VIS")) {
                        fileMap.get("VIS").add(s);
                    } else if (filename.contains("EDA10") && !filename.contains("EDA10MX")) {
                        fileMap.get("EDA10").add(s);
                    } else if (filename.contains("PPH")) {
                        fileMap.get("PPH").add(s);
                    } else if (filename.contains("FOG")) {
                        fileMap.get("FOG").add(s);
                    } else if (filename.contains("HZ")) {
                        fileMap.get("HZ").add(s);
                    } else if (filename.contains("SAND")) {
                        fileMap.get("SAND").add(s);
                    } else if (filename.contains("SSM")) {
                        fileMap.get("SSM").add(s);
                    } else if (filename.contains("HAIL")) {
                        fileMap.get("HAIL").add(s);
                    } else if (filename.contains("WP3")) {
                        fileMap.get("WP3").add(s);
                    }
                }
            }
            boolean parseFile = false;
            Map<String, String> changedFileMap = new HashMap<>();
            for (Map.Entry<String, List<SmbFile>> fileNamesEntry : fileMap.entrySet()) {
                //下载文件名称最新的文件
                String fileType = fileNamesEntry.getKey();
                List<SmbFile> files = fileNamesEntry.getValue();
                if (files.size() > 0) {
                    files.sort((a, b) -> (b.getName().split("_")[4].compareTo(a.getName().split("_")[4])));
                    //ftp服务器上最新的文件
                    SmbFile newestRemoteFile = files.get(0);
                    String newestRemoteFileName = newestRemoteFile.getName();
                    Long newestRemoteFileSize = newestRemoteFile.length();
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
                            SmbUtil.smbGet(newestRemoteFile, localFile);
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
        } catch (MalformedURLException e) {
            logger.error("URL错误:" + e.getMessage());
            e.printStackTrace();
        } catch (SmbException e) {
            logger.error("连接共享目录失败:" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("其它未知错误:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
