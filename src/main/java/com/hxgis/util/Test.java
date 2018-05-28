package com.hxgis.util;

import com.hxgis.timer.ParseGrbFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Li Hong on 2018/5/27 0027.
 */
@RestController
@RequestMapping("cc")
public class Test {

    @Autowired
    ParseGrbFileService parseGrbFileService;

    @RequestMapping("/dd")
    public void test() throws Exception{

        Map<String, String> map = new HashMap<>();
        //map.put("EDA10", "Z_NWGD_C_BCWH_20180205100000_P_RFFC_SPCC-EDA10_201802050800_24003.GRB2");
        //map.put("ER03", "Z_NWGD_C_BCWH_20180214101311_P_RFFC_SPCC-ER03_201802140800_24003 (1).GRB2");
        map.put("ER03", "201805272000.GRB2");


        //ParseGrbFileService service = new ParseGrbFileService();

        parseGrbFileService.parse(map);
       //float[][][] aa =  service.parse(map);

    }

}
