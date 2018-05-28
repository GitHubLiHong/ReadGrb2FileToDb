package com.hxgis.util;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.*;

/**
 * 共享目录文件传输方式
 *
 * @author YangHaiBin 2013-5-3
 */
public class SmbUtil {

    public static Logger logger = LoggerFactory.getLogger(SmbUtil.class);

	/*public static boolean downFile(String remoteUrl, String localDir){
		SmbFile remoteFile;
		try {
			remoteFile = new SmbFile(remoteUrl);
			if (!remoteFile.exists()) {
				logger.debug("共享文件不存在");
				return false;
			}
			SmbFile[] smbFiles = remoteFile.listFiles();
			for (SmbFile file : smbFiles) {
				smbGet(file, localDir);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}*/

    /**
     * @param remoteUrl 共享目录地址
     * @param localDir  本机地址
     */
    public static boolean smbGet(SmbFile remoteFile, File localFile) {
        boolean result = false;
        InputStream in = null;
        OutputStream out = null;
        try {
            File file = localFile.getParentFile();
            if (!file.exists()) {
                file.mkdirs();
            }
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            FileCopyUtils.copy(in, out);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }


    public static boolean uploadFile(String remoteUrl, String localDir) throws Exception {

        SmbFile remoteFile = new SmbFile(remoteUrl);
        logger.debug("#.url:" + remoteUrl);
        //创建目录
        if (!remoteFile.exists()) {
            logger.debug("创建目录");
            String parentPath = remoteFile.getParent();
            SmbFile remoteParent = new SmbFile(parentPath);
            if (!remoteParent.exists())
                remoteParent.mkdirs();

        }
        logger.debug("#in:" + localDir);
        InputStream in = new FileInputStream(localDir);
        logger.debug("#out:" + remoteFile);
        OutputStream out = new SmbFileOutputStream(remoteFile);
        logger.debug("#copy:");
        FileCopyUtils.copy(in, out);
        logger.debug("#finished copy");
        in.close();
        out.close();
        return true;
    }

    public static void main(String args[]) {
        try {
            SmbFile remoteFile = new SmbFile("smb://10.104.202.191/");
            String a[] = remoteFile.list();
            for (String aa : a) {
                System.out.println(aa);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	/*	SmbFile remoteFile;
		try {
			remoteFile = new SmbFile("smb://administrator:supertxwlk@172.20.5.13/Micaps3/sate/fy2e/");
			Long l = System.currentTimeMillis();
			//smbGet(remoteFile,new File("c://data/"));
			SmbFile[] files = remoteFile.listFiles();
			for(SmbFile f : files){
				if(f.getName().matches("Z_SATE_C_BAWX_\\d{14}_P_FY2E_ANI_(IR[1234]|VIS)_R04_\\d{8}_\\d{4}.AWX")){
					long modify = f.getLastModified();
					long time = (new Date()).getTime();
					
					if(true){ // time 世界时间  加5小时 后 拿到最近3小时的数据
						String str[] = f.getName().split("_");
						File nf = File.createTempFile(SNUtil.next(), ".AWX");
						smbGet(remoteFile = new SmbFile("smb://administrator:supertxwlk@172.20.5.13/Micaps3/sate/fy2e/"+f.getName()),nf);
						ReadSatellite rs = new ReadSatellite();
						rs.readFile(nf);
						//rs.saveImage("/bcwh/datashare/sate/"+str[8].toLowerCase()+"/"+str[4]+".png");
						rs.saveImage("c://data/"+str[8].toLowerCase()+"/"+str[4]+".png");
						nf.delete();
						System.out.println(f.getName());
					}
				}
			}
			Long ll = System.currentTimeMillis();
			System.out.println( ll - l);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        //downFile("smb://guest:jzsw@172.20.15.71/swyb/DTH/SWYB/","c://data/");

    }

}
